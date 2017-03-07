package com.yuaiwan.annoparse;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuaiwan.anno.Column;
import com.yuaiwan.anno.ColumnCanNull;
import com.yuaiwan.anno.IncreaseType;
import com.yuaiwan.utils.DateTools;
import com.yuaiwan.utils.ReflectUtils;
import com.yuaiwan.utils.StringUtils;
import com.yuaiwan.utils.ValidateUtils;

/**
 * sql生成器
 * 用于dao中对指定的实体类生成sql
 * @author guangyu
 * @param <T>
 */
public class SQLGenerator<T>{
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private EntityDataMapping entityDataMapping;
	private LinkedHashMap<String, String> allFieldColumnMap;
	private LinkedHashMap<String, String> allColumnFieldMap;
	//不包括primaryKey,deleted,createTime,updateTime,version
	private LinkedHashMap<String, String> columnFieldMap;
	//数据库表名
	private String tableName;
	//只有当primaryKeyNum==1的时候primaryKey才有值
	private String primaryKey;
	//数据库表主键列名集合
	private List<String> primaryKeyList;
	//数据库主键数
	private int primaryKeyNum;
	private String delete;
	private String createTime;
	private String updateTime;
	private String version;
	private IncreaseType increaseType;
	private String allColumnForQueryString;
	//删除标记值常量
	private final int deleted_true = 1;
	private final int deleted_false = 0;
	
	public SQLGenerator(EntityDataMapping entityDataMapping) {
		this.entityDataMapping = entityDataMapping;
		this.allColumnFieldMap = entityDataMapping.getColumnFieldMapping();
		this.columnFieldMap = entityDataMapping.getNormalColumnFieldMapping();
		this.allFieldColumnMap = new LinkedHashMap<String, String>();
		this.tableName = entityDataMapping.getTableName();
		this.primaryKeyList = entityDataMapping.getPrimaryKeyList();
		this.primaryKeyNum = entityDataMapping.getPrimaryKeyList().size();
		if(primaryKeyNum == 1){
			this.primaryKey = this.primaryKeyList.get(0);
		}
		this.delete = entityDataMapping.getDeleteColumn();
		this.createTime = entityDataMapping.getCreateTimeColumn();
		this.updateTime = entityDataMapping.getUpdateTimeColumn();
		this.version = entityDataMapping.getVersionColumn();
		this.increaseType = entityDataMapping.getIncreaseType();
		StringBuilder allColumnForQuerySb = new StringBuilder();
		for (Map.Entry<String, String> entry : this.allColumnFieldMap.entrySet()) {
			String columnName = entry.getKey();
			String fieldName = entry.getValue();
			allColumnForQuerySb.append("`").append(columnName).append("`,");
			this.allFieldColumnMap.put(fieldName, columnName);
		}
		if(allColumnForQuerySb.length() > 0){
			allColumnForQuerySb.deleteCharAt(allColumnForQuerySb.length()-1);
		}
		this.allColumnForQueryString = allColumnForQuerySb.toString();
	}
	
	//查询数据库信息sql start ---------------------------------------------
	/**
	 * 获取查询ColumnInfo的sql
	 * @return
	 */
	public String getQueryColumnInfoSql(){
		//查询结果集中包含字段:`Field`, `Type`, `Collation`, `Null`, `Key`, `Default`, `Extra`, `Privileges`, `Comment`
		return new StringBuilder("SHOW FULL COLUMNS FROM `").append(tableName).append("`").toString();
	}
	
	/**
	 * 获取实体类中的一些属性
	 * @return
	 */
	public EntityDataMapping getEntityDataMapping() {
		return entityDataMapping;
	}

	/**
	 * 获取删除某一列的sql
	 * @param tableName
	 * @param columnName
	 * @return
	 */
	public String getDropColumnSql(String columnName){
		return new StringBuilder("ALTER TABLE `").append(tableName).append("` DROP COLUMN `").append(columnName).append("`").toString();
	}
	
	/**
	 * 查询表名,用于查询表是否存在
	 * @param tableName
	 * @return
	 */
	public String getQueryTableNameSql(){
		return new StringBuilder("SHOW TABLES LIKE '").append(tableName).append("'").toString();
	}
	
