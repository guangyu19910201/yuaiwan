package com.yuaiwan.resultset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.yuaiwan.annoparse.ResultMapNanoParse;
import com.yuaiwan.utils.ReflectUtils;

public class ResultMap<T> implements ResultSetExtractor<T>{
	Logger logger = LoggerFactory.getLogger(this.getClass());
	private Map<String,String> columnFieldMap;
	private Class<T> clazz;
	
	public ResultMap(Class<T> clazz) {
		this.clazz = clazz;
		columnFieldMap = ResultMapNanoParse.getColumnFieldMapping(clazz);
	}
	
	@Override
	public T extractData(ResultSet rs) throws SQLException, DataAccessException {
		if(rs.next()){
			try {
				T result = clazz.newInstance();
				for (Map.Entry<String, String> entry : columnFieldMap.entrySet()) {
					String columnName = entry.getKey();
					String fieldName = entry.getValue();
					ReflectUtils.setFieldValue(result, fieldName, rs.getObject(columnName));
				}
				return result;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}else{
			return null;
		}
	}

}
