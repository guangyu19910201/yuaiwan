package com.yuaiwan.resultset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;


public class ResultSetForMap implements ResultSetExtractor<Map<String,Object>>{
	private final int LIST_FLAG = 1;
	private final int MAP_FLAG = 2;
	private List<String> fieldList;
	private Map<String,String> fieldMapList;
	private int flag = 0;
	
	public ResultSetForMap(List<String> fieldList) {
		this.fieldList = fieldList;
		flag = LIST_FLAG;
	}
	
	public ResultSetForMap(Map<String,String> fieldMapList) {
		this.fieldMapList = fieldMapList;
		flag = MAP_FLAG;
	}

	@Override
	public Map<String, Object> extractData(ResultSet rs) throws SQLException,DataAccessException {
		if(rs.next()){
			Map<String, Object> result = new HashMap<String, Object>();
			if(flag == LIST_FLAG){
				for (String fieldName : fieldList) {
					result.put(fieldName, rs.getObject(fieldName));
				}
			}if(flag == MAP_FLAG){
				for (Map.Entry<String, String> entry : fieldMapList.entrySet()) {
					result.put(entry.getKey(), rs.getObject(entry.getValue()));
				}
			}
			return result;
		}else{
			return null;
		}
	}
}
