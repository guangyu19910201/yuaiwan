package com.yuaiwan.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取配置文件属性工具类
 * @author guangyu
 */
public class Config {
	private static Map<String,Properties> propsCache = new HashMap<String, Properties>();
	private static Logger logger = LoggerFactory.getLogger(Config.class);
	private static final String DEFAULT_PATH = "config.properties";
	
	/**
	 * 获取制定路径的配置文件
	 * @param path
	 * @return
	 */
	public static Properties getProperties(String path){
		Properties props = propsCache.get(path); 
		if(props == null){
			try {
				props = new Properties();
				props.load(Config.class.getClassLoader().getResourceAsStream(path));
				propsCache.put(path, props);
			} catch (Exception e) {
				logger.error("加载配置文件失败",e);
			}
		}
		return props;
	}
	
	/**
	 * 获取默认配置属性的配置文件
	 * @return
	 */
	public static Properties getProperties(){
		return getProperties(DEFAULT_PATH);
	}
}