	/**
	 * 获取创建表的sql
	 * @param clazz
	 * @param jdbcTemplate
	 */
	public String getCreateTableSql(){
		String tableComment = entityDataMapping.getTableComment();
		List<Field> fieldList = entityDataMapping.getFieldList();
		StringBuffer sb = new StringBuffer("CREATE TABLE `").append(tableName).append("`");
		if(!ValidateUtils.isBlank(fieldList)){
			sb.append(" (");
			for (Field field : fieldList) {
				Column column = field.getAnnotation(Column.class);
				String columnName = SQLNanoParser.getColumnName(field.getName(),column);
				sb.append("`"+columnName+"` "+column.dataType());
				//判断是否可以为空
				if(column.canNull() == ColumnCanNull.NO){
					sb.append(" NOT NULL");
				}
				//增加默认值
				if(!ValidateUtils.isBlank(column.defaultValue())){
					sb.append(" DEFAULT "+column.defaultValue());
				}
				//判断该列是否唯一
				if(column.unique()){
					sb.append(" unique");
				}
				//判断主键的增长方式
				if(column.increaseType() == IncreaseType.AUTO_INCREMENT){
					sb.append(" AUTO_INCREMENT");
				}
				//增加备注
				if(!ValidateUtils.isBlank(column.comment())){
					sb.append(" COMMENT '").append(column.comment()).append("'");
				}
				sb.append(",");
			}
			//判断主键是否大于0
			if(primaryKeyNum > 0){
				sb.append("PRIMARY KEY (`").append(StringUtils.join(primaryKeyList, "`,`")).append("`)");
			}else{
				sb.deleteCharAt(sb.length()-1);
			}
			sb.append(")");
		}
		if(!ValidateUtils.isBlank(tableComment)){
			sb.append(" COMMENT='").append(tableComment).append("'");
		}
		return sb.toString();
	}
	//查询数据库信息sql end ---------------------------------------------
	
	public String getPrimaryKey(){
		if(primaryKey == null){
			throw new RuntimeException("表-tableName:对应的实体类中没有设置主键");
		}
		return this.primaryKey;
	}

	/**
	 * 获取删除标记字段的值
	 * @param deleted(true:删除,false:未删除)
	 * @return
	 */
	protected int getDeleteValue(boolean deleted){
		if(deleted){
			return deleted_true;
		}else{
			return deleted_false;
		}
	}
	
	public String getTableName() {
		return tableName;
	}

	/**
	 * 获取新的版本号 
	 * @return
	 */
	protected Object getVersionNum() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	/**
	 * 获取去除"-"的uuid
	 * @return
	 */
	protected Object getUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	/**
	 * 获取创建时间
	 * @return
	 */
	protected Object getCreateTime(){
		return DateTools.getTime();
	}
	
	/**
	 * 获取修改时间
	 * @return
	 */
	protected Object getUpdateTime(){
		return DateTools.getTime();
	}
	
	/**
	 * 根据字段名或数据库列名获取字段名
	 * @param fieldOrColumnName
	 * @return
	 */
	public String getFieldName(String fieldOrColumnName){
		if(allFieldColumnMap.containsKey(fieldOrColumnName)){
			return fieldOrColumnName;
		}else{
			return allColumnFieldMap.get(fieldOrColumnName);
		}
	}
	
	/**
	 * 获取字段对应的列名
	 * @return
	 */
	public String getColumnName(String columnOrFieldName){
		if(allColumnFieldMap.containsKey(columnOrFieldName)){
			return columnOrFieldName;
		}else{
			return allFieldColumnMap.get(columnOrFieldName);
		}
	}
	
