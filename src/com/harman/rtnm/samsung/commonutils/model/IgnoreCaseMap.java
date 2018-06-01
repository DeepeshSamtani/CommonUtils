package com.harman.rtnm.samsung.commonutils.model;

import java.util.HashMap;

import com.harman.rtnm.samsung.commonutils.util.StringUtils;


/**
 * Wrapper around the {@link HashMap} to handle the 
 * incasesensitivity for the inserted keys.
 *
 * @param <K> String type keys
 * @param <V>
 */
public class IgnoreCaseMap<K, V> extends HashMap<String , V> {

	private static final long serialVersionUID = 8026065232972746597L;

	@Override
	public V put(String key, V value) {
		if(!StringUtils.isNullOrEmpty(key)) {
			key = key.toLowerCase();
		}
		return super.put(key, value);
	}

	@Override
	public V get(Object key) {
		if(key instanceof String && !StringUtils.isNullOrEmpty((String)key)) {
			key = ((String)key).toLowerCase();
		}
		return super.get(key);
	}
}
