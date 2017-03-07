package com.yuaiwan.annoparse;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.yuaiwan.anno.Column;

/**
 * ResultMap功能注解解析器,用于自定义结果集的返回
 * 该解析器只用于解析出数据库字段和对应的实体类字段,没有SQLNanoParser解析器要求的严格,不去校验数据注解的各种属性的完整性和支持的字段类型
 * @author guangyu
 */
public class ResultMapNanoParse {
	/**
	 * 数据库表名和字段名的缓存
	 */
	private static Map<String,Map<String,String>> columnFieldMappingCache = new HashMap<String, Map<String,String>>();
	
	/**
	 * 获取class类中对应的字段名和数据列名
	 * @param clazz
	 * @return
	 */
	public static Map<String,String> getColumnFieldMapping(Class<?> clazz){
		String className = clazz.getName();
		Map<String,String> columnFieldMapping = columnFieldMappingCache.get(className);
		if(columnFieldMapping == null){
			columnFieldMapping = parse(clazz);
		}
		return columnFieldMapping;
	}
	
	/**
	 * 对class进行解析
	 * @param clazz
	 * @return
	 */
	private synchronized static Map<String,String> parse(Class<?> clazz){
		Map<String,String> columnFieldMapping = new HashMap<String, String>();
		//获取字段
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			Column column = field.getAnnotation(Column.class);
			if(column!=null){
				String fieldName = field.getName();
				String columnName = SQLNanoParser.getColumnName(fieldName, column);
				columnFieldMapping.put(columnName, fieldName);
			}
		}
		columnFieldMappingCache.put(clazz.getName(), columnFieldMapping);//放入缓存
		return columnFieldMapping;
	}
}