	/**
	 * 判断主键是否是自增的
	 * @return
	 */
	public boolean isAutoIncrement(){
		if(IncreaseType.AUTO_INCREMENT == increaseType){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 生成新增的SQL
	 * @throws Exception 
	 */
	public String getInsertSql(T entity){
		SQL sql = new SQL().INSERT_INTO(tableName);
		if(primaryKeyNum > 0){
			//联合主键的时候increaseType必为MANUAL
			for (String p : primaryKeyList) {
				if(IncreaseType.MANUAL == increaseType){//当不选择自增id的时候需要获取手动对id赋值
					sql.VALUES("`"+p+"`", ":"+getFieldName(p));
				}else if(IncreaseType.UUID == increaseType){
					String fieldName = getFieldName(p);
					sql.VALUES("`"+p+"`", ":"+getFieldName(p));
					ReflectUtils.setFieldValue(entity, fieldName, getUUID());
				}
			}
		}
		if(version != null){
			String fieldName = getFieldName(version);
			sql.VALUES("`"+version+"`", ":"+fieldName);
			ReflectUtils.setFieldValue(entity, fieldName, getVersionNum());
		}
		if(delete != null){
			String fieldName = getFieldName(delete);
			sql.VALUES("`"+delete+"`", ":"+fieldName);
			ReflectUtils.setFieldValue(entity, fieldName, 0);
		}
		if(createTime != null){
			String fieldName = getFieldName(createTime);
			sql.VALUES("`"+createTime+"`", ":"+fieldName);
			ReflectUtils.setFieldValue(entity, fieldName, getCreateTime());
		}
		if(updateTime != null){
			String fieldName = getFieldName(updateTime);
			sql.VALUES("`"+updateTime+"`", ":"+fieldName);
			ReflectUtils.setFieldValue(entity, fieldName, getUpdateTime());
		}
		for (Map.Entry<String, String> entry : columnFieldMap.entrySet()) {
			String column = entry.getKey();
			String fieldName = entry.getValue();
			sql.VALUES("`"+column+"`", ":"+fieldName);
		}
		return sql.toString();
		
	}
	
	/**
	 * 生成更新的SQL
	 */
	public SQLMap getUpdateSql(T entity) {
		SQL sql = new SQL().UPDATE(tableName);
		Map<String,Object> paramMap = new HashMap<String, Object>();
		if(version != null){
			//获取老的版本号并生成对应的条件语句
			String fieldName = getFieldName(version);
			String oldVersion = getNewPlaceholder(fieldName);
			sql.WHERE("`"+version+"`=:"+oldVersion);
			Object oldValue = ReflectUtils.getFieldValue(entity, fieldName);
			paramMap.put(oldVersion, oldValue);
			//生成新的版本号
			Object newValue = getVersionNum();
			sql.SET("`"+version+"`=:"+fieldName);
			paramMap.put(fieldName, newValue);
			ReflectUtils.setFieldValue(entity, fieldName, newValue);
		}
		if(updateTime != null){
			String fieldName = getFieldName(updateTime);
			Object newValue = getUpdateTime();
			sql.SET("`"+updateTime+"`=:"+fieldName);
			paramMap.put(fieldName, newValue);
			ReflectUtils.setFieldValue(entity, fieldName, newValue);
		}
		for (Map.Entry<String, String> entry : columnFieldMap.entrySet()) {
			String column = entry.getKey();
			String fieldName = entry.getValue();
			sql.SET("`"+column+"`=:"+fieldName);
			paramMap.put(fieldName, ReflectUtils.getFieldValue(entity, fieldName));
		}
		if(primaryKeyNum > 0){
			for (String p : primaryKeyList) {
				String pFieldName = getFieldName(p);
				sql.WHERE("`"+p+"`=:"+pFieldName);
				paramMap.put(pFieldName, ReflectUtils.getFieldValue(entity, pFieldName));
			}
			return new SQLMap(sql, paramMap);
		}else{
			throw new RuntimeException("实体类"+entityDataMapping.getClassName()+"没有设置主键,不能直接update整个对象");
		}
	}

	/**
	 * 生成不检测版本号更新的SQL
	 */
	public SQLMap getUnLockUpdateSql(T entity) {
		SQL sql = new SQL().UPDATE(tableName);
		Map<String,Object> paramMap = new HashMap<String, Object>();
		if(updateTime != null){
			String fieldName = getFieldName(updateTime);
			Object newValue = getUpdateTime();
			sql.SET("`"+updateTime+"`=:"+fieldName);
			paramMap.put(fieldName, newValue);
			ReflectUtils.setFieldValue(entity, fieldName, newValue);
		}
		for (Map.Entry<String, String> entry : columnFieldMap.entrySet()) {
			String column = entry.getKey();
			String fieldName = entry.getValue();
			sql.SET("`"+column+"`=:"+fieldName);
			paramMap.put(fieldName, ReflectUtils.getFieldValue(entity, fieldName));
		}
		if(primaryKeyNum > 0){
			for (String p : primaryKeyList) {
				String pFieldName = getFieldName(p);
				sql.WHERE("`"+p+"`=:"+pFieldName);
				paramMap.put(pFieldName, ReflectUtils.getFieldValue(entity, pFieldName));
			}
			return new SQLMap(sql, paramMap);
		}else{
			throw new RuntimeException("实体类"+entityDataMapping.getClassName()+"没有设置主键,不能直接update整个对象");
		}
	}
	
	/**
	 * 生成新的占位符
	 * @param placeholder
	 * @return
	 */
	private String getNewPlaceholder(String placeholder){
		return SQLGeneratorFactory.KEYWORD + placeholder;
	}
	
	public SQLMap getDeleteSql(Serializable id) {
		if(primaryKeyNum == 1){//只有一个主键
			if(delete == null){
				//不存在删除标记,真删
				return getDeleteDataSql(id);
			}else{
				Map<String,Object> where = new LinkedHashMap<String, Object>();
				where.put(primaryKey, id);
				return getDeleteSql(where);
			}
		}else{
			throw new RuntimeException("实体类"+entityDataMapping.getClassName()+"中设置有多个主键,该方法只支持单主键的形式");
		}
	}
	
	/**
	 * 生成删除的SQL
	 */
	public SQLMap getDeleteSql(Map<String,?> where) {
		if(delete == null){
			//不存在删除标记,真删
			return getDeleteDataSql(where);
		}else{
			SQL sql = new SQL().UPDATE(tableName);
			//存在删除标记,将删除标记置为已删除,更新更新时间
			Map<String,Object> paramMap = new HashMap<String, Object>();
			String deletePlaceholder = getFieldName(delete);
			sql.SET("`"+delete+"`=:"+deletePlaceholder);
			paramMap.put(deletePlaceholder, getDeleteValue(true));
			if(updateTime != null){
				String placeholder = getFieldName(updateTime);
				sql.SET("`"+updateTime+"`=:"+placeholder);
				paramMap.put(placeholder, getUpdateTime());
			}
			if(version != null){
				String placeholder = getFieldName(version);
				sql.SET("`"+version+"`=:"+placeholder);
				paramMap.put(placeholder, getVersionNum());
			}
			if(!ValidateUtils.isBlank(where)){
				for (Map.Entry<String,?> entry : where.entrySet()) {
					String placeholder = entry.getKey();
					Object value = entry.getValue();
					String column = getColumnName(placeholder);
					if(column != null){
						if(paramMap.containsKey(placeholder)){//如果参数重复
							placeholder = getNewPlaceholder(placeholder);
						}
						sql.WHERE("`"+column+"`=:"+placeholder);
						paramMap.put(placeholder, value);
					}
				}
			}
			//查询条件加上删除标记为未删除
			String newDeletePlaceholder = getNewPlaceholder(deletePlaceholder);
			sql.WHERE("`"+delete+"`=:"+newDeletePlaceholder);
			paramMap.put(newDeletePlaceholder, getDeleteValue(false));
			return new SQLMap(sql, paramMap);
		}
	}
	
	/**
	 * 生成删除的SQL,不判断版本号
	 */
	public SQLMap getDeleteDataSql(Serializable id) {
		if(primaryKeyNum == 1){//只有一个主键
			Map<String,Object> where = new LinkedHashMap<String, Object>();
			where.put(primaryKey, id);
			return getDeleteDataSql(where);
		}else{
			throw new RuntimeException("实体类"+entityDataMapping.getClassName()+"中设置有多个主键,该方法只支持单主键的形式");
		}
	}
	
	/**
	 * 生成删除的SQL
	 */
	public SQLMap getDeleteDataSql(Map<String,?> where) {
		SQL sql = new SQL().DELETE_FROM("`"+tableName+"`");
		Map<String,Object> paramMap = new HashMap<String, Object>();
		if(!ValidateUtils.isBlank(where)){
			for (Map.Entry<String,?> entry : where.entrySet()) {
				String placeholder = entry.getKey();
				Object value = entry.getValue();
				String column = getColumnName(placeholder);
				if(column != null){
					if(paramMap.containsKey(placeholder)){//如果参数重复
						placeholder = getNewPlaceholder(placeholder);
					}
					sql.WHERE("`"+column+"`=:"+placeholder);
					paramMap.put(placeholder, value);
				}
			}
		}
		return new SQLMap(sql, paramMap);
	}
	
	/**
	 * 生成更新的sql
	 * @param setMap
	 * @param where
	 * @return
	 */
	public SQLMap getUpdateSql(Map<String,?> setMap,Map<String,?> where) {
		SQL sql = new SQL().UPDATE("`"+tableName+"`");
		Map<String,Object> paramMap = new HashMap<String, Object>();
		if(!ValidateUtils.isBlank(setMap)){
			//setMap没有更新updateTime字段
			if(updateTime!=null && !setMap.containsKey(updateTime) && !setMap.containsKey(getFieldName(updateTime))){
				String placeholder = getFieldName(updateTime);
				sql.SET("`"+updateTime+"`=:"+placeholder);
				paramMap.put(placeholder, getUpdateTime());
			}
			//setMap没有更新version字段
			if(version!=null && !setMap.containsKey(version) && !setMap.containsKey(getFieldName(version))){
				String placeholder = getFieldName(version);
				sql.SET("`"+version+"`=:"+placeholder);
				paramMap.put(placeholder, getVersionNum());
			}
			for (Map.Entry<String,?> entry : setMap.entrySet()) {
				String placeholder = entry.getKey();
				Object value = entry.getValue();
				String column = this.getColumnName(placeholder);
				if(column != null){
					sql.SET("`"+column+"`=:"+placeholder);
					paramMap.put(placeholder, value);
				}
			}
		}
		if(!ValidateUtils.isBlank(where)){
			for (Map.Entry<String,?> entry : where.entrySet()) {
				String placeholder = entry.getKey();
				Object value = entry.getValue();
				String column = getColumnName(placeholder);
				if(column != null){
					if(paramMap.containsKey(placeholder)){//如果参数重复
						placeholder = getNewPlaceholder(placeholder);
					}
					sql.WHERE("`"+column+"`=:"+placeholder);
					paramMap.put(placeholder, value);
				}
			}
		}
		return new SQLMap(sql, paramMap);
	}
	
	/**
	 * 生成更新的sql
	 * @param setMap
	 * @param where
	 * @return
	 */
	public SQLMap getUpdateSql(Serializable id,Map<String,?> setMap) {
		if(primaryKeyNum == 1){//只有一个主键
			Map<String,Object> where = new LinkedHashMap<String, Object>();
			where.put(primaryKey, id);
			return getUpdateSql(setMap, where);	
		}else{
			throw new RuntimeException("实体类"+entityDataMapping.getClassName()+"中设置有多个主键,该方法只支持单主键的形式");
		}
	}

	public SQLMap getGetSql(Serializable id, List<String> fieldList,boolean checkDelete) {
		if(primaryKeyNum == 1){//只有一个主键
			Map<String,Object> where = new LinkedHashMap<String, Object>();
			where.put(primaryKey, id);
			return getQuerySql(fieldList, where,0,1,checkDelete);
		}else{
			throw new RuntimeException("实体类"+entityDataMapping.getClassName()+"中设置有多个主键,该方法只支持单主键的形式");
		}
	}
	
	public SQLMap getQuerySql(List<String> fieldList,Map<String, ?> where,int startIndex, int size,boolean checkDelete) {
		SQL sql = new SQL();
		if(ValidateUtils.isBlank(fieldList)){
			sql.SELECT(allColumnForQueryString);
		}else{
			for (int i = 0; i < fieldList.size(); i++) {
				String field = fieldList.get(i);
				String column = getColumnName(field);
				sql.SELECT("`"+column+"` '"+field+"'");
			}
		}
		sql.FROM("`"+tableName+"`");
		Map<String,Object> paramMap = new HashMap<String, Object>();
		if(!ValidateUtils.isBlank(where)){
			if(checkDelete && delete!=null){
				if(!where.containsKey(delete) && !where.containsKey(getFieldName(delete))){
					String placeholder = getFieldName(delete);
					sql.WHERE("`"+delete+"`=:"+placeholder);
					paramMap.put(placeholder, getDeleteValue(false));
				}
			}
			for (Map.Entry<String,?> entry : where.entrySet()) {
				String placeholder = entry.getKey();
				Object value = entry.getValue();
				String column = this.getColumnName(placeholder);
				if(column != null){
					sql.WHERE("`"+column+"`=:"+placeholder);
					paramMap.put(placeholder, value);
				}
			}
		}else{
			if(checkDelete && delete!=null){
				String placeholder = getFieldName(delete);
				sql.WHERE("`"+delete+"`=:"+placeholder);
				paramMap.put(placeholder, getDeleteValue(false));
			}
		}
		//只有当size大于0 的时候才会执行分页
		sql.LIMIT(startIndex, size);
		return new SQLMap(sql, paramMap);
	}
}
