package com.yuaiwan.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanUtils {
	/**
	 * ObjectTOmap
	 * @param obj
	 * @return
	 */
	public static Map<String, Object> objectToMap(Object obj){
		try {
			if (obj == null)
				return null;
			Map<String, Object> map = new HashMap<String, Object>();
			BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
			PropertyDescriptor[] propertyDescriptors = beanInfo
					.getPropertyDescriptors();
			for (PropertyDescriptor property : propertyDescriptors) {
				String key = property.getName();
				if (key.compareToIgnoreCase("class") == 0) {
					continue;
				}
				Method getter = property.getReadMethod();
				Object value = getter != null ? getter.invoke(obj) : "";
				map.put(key, value);
			}
			return map;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static <T> List<T> asList(T[] arr){
		ArrayList<T> result = new ArrayList<T>();
		if(arr!=null && arr.length>0){
			for (T t : arr) {
				result.add(t);
			}
		}
		return result;
	}
}
