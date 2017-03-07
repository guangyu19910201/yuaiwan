package com.yuaiwan.utils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	
	public static String join(List<?> list, String separator){
		if(!ValidateUtils.isBlank(list)){
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < list.size(); i++) {
				if(i > 0){
					sb.append(separator);
				}
				sb.append(list.get(i));
			}
			return sb.toString();
		}else{
			return "";
		}
	}
	
	public static String getMobile(String content){
		if(content == null || content.length() == 0){return "";}
		Pattern pattern = Pattern.compile("(?<!\\d)(?:(?:1[358]\\d{9})|(?:861[34578]\\d{9}))(?!\\d)"); 
		Matcher matcher = pattern.matcher(content); 
		StringBuffer bf = new StringBuffer(64);
		while (matcher.find()) {
			bf.append(matcher.group()).append(",");
		}
		int len = bf.length();
		if (len > 0) {
			bf.deleteCharAt(len - 1);
		}
		return bf.toString();
	}
}
