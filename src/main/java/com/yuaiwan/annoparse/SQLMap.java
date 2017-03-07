package com.yuaiwan.annoparse;

import java.util.Map;

/**
 * 用于和SQLGenerator配套使用的返回类型
 * @author guangyu
 */
public class SQLMap {
	public SQLMap(SQL sql, Map<String, ?> paramMap) {
		this.sql = sql;
		this.paramMap = paramMap;
	}

	private SQL sql;
	private Map<String, ?> paramMap;

	public SQL getSql() {
		return sql;
	}

	public void setSql(SQL sql) {
		this.sql = sql;
	}

	public Map<String, ?> getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map<String, ?> paramMap) {
		this.paramMap = paramMap;
	}
}
