package com.yuaiwan.annoparse;

import java.util.HashMap;
import java.util.Map;

/**
 * sql生成器的工厂类
 * @author guangyu
 */
public class SQLGeneratorFactory {
	/**
	 * 用于组装sql,重复时使用的关键字
	 */
	public static final String KEYWORD = "YUAIWAN_";
	
	//SQLGenerator的缓存
	private static Map<String,SQLGenerator<?>> sQLGeneratorCache = new HashMap<String, SQLGenerator<?>>();
	
	public static <T> SQLGenerator<T> initSQLGenerator(Class<T> clazz){
		SQLGenerator<T> generator = getSQLGeneratorByCache(clazz);
		if(generator != null){
			return generator;
		}else{
			return createSQLGenerator(clazz);
		}
	}
	
	private static <T> SQLGenerator<T> createSQLGenerator(Class<T> clazz){
		//构建sql生成器
		SQLGenerator<T> sqlGenerator = new SQLGenerator<T>(SQLNanoParser.getEntityDataMapping(clazz));
		sQLGeneratorCache.put(clazz.getName(), sqlGenerator);
		return sqlGenerator;
	}
	
	/**
	 * 从缓存中获取SQLGenerator
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T> SQLGenerator<T> getSQLGeneratorByCache(Class<T> clazz){
		SQLGenerator<T> generator = (SQLGenerator<T>) sQLGeneratorCache.get(clazz.getName());
		return generator;
	}
}
