package org.langrid.mlgridservices.util;

import java.util.Map;

public class MapUtil {
	public static <T> T findValueByPrefix(String key, Map<String, T> map){
		if(key == null) key = "";
		T ret = map.get(key);
		if(ret != null) return ret;
		for(var e : map.entrySet()){
			if(e.getKey().toLowerCase().startsWith(key)){
				return e.getValue();
			}
		}
		return null;
	}
	public static <T> T findValueByPartial(String key, Map<String, T> map){
		if(key == null) key = "";
		T ret = map.get(key);
		if(ret != null) return ret;
		for(var e : map.entrySet()){
			if(e.getKey().toLowerCase().contains(key)){
				return e.getValue();
			}
		}
		return null;
	}
}
