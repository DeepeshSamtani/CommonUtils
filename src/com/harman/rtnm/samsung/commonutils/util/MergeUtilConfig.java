package com.harman.rtnm.samsung.commonutils.util;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.harman.rtnm.samsung.commonutils.util.IOUtils;

public final class MergeUtilConfig {

	private static final String KEY_FILE_PATH_INPUT = "PATH_INPUT";
	private static final String KEY_FILE_PATH_OUTPUT = "PATH_OUTPUT";
	private static final String KEY_OUTPUT_HEADER = "OUTPUT_HEADER";
	private static final String KEY_OUTPUT_HEADER_DELIMITER = "HEADER_DELIMITER";
	
	private Map<String, List<Long>>  indexHashMap = null;

	private String pathInputFile = StringUtils.EMPTY_BLANK_STRING;
	private String pathOuputFile = StringUtils.EMPTY_BLANK_STRING;
	private String headerOutput = StringUtils.EMPTY_BLANK_STRING;
	private String headerDelimiter = StringUtils.SYMBOL_COMMA;
	
	public MergeUtilConfig(Map<String, List<Long>> indexHasMap, String configPath) {
		this(indexHasMap, configPath, false);
	}
	
	public MergeUtilConfig(Map<String, List<Long>> indexHasMap, String configPath, boolean classpathConfig) {
		try {
			ConfigUtil.loadProperties(IOUtils.getFile(configPath, classpathConfig), new Properties());			
		} catch(Exception e) {
			//ignore
		}
		
		setIndexHashMap(indexHasMap);
	}
	


	/**
	 * @param pathInputFile the pathInputFile to set
	 */
	public void setPathInputFile(String pathInputFile) {
		this.pathInputFile = pathInputFile;
	}

	/**
	 * @param pathOuputFile the pathOuputFile to set
	 */
	public void setPathOuputFile(String pathOuputFile) {
		this.pathOuputFile = pathOuputFile;
	}

	/**
	 * @param headerOutput the headerOutput to set
	 */
	public void setHeaderOutput(String headerOutput) {
		this.headerOutput = headerOutput;
	}

	/**
	 * @param headerDelimiter the headerDelimiter to set
	 */
	public void setHeaderDelimiter(String headerDelimiter) {
		this.headerDelimiter = headerDelimiter;
	}

	public String getInputFilePath() {
		
		if(StringUtils.isNullOrEmpty(pathInputFile)) {
			return ConfigUtil.getpropertyValue(KEY_FILE_PATH_INPUT);
		}
		
		return pathInputFile;
	}

	
	public String getOuptutFilePath() {
		if(StringUtils.isNullOrEmpty(pathOuputFile)) {
			return ConfigUtil.getpropertyValue(KEY_FILE_PATH_OUTPUT);
		}
		return pathOuputFile;
	}

	public String getHeaderDelimiter() {
		if(StringUtils.isNullOrEmpty(headerDelimiter)) {
			return ConfigUtil.getpropertyValue(KEY_OUTPUT_HEADER_DELIMITER);
		}
		return headerDelimiter;
	}

	public Map<String, List<Long>> getIndexHashMap() {
		return indexHashMap;
	}
	
	public String getOutputHeader() {
		if(StringUtils.isNullOrEmpty(this.headerOutput)) {
			return ConfigUtil.getpropertyValue(KEY_OUTPUT_HEADER); 
		}
		return this.headerOutput;
	}

	private void setIndexHashMap(Map<String, List<Long>> indexHashMap) {
		this.indexHashMap = indexHashMap;
	}
	
}
