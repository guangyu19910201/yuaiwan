package com.yuaiwan.annoparse;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.yuaiwan.anno.ColumType;
import com.yuaiwan.anno.Column;
import com.yuaiwan.anno.IncreaseType;
import com.yuaiwan.anno.Table;
import com.yuaiwan.utils.Constants;
import com.yuaiwan.utils.ValidateUtils;


/**
 * 注解解析器,解析范围:@Column,@Table
 * @author guangyu
 */
public class SQLNanoParser{
	private static Logger logger = LoggerFactory.getLogger(Constants.logName_checkDBInfo);
	//实体类和数据库的映射关系类缓存Map<className,EntityDataMapping>
	private static final Map<String,EntityDataMapping> entityDataMappingCache = new HashMap<String, EntityDataMapping>();
	
	//删除标记字段类型取值类型范围
	private static final List<String> deleteFieldType = Arrays.asList(new String[]{"int","java.lang.Integer"});
	//创建时间标记字段类型取值类型范围
	private static final List<String> createTimeFieldType = Arrays.asList(new String[]{"java.lang.String"});
	//更新时间标记字段类型取值类型范围
	private static final List<String> updateTimeFieldType = Arrays.asList(new String[]{"java.lang.String"});
	//版本号标记字段类型取值类型范围
	private static final List<String> versionFieldType = Arrays.asList(new String[]{"java.lang.String"});
	
	/**
	 * 获取数据库表的注解
	 * @param clazz
	 * @return
	 */
	private static Table getTable(Class<?> clazz) {
		Table table = clazz.getAnnotation(Table.class);
		if(table==null){
			throw new RuntimeException("类-" + clazz.getName() + ",未用@TableName注解标识!!");
		}else{
			return table;
		}
	}
	
	/**
	 * 获取数据库表中的字段名
	 * @param fieldName
	 * @param column
	 * @return
	 */
	public static String getColumnName(Field field){
		String fieldName = field.getName();
		Column column = field.getAnnotation(Column.class);
		if(column!=null){
			if(ValidateUtils.isBlank(column.dataType())){
				throw new RuntimeException("字段:"+fieldName+"没有设置数据类型!!!");
			}
			return getColumnName(fieldName, column);
		}else{
			throw new RuntimeException("字段:"+fieldName+"没有设置对应的数据库字段");
		}
	}
	
	/**
	 * 获取数据库表中的字段名
	 * @param fieldName
	 * @param column
	 * @return
	 */
	public static String getColumnName(String fieldName,Column column){
		String columnName = column.value();
		if(StringUtils.isEmpty(columnName)){
			columnName = fieldName;
		}
		return columnName;
	}

