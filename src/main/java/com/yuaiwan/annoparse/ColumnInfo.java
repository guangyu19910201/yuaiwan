package com.yuaiwan.annoparse;

import com.yuaiwan.anno.Column;

/**
 * 用户记录数据库表信息的类
 * @author guangyu
 */
public class ColumnInfo {
	@Column(value="Field",dataType="varchar(100)")
	private String field;
	@Column(value="Type",dataType="varchar(100)")
	private String type;
	@Column(value="Comment",dataType="varchar(100)")
	private String comment;
	@Column(value="Default",dataType="varchar(100)")
	private String defaultValue;
	@Column(value="Null",dataType="varchar(100)")
	private String canNull;
	
	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getCanNull() {
		return canNull;
	}

	public void setCanNull(String canNull) {
		this.canNull = canNull;
	}
	
}
