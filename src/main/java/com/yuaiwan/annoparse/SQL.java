package com.yuaiwan.annoparse;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yuaiwan.utils.StringUtils;
import com.yuaiwan.utils.ValidateUtils;

public class SQL {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	public enum StatementType {
		DELETE, INSERT, SELECT, UPDATE
	}
	private StatementType statementType;
	private List<String> sets = new ArrayList<String>();
	private List<String> select = new ArrayList<String>();
	private List<String> tables = new ArrayList<String>();
	private List<String> where = new ArrayList<String>();
	private List<String> having = new ArrayList<String>();
	private List<String> groupBy = new ArrayList<String>();
	private List<String> orderBy = new ArrayList<String>();
	private List<String> columns = new ArrayList<String>();
	private List<String> values = new ArrayList<String>();
	private String limit;
	
	public SQL LIMIT(int size){
		limit = " limit "+size;
		return this;
	}

	public SQL LIMIT(int start,int size){
		if(size > 0){
			if(start > 0){
				limit = " limit "+start+","+size;
			}else{
				limit = " limit "+size;
			}
		}
		return this;
	}
	
	public SQL UPDATE(String tableName) {
		statementType = StatementType.UPDATE;
		tables.add(tableName);
		return this;
	}

	public SQL SET(String set) {
		sets.add(set);
		return this;
	}

	public SQL INSERT_INTO(String tableName) {
		statementType = StatementType.INSERT;
		tables.add(tableName);
		return this;
	}

	public SQL VALUES(String column, String value) {
		columns.add(column);
		values.add(value);
		return this;
	}

	public SQL SELECT(String columns) {
		statementType = StatementType.SELECT;
		select.add(columns);
		return this;
	}

	public SQL DELETE_FROM(String tableName) {
		statementType = StatementType.DELETE;
		tables.add(tableName);
		return this;
	}

	public SQL FROM(String table) {
		tables.add(table);
		return this;
	}

	public SQL WHERE(String conditions) {
		where.add(conditions);
		return this;
	}

	public SQL GROUP_BY(String columns) {
		groupBy.add(columns);
		return this;
	}

	public SQL HAVING(String conditions) {
		having.add(conditions);
		return this;
	}

	public SQL ORDER_BY(String columns) {
		orderBy.add(columns);
		return this;
	}

	private void sqlClause(StringBuilder sb,String keyword, List<String> parts, String separator, String open, String close){
		if(!parts.isEmpty()){
			sb.append(keyword).append(" ").append(open).append(StringUtils.join(parts, separator)).append(close);
		}
	}
	
	private String selectSQL(StringBuilder sb) {
		sqlClause(sb, "SELECT", select, ", ", "", "");
		sqlClause(sb, " FROM", tables, ", ", "","");
		sqlClause(sb, " WHERE", where," AND ", "(", ")");
		sqlClause(sb, " GROUP BY", groupBy,", ", "", "");
		sqlClause(sb, " HAVING", having," AND ", "(", ")");
		sqlClause(sb, " ORDER BY", orderBy,", ", "", "");
		if(!ValidateUtils.isBlank(limit)){
			sb.append(limit);
		}
		return sb.toString();
	}

	private String insertSQL(StringBuilder sb) {
		sqlClause(sb, "INSERT INTO", tables, "", "", "");
		sqlClause(sb, "", columns,", ", "(", ")");
		sqlClause(sb, " VALUES", values, ", ", "(", ")");
		return sb.toString();
	}

	private String deleteSQL(StringBuilder sb) {
		sqlClause(sb, "DELETE FROM", tables, "", "", "");
		sqlClause(sb, " WHERE", where, " AND ", "(", ")");
		return sb.toString();
	}

	private String updateSQL(StringBuilder sb) {
		sqlClause(sb, "UPDATE", tables, "", "", "");
		sqlClause(sb, " SET", sets, ", ", "", "");
		sqlClause(sb, " WHERE", where, " AND ", "(", ")");
		return sb.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (statementType == null) {
			return null;
		}
		String answer;
		switch (statementType) {
			case DELETE:
				answer = deleteSQL(sb);
				break;
			case INSERT:
				answer = insertSQL(sb);
				break;
			case SELECT:
				answer = selectSQL(sb);
				break;
			case UPDATE:
				answer = updateSQL(sb);
				break;
			default:
				answer = null;
		}
		logger.debug("sql:"+answer);
		return answer;
	}
}
