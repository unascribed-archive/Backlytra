package com.unascribed.backlytra;

import java.util.Map;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;

public class FieldImitations {
	private static Map<Object, Map<String, Object>> imitations = new MapMaker().weakKeys().concurrencyLevel(1).makeMap();
	
	public static <T, U> T get(U subject, String field, T def) {
		if (!imitations.containsKey(subject)) {
			imitations.put(subject, Maps.newHashMap());
		}
		Map<String, Object> map = imitations.get(subject);
		if (map.containsKey(field) || def == null) {
			return (T) map.get(field);
		} else {
			return def;
		}
	}
	
	public static <T, U> T set(U subject, String field, T value) {
		if (!imitations.containsKey(subject)) {
			imitations.put(subject, Maps.newHashMap());
		}
		Map<String, Object> map = imitations.get(subject);
		map.put(field, value);
		return value;
	}
}
