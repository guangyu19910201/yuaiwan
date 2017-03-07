package com.yuaiwan.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;

import com.yuaiwan.anno.ColumType;
import com.yuaiwan.anno.Column;
import com.yuaiwan.anno.ColumnCanNull;
import com.yuaiwan.anno.TableAutoType;
import com.yuaiwan.annoparse.ColumnInfo;
import com.yuaiwan.annoparse.SQLGenerator;
import com.yuaiwan.annoparse.SQLGeneratorFactory;
import com.yuaiwan.annoparse.SQLNanoParser;
import com.yuaiwan.resultset.RowMapperForModel;

/**
 * 用于检测数据库表的字段是否和一致实体类中的数据一致的执行器
 * 使用方法:
 * 	添加要检测的实体类:AutoTableExecutor.addAutoTableClass(clazz);
 * 	在spring容器加载完成后检测数据库字段和实体类字段:
 * 	<bean class="com.yuaiwan.utils.AutoTableExecutor">
		<constructor-arg ref="jdbcTemplate"/>
	</bean>
 * @author guangyu
 */
public class AutoTableExecutor implements ApplicationListener<ContextRefreshedEvent>{
	Logger logger = LoggerFactory.getLogger(Constants.logName_checkDBInfo);
	//自动表的实体类集合
	private static List<Class<?>> autoTableClassList = new ArrayList<Class<?>>();
	private String databaseName; 
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * 使用该执行器的时候需要注入JdbcTemplate
	 * @param jdbcTemplate
	 */
	public AutoTableExecutor(JdbcTemplate jdbcTemplate){
		this.jdbcTemplate = jdbcTemplate;
	}
	