	public static synchronized EntityDataMapping getEntityDataMapping(Class<?> clazz){
		//实体类名
		String className = clazz.getName();
		EntityDataMapping mapping = entityDataMappingCache.get(className);
		if(mapping != null){
			return mapping;
		}else{
			Table table = getTable(clazz);
			//初始化数据库表名
			String tableName = table.value();
			if(ValidateUtils.isBlank(tableName)){
				tableName = clazz.getSimpleName().toLowerCase();
			}
			//解析出实体类中和数据库对应的字段的集合,该方法获得的Field,一定能获取到Column
			List<Field> fieldList = new ArrayList<Field>();
			//缓存数据库字段和实体类字段的映射
			LinkedHashMap<String,String> columnFieldMapping = new LinkedHashMap<String, String>();
			//缓存数据库字段和实体类字段的映射(正常的列,columnFieldMapping除去特殊列)
			LinkedHashMap<String,String> normalColumnFieldMapping = new LinkedHashMap<String, String>();
			//获取实体类所有的字段进行遍历
			Field[] fields = clazz.getDeclaredFields();
			//数据库主键的自增类型
			IncreaseType increaseType = null;
			//创建时间数据库列名
			String createTimeColumn = null;
			//更新时间数据库列名
			String updateTimeColumn = null;
			//删除标记数据库列名
			String deleteColumn = null;
			//版本号数据库列名
			String versionColumn = null;
			List<String> primaryKeyList = new ArrayList<String>();
			for (Field field : fields) {
				Column column = field.getAnnotation(Column.class);
				if(column != null){
					String fieldName = field.getName();
					String columnName = getColumnName(fieldName, column);
					if(ValidateUtils.isBlank(column.dataType())){
						throw new RuntimeException("类:" + className + "-字段:"+fieldName+"没有设置数据类型!!!");
					}
					if(fieldName.startsWith(SQLGeneratorFactory.KEYWORD)){
						throw new RuntimeException("实体类'"+className+"'中的'"+fieldName+"'字段不能以关键字'"+SQLGeneratorFactory.KEYWORD+"'开头");
					}
					columnFieldMapping.put(columnName,fieldName);
					
					if(ColumType.PRIMARY_KEY == column.type()){
						//只取第一个主键字段作为程序中的主键判断
						if(primaryKeyList.size() == 0){
							increaseType = column.increaseType();
						}else{
							//当存在联合主键的时候,主键的生成形式改为手动生成
							increaseType = IncreaseType.MANUAL;
						}
						primaryKeyList.add(columnName);
						//将主键放到列的前面
						fieldList.add(primaryKeyList.size()-1, field);
					}else if(ColumType.COLUMN == column.type()){
						switch (column.specialColum()) {
							case DELETE:{//删除标记
								//删除标记类型不对应
								if(!deleteFieldType.contains(field.getType().getName())){
									throw new RuntimeException("实体类'"+className+"'中的'"+fieldName+"'字段删除标记类型不支持,仅支持'"+deleteFieldType+"'类型字段");
								}
								deleteColumn = columnName;break;
							}
							case CREATETIME:{//创建时间
								if(!createTimeFieldType.contains(field.getType().getName())){
									throw new RuntimeException("实体类'"+className+"'中的'"+fieldName+"'字段创建时间标记类型不支持,仅支持'"+createTimeFieldType+"'类型字段");
								}
								createTimeColumn = columnName;break;
							}
							case UPDATETIME:{//编辑时间
								if(!updateTimeFieldType.contains(field.getType().getName())){
									throw new RuntimeException("实体类'"+className+"'中的'"+fieldName+"'字段更新时间标记类型不支持,仅支持'"+updateTimeFieldType+"'类型字段");
								}
								updateTimeColumn = columnName;break;
							}
							case VERSION:{//版本号的格式
								if(!versionFieldType.contains(field.getType().getName())){
									throw new RuntimeException("实体类'"+className+"'中的'"+fieldName+"'字段版本号标记类型不支持,仅支持'"+versionFieldType+"'类型字段");
								}
								versionColumn = columnName;break;
							}
							default:
								normalColumnFieldMapping.put(columnName,fieldName);break;
						}
						fieldList.add(field);
					}
				}
			}
			if(ValidateUtils.isBlank(fieldList)){
				logger.warn("类-" + className + ",中没有配置任何数据库字段!!!");
			}
			if(primaryKeyList.size() == 0){
				logger.warn("类-" + className + ":没有设置主键");
			}
			mapping = new EntityDataMapping(className, tableName, primaryKeyList, increaseType, fieldList, columnFieldMapping, normalColumnFieldMapping);
			mapping.setCreateTimeColumn(createTimeColumn);
			mapping.setUpdateTimeColumn(updateTimeColumn);
			mapping.setDeleteColumn(deleteColumn);
			mapping.setVersionColumn(versionColumn);
			mapping.setTableComment(table.comment());
			mapping.setCheckTable(table.checkTable());
			mapping.setAutoTypes(Arrays.asList(table.auto()));
			entityDataMappingCache.put(className, mapping);
			return mapping;
		}
	}
}
