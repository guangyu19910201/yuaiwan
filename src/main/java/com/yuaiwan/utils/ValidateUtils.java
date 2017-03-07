package com.yuaiwan.utils;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 验证有关的工具类
 * @author guangyu
 */
public class ValidateUtils {
	
	/**
	 * 用于判断集合是否为空
	 * @param c
	 * @return
	 */
	public static boolean isBlank(Collection<?> c) {
		return (c == null || c.size() == 0) ? true : false;
	}
	
	/**
	 * 用于判断map是否为空
	 * @param c
	 * @return
	 */
	public static boolean isBlank(Map<?, ?> map){
		return (map==null || map.size() == 0) ? true : false;
	}
	
	/**
	 * 用于验证数组是否为空
	 * @param arr
	 * @return
	 */
	public static boolean isBlank(int[] arr) {
		return arr == null || arr.length == 0;
	}
	
	/**
	 * 用于验证数组是否为空
	 * @param arr
	 * @return
	 */
	public static boolean isBlank(String[] arr) {
		return arr == null || arr.length == 0;
	}
	
	/**
	 * 用于验证一个字符串是否为空
	 * @param str
	 * @return
	 */
	public static boolean isBlank(String str) {
		return str == null || str.length() == 0 || str.trim().length() == 0;
	}
	
	/**
	 * 用于验证一个字符串是否为空或者全空格
	 * @param str
	 * @return
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0;
	}
	
	/**
	 * 检查一个字符串是不是邮箱
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email){
		Pattern pattern = Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}
	
	/**
	 * 检查一个字符串是不是手机号
	 * @param email
	 * @return
	 */
	public static boolean isMobile(String mobile){
		Pattern pattern = Pattern.compile("^1[34578]\\d{9}$");
		Matcher matcher = pattern.matcher(mobile);
		return matcher.matches();
	}
	
	/**
	 * 用于判断map中的某个字段是不是空的
	 * @param map
	 * @param key
	 * @return
	 */
	public static boolean isBlank(Map<?, ?> map,String key){
		if(map==null || map.get(key)==null || isBlank(map.get(key).toString())){
			return true;
		}
		return false;
	}
}