	/**
	 * 添加需要校验的实体类的class
	 * @param clazz
	 */
	public synchronized static void addAutoTableClass(Class<?> clazz){
		SQLGenerator<?> sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
		if(sqlGenerator.getEntityDataMapping().isCheckTable() && !autoTableClassList.contains(clazz)){
			autoTableClassList.add(clazz);
		}
	}
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		//root application context 没有parent，他就是老大.
		if(event.getApplicationContext().getParent() == null){ 
			logger.info("检查数据库字段和实体类对应关系 start");
			//当spring容器初始化完成后就会执行该方法。 
			//初始化数据库名:
			if(databaseName == null){
				databaseName = getValue("SELECT DATABASE()");
			}
			if(autoTableClassList.size() > 0){
				for (Class<?> clazz : autoTableClassList) {
					SQLGenerator<?> sqlGenerator = SQLGeneratorFactory.initSQLGenerator(clazz);
					List<TableAutoType> typeList = sqlGenerator.getEntityDataMapping().getAutoTypes();
					if(typeList.contains(TableAutoType.AUTO_CREATETABLE)){
						autoCreateTable(sqlGenerator);
					}
					if (typeList.contains(TableAutoType.AUTO_CREATECOLUM)) {
						autoCreateColumn(sqlGenerator);
					}
					if (typeList.contains(TableAutoType.AUTO_DELETECOLUM)) {
						autoDeleteColumn(sqlGenerator);
					}
					if (typeList.contains(TableAutoType.AUTO_ADDCOMMENT)) {
						autoAddComment(sqlGenerator);
					}
					if (typeList.contains(TableAutoType.AUTO_CHECKDIFFERENCE)) {
						autoCheckDifference(sqlGenerator);
					}
				}
			}
			logger.info("检查数据库字段和实体类对应关系 over");
		} 
	}
	
	private List<ColumnInfo> queryColumnInfoList(SQLGenerator<?> sqlGenerator){
		String sql = sqlGenerator.getQueryColumnInfoSql();
		return jdbcTemplate.query(sql, new RowMapperForModel<ColumnInfo>(ColumnInfo.class));
	}
	
	private String getValue(String sql,Object... params){
		List<String> queryForList = jdbcTemplate.queryForList(sql, String.class, params);
		if(ValidateUtils.isBlank(queryForList)){
			return null;
		}else{
			return queryForList.get(0);
		}
	}
	
	/**
	 * 自动删除数据库中实体类没用的列
	 * @param clazz
	 * @param jdbcTemplate
	 */
	private void autoDeleteColumn(SQLGenerator<?> sqlGenerator) {
		//获取实体类中的所有数据库字段
		List<String> beanColumnList = new ArrayList<String>();
		//获取当前注解配置的表结构
		List<Field> fieldList = sqlGenerator.getEntityDataMapping().getFieldList();
		for (Field field : fieldList) {
			String columnName = SQLNanoParser.getColumnName(field);
			beanColumnList.add(columnName);
		}
		//获取数据库现有表结构
		List<ColumnInfo> columnInfoList = queryColumnInfoList(sqlGenerator);
		if(!ValidateUtils.isBlank(columnInfoList)){
			for (ColumnInfo columnInfo : columnInfoList) {
				String columnName = columnInfo.getField();
				if(!beanColumnList.contains(columnName)){
					jdbcTemplate.execute(sqlGenerator.getDropColumnSql(columnName));
				}
			}
		}
	}

	/**
	 * 判断表是否存在,不存在则创建表
	 * @param clazz
	 * @param jdbcTemplate
	 * @return
	 */
	private void autoCreateTable(SQLGenerator<?> sqlGenerator){
		//判断该表是否存在
		if(ValidateUtils.isBlank(getValue(sqlGenerator.getQueryTableNameSql()))){
			//创建该数据库表
			jdbcTemplate.execute(sqlGenerator.getCreateTableSql());
		}
	}
	
	/**
	 * 自动增加注释
	 */
	private void autoAddComment(SQLGenerator<?> sqlGenerator) {
		//获取数据库现有表结构
		List<ColumnInfo> columnInfoList = queryColumnInfoList(sqlGenerator);
		Map<String,ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		if(!ValidateUtils.isBlank(columnInfoList)){
			for (ColumnInfo columnInfo : columnInfoList) {
				columnInfoMap.put(columnInfo.getField(), columnInfo);
			}
		}
		//获取当前注解配置的表结构
		List<Field> fieldList = sqlGenerator.getEntityDataMapping().getFieldList();
		for (Field field : fieldList) {
			Column column = field.getAnnotation(Column.class);
			String columnName = SQLNanoParser.getColumnName(field.getName(),column);
			ColumnInfo columnInfo = columnInfoMap.get(columnName);
			if(columnInfo != null){
				//备注:
				String comment_table = columnInfo.getComment();
				String comment_bean = column.comment();
				
				//当表中字段备注为空,且bean中的备注不为空的时候,给字段增加备注
				if(ValidateUtils.isBlank(comment_table) && !ValidateUtils.isBlank(comment_bean)){
					String default_table = columnInfo.getDefaultValue();
					if(default_table == null){
						default_table = "null";
					}
					String default_bean = column.defaultValue();
					if(ValidateUtils.isBlank(default_bean)){
						default_bean = "null";
					}else{
						default_bean = default_bean.replace("'", "");
					}
					//判断数据库默认值和bean中配置的默认值是否一致,如果一直则继续,不一致则提示
					if(default_table.equals(default_bean)){
						if(!ValidateUtils.isBlank(column.defaultValue())){
							//ALTER TABLE `TABLE_NAME` MODIFY COLUMN `COLUMN_NAME` DATA_TYPE NOT NULL DEFAULT DEFAULT_VALUE COMMENT 'COLUMN_COMMENT'
							StringBuffer alterSql = new StringBuffer("ALTER TABLE");
							alterSql.append(" `").append(sqlGenerator.getTableName()).append("`");
							alterSql.append(" MODIFY COLUMN `").append(columnName).append("` ").append(columnInfo.getType());
							if(ColumnCanNull.NO.toString().equals(columnInfo.getCanNull())){
								alterSql.append(" NOT NULL");
							}
							alterSql.append(" DEFAULT "+column.defaultValue());
							alterSql.append(" COMMENT '").append(comment_bean).append("'");
							jdbcTemplate.execute(alterSql.toString());
						}
					}else{
						logger.warn(sqlGenerator.getTableName()+"实体类"+field.getName()+"字段新增备注错误,原因:默认值配置和数据库配置不一致");
					}
				}
			}
		}
	}
	
	/**
	 * 自动判断数据库同名表同名列的不同,并打印出单独的log
	 */
	private void autoCheckDifference(SQLGenerator<?> sqlGenerator) {
		//获取数据库现有表结构
		List<ColumnInfo> columnInfoList = queryColumnInfoList(sqlGenerator);
		Map<String,ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		if(!ValidateUtils.isBlank(columnInfoList)){
			for (ColumnInfo columnInfo : columnInfoList) {
				columnInfoMap.put(columnInfo.getField(), columnInfo);
			}
		}
		StringBuffer msg = new StringBuffer("");
		//获取当前注解配置的表结构
		List<Field> fieldList = sqlGenerator.getEntityDataMapping().getFieldList();
		for (Field field : fieldList) {
			Column column = field.getAnnotation(Column.class);
			String columnName = SQLNanoParser.getColumnName(field.getName(),column);
			ColumnInfo columnInfo = columnInfoMap.get(columnName);
			if(columnInfo != null){
				//数据类型,实体类和数据库中均不为空:
				String type_table = columnInfo.getType();
				String type_bean = column.dataType();
				//可以为null
				String canNull_table = columnInfo.getCanNull();
				String canNull_bean = column.canNull().toString();
				//默认值
				String default_table = columnInfo.getDefaultValue();
				if(default_table == null){
					default_table = "null";
				}
				String default_bean = column.defaultValue();
				if(ValidateUtils.isBlank(default_bean)){
					default_bean = "null";
				}else{
					default_bean = default_bean.replace("'", "");
				}
				//备注:
				String comment_table = columnInfo.getComment();
				String comment_bean = column.comment();
				
				//列相同,如果对应数据库没有该属性则自动增加该属性,否则比较属性的差异并记录
				StringBuffer info_table = new StringBuffer(type_table);
				StringBuffer info_bean = new StringBuffer(type_bean);

				if(canNull_table.equals(ColumnCanNull.NO.toString())){
					info_table.append(" NOT NULL");
				}
				if(canNull_bean.equals(ColumnCanNull.NO.toString()) || column.type()==ColumType.PRIMARY_KEY){
					info_bean.append(" NOT NULL");
				}
				
				if(!"null".equals(default_table)){
					info_table.append(" DEFAULT ").append(default_table);
				}
				if(!"null".equals(default_bean)){
					info_bean.append(" DEFAULT ").append(default_bean);
				}
				
				if(!ValidateUtils.isBlank(comment_table)){
					info_table.append(" COMMENT ").append("'").append(comment_table).append("'");
				}
				if(!ValidateUtils.isBlank(comment_bean)){
					info_bean.append(" COMMENT ").append("'").append(comment_bean).append("'");
				}
				String infoTable = info_table.toString();
				String infoBean = info_bean.toString();
				
				//输出配置区别:
				if(!infoBean.equals(infoTable)){//数据配置不一致
					msg.append(columnName).append("：{").append("bean:(").append(infoBean).append(")-->数据库:(").append(infoTable).append(")").append("}；");
				}
			}
		}
		if(msg.length() > 0){
			logger.warn(sqlGenerator.getTableName()+"实体类配置和数据库配置不一致:"+msg.toString());
		}
	}
	
	/**
	 * 判断字段是否存在,不存在则创建字段
	 * @param clazz
	 * @param jdbcTemplate
	 */
	private void autoCreateColumn(SQLGenerator<?> sqlGenerator){
		//获取数据库现有表结构
		List<ColumnInfo> columnInfoList = queryColumnInfoList(sqlGenerator);
		Map<String,ColumnInfo> columnInfoMap = new HashMap<String, ColumnInfo>();
		if(!ValidateUtils.isBlank(columnInfoList)){
			for (ColumnInfo columnInfo : columnInfoList) {
				columnInfoMap.put(columnInfo.getField(), columnInfo);
			}
		}
		//获取当前注解配置的表结构
		List<Field> fieldList = sqlGenerator.getEntityDataMapping().getFieldList();
		for (Field field : fieldList) {
			Column column = field.getAnnotation(Column.class);
			String columnName = SQLNanoParser.getColumnName(field.getName(),column);
			if(!columnInfoMap.containsKey(columnName)){
				StringBuffer sb = new StringBuffer("alter table `"+sqlGenerator.getTableName()+"` add ");
				sb.append("`"+columnName+"` "+column.dataType());
				if(column.canNull() == ColumnCanNull.NO){
					sb.append(" NOT NULL");
				}
				if(!ValidateUtils.isBlank(column.defaultValue())){
					sb.append(" DEFAULT "+column.defaultValue());
				}
				if(column.unique()){
					sb.append(" unique");
				}
				//增加备注
				if(!ValidateUtils.isBlank(column.comment())){
					sb.append(" COMMENT '"+column.comment()+"'");
				}
				jdbcTemplate.execute(sb.toString());//数据库表增加该字段
			}
		}
	}
}
