package com.harman.rtnm.samsung.commonutils.model;

import java.util.HashMap;
import java.util.Map;

public class TreeNode {

	private Long recordIndex;
	private String recordKey = "";
	private String parentNodeKey = "";
	
	private Map<String, Long> mapChildrenIndexes = new HashMap<String, Long>();


	public TreeNode(String key, Long index) {
		this.recordKey = key;
		this.recordIndex = index;
	}

	/**
	 * Get the key for 
	 * @return
	 */
	public String getRecordKey() {
		return recordKey;
	}
	
	/**
	 * 
	 * @return
	 */
	public Long getByteOffset() {		
		return recordIndex;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getParentKey() {		
		return parentNodeKey;		
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String, Long> getChildrenMap() {
		return mapChildrenIndexes;
	}
	
	/**
	 * 
	 * @param childNodeKey
	 * @return
	 */
	public Long getChildIndex(String childNodeKey) {
		return mapChildrenIndexes.get(childNodeKey);
	}
}
