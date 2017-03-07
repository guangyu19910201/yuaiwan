package com.yuaiwan.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 反射的一些工具类
 * @author guangyu
 */
public class ReflectUtils {
	private static Logger logger = LoggerFactory.getLogger(ReflectUtils.class);
	/**
	 * 用于缓存字段的map(<类全名,字段>)
	 * 该对象不需要设置同步,因为同一class的field相同
	 */
	private static Map<String,Field> fieldCacheMap = new HashMap<String, Field>();
	
	public static Object getFieldValue(Object object,String fieldName){
		try {
			Field field = getField(object.getClass(), fieldName);
			return field.get(object);
		} catch (Exception e) {
			logger.error("getFieldValue in "+object.getClass().getName()+" fail",e);
			throw new RuntimeException(e);
		}
	}
	
	public static void setFieldValue(Object object,String fieldName,Object value){
		try {
			Field field = getField(object.getClass(), fieldName);
			field.set(object, value);
		} catch (Exception e) {
			logger.error("setFieldValue in "+object.getClass().getName()+" fail",e);
			throw new RuntimeException(e);
		}
	}
	
	public static Field getField(Class<?> clazz,String fieldName) throws Exception{
		String className = clazz.getName();
		String key = className+"_"+fieldName;
		Field field = fieldCacheMap.get(key);
		if(field == null){
			field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			fieldCacheMap.put(key, field);
		}
		return field;
	}
}
