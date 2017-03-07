package com.yuaiwan.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTools {
	public static final SimpleDateFormat dateSecondFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	public static String getTime() {
		Date date = new Date();
		return dateSecondFormat.format(date);
	}
	
	// 计算两个日期相隔的天数
	public static int nDaysBetweenTwoDate(String firstString,
			String secondString) {
		Date firstDate = null;
		Date secondDate = null;
		try {
			firstDate = dateFormat.parse(firstString);
			secondDate = dateFormat.parse(secondString);
			int nDay = (int) ((secondDate.getTime() - firstDate.getTime()) / (24 * 60 * 60 * 1000));
			return nDay;
		} catch (Exception e) {
			// 日期型字符串格式错误
			return 0;
		}
	}
	
	/**
	 * 取得字符串格式的当前日期
	 * @return
	 */
	public static String getDate() {
		Date date = new Date();
		return dateFormat.format(date);
	}
}
