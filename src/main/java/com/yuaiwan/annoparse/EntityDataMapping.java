package com.yuaiwan.annoparse;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;

import com.yuaiwan.anno.IncreaseType;
import com.yuaiwan.anno.TableAutoType;

/**
 * 实体类和数据库的映射关系类
 * 
 * @author guangyu
 */
public class EntityDataMapping {
	public EntityDataMapping(String className, String tableName,
			List<String> primaryKeyList, IncreaseType increaseType,List<Field> fieldList,
			LinkedHashMap<String, String> columnFieldMapping,LinkedHashMap<String, String> normalColumnFieldMapping) {
		this.className = className;
		this.tableName = tableName;
		this.primaryKeyList = primaryKeyList;
		this.increaseType = increaseType;
		this.fieldList = fieldList;
		this.columnFieldMapping = columnFieldMapping;
		this.normalColumnFieldMapping = normalColumnFieldMapping;
	}

	// 实体类名
	private String className;
	// 数据库表名
	private String tableName;
	// 主键数据库列名的集合
	private List<String> primaryKeyList;
	// 数据库主键的自增类型
	private IncreaseType increaseType;
	// 创建时间数据库列名
	private String createTimeColumn;
	// 更新时间数据库列名
	private String updateTimeColumn;
	// 删除标记数据库列名
	private String deleteColumn;
	// 版本号数据库列名
	private String versionColumn;
	// 记录实体类中存在Column注解的所有字段
	private List<Field> fieldList;
	// 缓存数据库字段和实体类字段的映射
	private LinkedHashMap<String,String> columnFieldMapping;
	// 缓存数据库字段和实体类字段的映射(正常的列,columnFieldMapping除去特殊列)
	private LinkedHashMap<String,String> normalColumnFieldMapping;
	// 数据库的备注
	private String tableComment;
	// 记录是否检查表结构
	private boolean checkTable;
	// 检查表结构的内容
	private List<TableAutoType> autoTypes;

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<String> getPrimaryKeyList() {
		return primaryKeyList;
	}

	public void setPrimaryKeyList(List<String> primaryKeyList) {
		this.primaryKeyList = primaryKeyList;
	}

	public IncreaseType getIncreaseType() {
		return increaseType;
	}

	public void setIncreaseType(IncreaseType increaseType) {
		this.increaseType = increaseType;
	}

	public String getCreateTimeColumn() {
		return createTimeColumn;
	}

	public void setCreateTimeColumn(String createTimeColumn) {
		this.createTimeColumn = createTimeColumn;
	}

	public String getUpdateTimeColumn() {
		return updateTimeColumn;
	}

	public void setUpdateTimeColumn(String updateTimeColumn) {
		this.updateTimeColumn = updateTimeColumn;
	}

	public String getDeleteColumn() {
		return deleteColumn;
	}

	public void setDeleteColumn(String deleteColumn) {
		this.deleteColumn = deleteColumn;
	}

	public String getVersionColumn() {
		return versionColumn;
	}

	public void setVersionColumn(String versionColumn) {
		this.versionColumn = versionColumn;
	}

	public List<Field> getFieldList() {
		return fieldList;
	}

	public void setFieldList(List<Field> fieldList) {
		this.fieldList = fieldList;
	}

	public LinkedHashMap<String, String> getColumnFieldMapping() {
		return columnFieldMapping;
	}

	public void setColumnFieldMapping(
			LinkedHashMap<String, String> columnFieldMapping) {
		this.columnFieldMapping = columnFieldMapping;
	}

	public LinkedHashMap<String, String> getNormalColumnFieldMapping() {
		return normalColumnFieldMapping;
	}

	public void setNormalColumnFieldMapping(
			LinkedHashMap<String, String> normalColumnFieldMapping) {
		this.normalColumnFieldMapping = normalColumnFieldMapping;
	}

	public String getTableComment() {
		return tableComment;
	}

	public void setTableComment(String tableComment) {
		this.tableComment = tableComment;
	}

	public boolean isCheckTable() {
		return checkTable;
	}

	public void setCheckTable(boolean checkTable) {
		this.checkTable = checkTable;
	}

	public List<TableAutoType> getAutoTypes() {
		return autoTypes;
	}

	public void setAutoTypes(List<TableAutoType> autoTypes) {
		this.autoTypes = autoTypes;
	}
}
