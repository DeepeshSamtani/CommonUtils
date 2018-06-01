package com.harman.rtnm.samsung.commonutils.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.harman.rtnm.samsung.commonutils.util.IOUtils;
import com.harman.rtnm.samsung.commonutils.util.StringUtils;

/**
 * ResourceLoader caches the configuration or properties' values   
 *
 */
public final class ResourceLoader {
	
	private static final Logger LOGGER = Logger.getLogger(ResourceLoader.class);

	private static Map<String, String> inventoryFilesMap = new HashMap<String, String>();

	/**
	 * To retrieve the vale of the property mapped to the provided key, if any
	 * @param resourceKey : Key to the property
	 * @return
	 */
	public static String getResourceValue(String resourceKey) {
		return inventoryFilesMap.get(resourceKey);
	}


	/**
	 * Loads all the inventory source from the config file and stores it locally
	 * To optimize the memory use, it assumes the key to property as the value mentioned after (dot) '.' operator<br>
	 * Example : the key to following property would be UKCORE, value after dot symbol<br>
	 * INVENTORY_FEED_SOURCE.UKCORE=autosavefile.tsv 
	 * @throws Exception 
	 */
	public static void loadConfigurationInfo(String configFilePath) throws Exception {
		LOGGER.info("Start loading inventory configutation");
		Map<String, String> map = IOUtils.getPropertiesMap(configFilePath);
		Iterator<String> keyIterator = map.keySet().iterator();

		while(keyIterator.hasNext()) {
			String oldKey = keyIterator.next();					
			String newKey = StringUtils.trimIt(oldKey.substring((oldKey.indexOf(StringUtils.SYMBOL_DOT)+1), oldKey.length()));

			inventoryFilesMap.put(newKey, StringUtils.trimIt(map.get(oldKey)));				
		}
		LOGGER.info("Loaded inventory configutation successfully");
	}
}
