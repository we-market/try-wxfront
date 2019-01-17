package cn.wemarket.wxfront.common.util;


import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class CommonUtil {
	
	public static List<Long> strArrToLong(String[] arr){
		List<Long> list = new ArrayList<>();
		for (String str : arr) {
			if (!StringUtils.isEmpty(str)) {
				list.add(Long.valueOf(str));
			}
		}
		return list;
	}
	
	public static <T> String listToString(List<T> list){
		if (list == null) {
			return null;
		}
		return StringUtils.join(list.iterator(), ",");
	}
	
	public static Object getValueByKey(Object obj,String[] key) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		Object src = obj;
		for (int i = 1; i < key.length; i++) {
			src = getValueByKey(src.getClass(),src, key[i]);
		}
		return src;
	}
	
	public static Object getValueByKey(Class<?> _class, Object obj, String key)
			throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
		try {
			Field f = _class.getDeclaredField(key);
			f.setAccessible(true);
			return f.get(obj);
		} catch (NoSuchFieldException e) {
			Class<?> tempClazz = _class.getSuperclass();
			if (tempClazz != null) {
				return getValueByKey(tempClazz, obj, key);
			} else {
				throw e;
			}
		}
	}	
	

}
