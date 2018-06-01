package com.harman.rtnm.samsung.commonutils.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.harman.rtnm.samsung.commonutils.parser.csv.IParser;
import com.harman.rtnm.samsung.commonutils.parser.csv.NpmFileParser;
import com.harman.rtnm.samsung.commonutils.parser.csv.NpmMultiFileParser;
import com.harman.rtnm.samsung.commonutils.writer.IWriter;
import com.harman.rtnm.samsung.commonutils.writer.NpmFileWriter;


/**
 * IOUtils is designed to handle common IO operations. It has various utility methods. 
 *
 */
public final class IOUtils {

	private static final String COMMAND_UNCOMPRESS = "uncompress ";
	private static ClassLoader classLoader;
	private static boolean reverseKeyValueOrder = false;

	/**
	 * Return the file path for the given file path 
	 * @param filePath : Absolute path to the file
	 * @return
	 * @throws IOException 
	 */
	public static File getFile(final String filePath) throws IOException {
		return getFile(filePath, false);
	}

	/**
	 * Return the file path for the given file name.
	 * @param fileName
	 * @return File object for the give abstract file path
	 * @throws IOException 
	 */
	public static File getFile(final String fileName, boolean isFromClasspath) throws IOException {		

		String path = fileName;
		if(isFromClasspath) {
			path =	getClassLoader().getResource(fileName).getFile();
		}
		File file = new File(path);
		if(!file.exists()){			
			String dirPath = path.substring(0, path.lastIndexOf(StringUtils.DELIMITER_SYMBOL_FILE_SEPARATOR)+1);
			File dir = new File(dirPath);
			if(! dir.exists()) {
				dir.mkdirs();
			}

			file.createNewFile();
		}

		return new File(path);

	}

	/**
	 * Set the provided read permission for a given file param
	 * @param file
	 * @param readable
	 * @param ownerReadableOnly
	 */
	public static void setReadPermissions(File file, boolean readable, boolean ownerReadableOnly) throws SecurityException{
		if(file == null) {
			throw new NullPointerException("Cannot set read permissions for : " + file);
		}

		file.setReadable(readable,ownerReadableOnly);
	}

	/**
	 * Set the provided write permission for a given file param
	 * @param file
	 * @param writable
	 * @param ownerWritableOnly
	 */
	public static void setWritePermissions(File file, boolean writable, boolean ownerWritableOnly) throws SecurityException {
		if(file == null) {
			throw new NullPointerException(" Cannon set write permissions for : " + file);
		}

		file.setWritable(writable, ownerWritableOnly);
	}

	/**
	 * Uses the uncompress unix utility to decompress the specified file
	 * @param path
	 * @return
	 */
	public boolean decompressUnixFile(String path) {
		return executeSystemCommand(COMMAND_UNCOMPRESS+path);
	}

	/**
	 * Executes the specified command as a different process
	 * @param path
	 */
	private boolean executeSystemCommand(String command) {
		Process uncompressCommand = null;
		try {
			uncompressCommand = Runtime.getRuntime().exec(command);
			uncompressCommand.waitFor();
		} catch (IOException e) {
			return false;
		} catch (InterruptedException e) {
			return false;
		}
		return (uncompressCommand.exitValue() == 0);
	}

	/**
	 * Utility method to serialize and object
	 * @param obj
	 * @param file
	 * @throws IOException
	 */
	public static void marshalingObject(Object obj, File file) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(file);
		ObjectOutputStream objOut = new ObjectOutputStream(fileOut);

		objOut.writeObject(obj);
		objOut.flush();

		IOUtils.close(fileOut);
		IOUtils.close(objOut);
	}

	/**
	 * Utility method to serialize and object
	 * @param obj
	 * @param file
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public static Object unmarshalingObject(File file) throws IOException, ClassNotFoundException {
		Object obj = null;

		FileInputStream fileIn = new FileInputStream(file);
		ObjectInputStream objin = new ObjectInputStream(fileIn);

		obj = objin.readObject();		

		IOUtils.close(fileIn);
		IOUtils.close(objin);

		return obj;

	}

	/**
	 * Returns the list of file paths at the provided directory path.
	 * @param directoryPath
	 * @return list of files path
	 * @throws IOException
	 */
	public static List<String> getListOfFilesPathAtDirectory(String directoryPath) throws IOException {
		List<String> filesPathList = new ArrayList<String>();

		File file = getFile(directoryPath);

		for(String filename : file.list()) {
			String filePath = directoryPath+File.separator+filename;
			if(getFile(filePath).isFile()) {
				filesPathList.add(filePath);
			}
		}

		return filesPathList;
	}

	/**
	 * Returns the list of file paths at the provided directory as file object.
	 * @param directoryPath
	 * @return list of files path
	 * @throws IOException
	 */
	public static List<String> getListOfFilesPathAtDirectory(File directory) throws IOException {
		List<String> filesPathList = new ArrayList<String>();

		String directoryPath = directory.getAbsolutePath();

		for(String filename : directory.list()) {
			String filePath = directoryPath+File.separator+filename;
			if(getFile(filePath).isFile()) {
				filesPathList.add(filePath);
			}
		}

		return filesPathList;
	}



	/**
	 * Retrieves all the properties stored in the <b>file</b> as key,value pair.
	 * @param file : File(object) contaning the properties
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, String> getPropertiesMap(File file, String propertyDelimiter) throws Exception {
		return getPropertiesMap(new NpmFileParser(file, propertyDelimiter), new int[]{0}, 1);
	}

	public static Map<String, String> getPropertiesMap(String file, String propertyDelimiter, int[] keyColumnIndex, int valueColumnIndex, boolean fromDir) throws Exception {		
		return getPropertiesMap(new NpmMultiFileParser(file, propertyDelimiter), keyColumnIndex, valueColumnIndex);
	}


	public static Map<String, String> getPropertiesMap(File file, String propertyDelimiter, int[] keyColumnIndex, int valueColumnIndex, boolean fromDir) throws Exception {		
		return getPropertiesMap(new NpmMultiFileParser(file, propertyDelimiter), keyColumnIndex, valueColumnIndex);
	}

	public static Map<String, String> getPropertiesMap(String filePath, String propertyDelimiter, int[] keyColumnIndex, int valueColumnIndex) throws Exception {
		return getPropertiesMap(new NpmFileParser(filePath, propertyDelimiter), keyColumnIndex, valueColumnIndex);
	}

	public static Map<String, String> getPropertiesMap(IParser parser, int[] keyColumnIndex, int valueColumnIndex) throws Exception {
		Map<String, String> propertiesMap = new HashMap<String, String>();

		Iterator<String[]> iterator = parser.getAllRows().iterator();
		while(iterator.hasNext()) {
			String propertyKey = null;
			String propertyValue = null;
			String[] propertyRow = iterator.next();
			if(propertyRow.length < (valueColumnIndex+1)) { // only key-value pair has to be stored
				continue;
			}

			StringBuilder key = new StringBuilder();
			for(int i : keyColumnIndex) {
				key.append(propertyRow[i]);
			}

			if(reverseKeyValueOrder) {
				propertyKey = propertyRow[valueColumnIndex];
				propertyValue = key.toString();
			} else {
				propertyKey = key.toString();
				propertyValue = propertyRow[valueColumnIndex];
			}
			propertiesMap.put(StringUtils.trimIt(propertyKey), StringUtils.trimIt(propertyValue));
		}

		close(parser);
		return propertiesMap;
	}

	/**
	 * Retrieve the row as value and key as the specific column 
	 * @param keyColumnIndex the column value considered as key in the returned map
	 * @param filePath
	 * @param delimiter
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String[]> getPropertiesRowMapWithKey(int keyColumnIndex, String filePath, String delimiter) throws Exception {
		return getPropertiesRowMapWithMultiColKey(new int[]{keyColumnIndex}, filePath, delimiter);
	}

	/**
	 * Retrieve the row as value and with key specified as the combination of multiple
	 * columns specified as keyColumnIndex array.<br><br>
	 * <b>NOTE:</b> This is method is expecting the key column index to be defined in increasing order only
	 * @param keyColumnIndex the columns' combined value considered as key in the returned map
	 * @param filePath
	 * @param delimiter
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String[]> getPropertiesRowMapWithMultiColKey(int[] keyColumnIndex, String filePath, String delimiter) throws Exception {
		Map<String, String[]> map = new HashMap<String, String[]>();
		StringBuilder key = new StringBuilder();

		NpmFileParser parser = new NpmFileParser(filePath, delimiter, true);
		Iterator<String[]> iterator = parser.getAllRows().iterator();

		while(iterator.hasNext()) {
			key.setLength(0);
			String[] propertyRow = iterator.next();
			if(propertyRow.length < keyColumnIndex[keyColumnIndex.length - 1]) { // only key-value pair has to be stored
				continue;
			}			

			for(int i : keyColumnIndex) {
				key.append(propertyRow[i]);
			}
			map.put(key.toString(), propertyRow);
		}

		close(parser);
		return map;
	}

	/**
	 * Retrieves all the properties stored at the <b>filePath</b> as key,value pair.
	 * @param filePath
	 * @param propertyDelimiter : delimiter by which the the key,value is separated in the properties file.
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, String> getPropertiesMap(String filePath, String propertyDelimiter) throws Exception {
		Map<String, String> propertiesMap = new HashMap<String, String>();		
		File propertiesFile = getFile(filePath, true);
		propertiesMap = getPropertiesMap(propertiesFile, propertyDelimiter);
		return propertiesMap;
	}

	public static Map<String, String> getPropertiesMapInReverseOrder(String filePath, String propertyDelimiter) throws Exception {
		Map<String, String> propertiesMap = new HashMap<String, String>();		
		File propertiesFile = getFile(filePath, false);
		reverseKeyValueOrder = true;
		propertiesMap = getPropertiesMap(propertiesFile, propertyDelimiter);
		reverseKeyValueOrder = false;
		return propertiesMap;
	}

	public static Map<String, ArrayList<String>> getPropertiesMapWithDupesInReverseOrder(String filePath, String propertyDelimiter) throws IOException {
		Map<String, ArrayList<String>> propertiesMap = new HashMap<String, ArrayList<String>>();		
		File propertiesFile = getFile(filePath, false);
		reverseKeyValueOrder = true;
		propertiesMap = getPropertiesMapWithDupes(propertiesFile, propertyDelimiter);
		reverseKeyValueOrder = false;
		return propertiesMap;
	}

	public static Map<String, ArrayList<String>> getPropertiesMapWithDupes(File file, String propertyDelimiter) throws IOException {
		Map<String, ArrayList<String>> propertiesMap = new HashMap<String, ArrayList<String>>();
		NpmFileParser parser = new NpmFileParser(file, propertyDelimiter);
		Iterator<String[]> iterator = parser.getAllRows().iterator();

		while(iterator.hasNext()) {
			ArrayList<String> values = new ArrayList<String>();

			String propertyKey = null;
			String propertyValue = null;
			String[] propertyRow = iterator.next();
			if(propertyRow.length < 2) { // only key-value pair has to be stored
				continue;
			}
			if(reverseKeyValueOrder) {
				propertyKey = StringUtils.trimIt(propertyRow[1]);
				propertyValue = StringUtils.trimIt(propertyRow[0]);
			} else {
				propertyKey = StringUtils.trimIt(propertyRow[0]);
				propertyValue = StringUtils.trimIt(propertyRow[1]);
			}

			if(propertiesMap.containsKey(propertyKey)) {
				propertiesMap.get(propertyKey).add(propertyValue);
			} else {
				values.add(propertyValue);
				propertiesMap.put(propertyKey, values);
			}			
		}
		close(parser);
		return propertiesMap;
	}


	/**
	 * Retrieves all the properties stored at the <b>filePath</b> as key,value pair.
	 * @param filePath : Path to the properties file
	 * @return
	 * @throws Exception 
	 */
	public static Map<String, String> getPropertiesMap(String filePath) throws Exception {

		return getPropertiesMap(filePath, StringUtils.SYMBOL_EQUAL);
	}

	/**
	 * Close the parser and its channels
	 * @param parser
	 */
	public static void close(IParser parser) {
		try {
			if(parser != null) {
				parser.close();
				parser = null;
			}
		} catch(Exception e) {

		}
	}

	public static void close(Reader reader) {
		try {
			if(reader != null) {
				reader.close();
				reader = null;
			}
		} catch(Exception e) {

		}
	}


	/**
	 * Close the parser and its channels
	 * @param writer
	 */
	public static void close(IWriter writer) {
		try {
			if(writer != null) {
				writer.close();
				writer = null;
			}
		} catch(Exception e) {
		}
	}

	/**
	 * Closing an InputStream
	 * @param is
	 */
	public static void close(InputStream is) {
		try {
			if(is != null) {
				is.close();
				is = null;
			}
		} catch (IOException e) {
		}
	}

	/**
	 * Closing an OutputStream
	 * @param out
	 */
	public static void close(OutputStream out) {
		try {
			if(out != null) {
				out.close();
				out = null;
			}
		} catch (IOException e) {
		}
	}

	/**
	 * loading the current thread's class loader for loading file in classpath 
	 * @return
	 */
	public static ClassLoader getClassLoader() {
		return (classLoader = Thread.currentThread().getContextClassLoader());

	}

	/**
	 * Delete the specified file
	 * @param path
	 * @throws IOException 
	 */
	public static void deleteFile(String path) throws IOException {
		deleteFile(getFile(path));
	}

	/**
	 * Delete the specified file
	 * @param file
	 */
	public static void deleteFile(File file) {
		file.delete();
	}

	public static Collection<File> getFilesInChronologicalOrder(String dirPath, String filenameContains, final boolean desc) throws IOException {
		return getFilesInChronologicalOrder(getFile(dirPath), filenameContains, desc);
	}
	
	public static Collection<File> getFilesInChronologicalOrder(File dirPath, String filenameContains, final boolean desc) throws IOException {
		
		List<File> listOfFiles = new ArrayList<File>();
		
		File[] files = dirPath.listFiles();
		
		for(int index = 0; index < files.length; index++) {
			if(StringUtils.isNullOrEmpty(filenameContains) || files[index].getName().contains(filenameContains)) {
				listOfFiles.add(files[index]);
			}						
		}
		
		Comparator<File> comparator = new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				if(desc) {
					return (int)(o2.lastModified() - o1.lastModified());
				} else {
					return (int)(o1.lastModified() - o2.lastModified());
				}
			}			
		};
		
		Collections.sort(listOfFiles, comparator);

		return listOfFiles;
	}
	
	
	public static File getMostRecentFile(String dirPath, String filenameContains) throws IOException {
		return getMostRecentFile(getFile(dirPath), filenameContains);
	}

	public static File getMostRecentFile(File dirPath, String filenameContains) throws IOException {

		File[] files = dirPath.listFiles();
		File recentFile = null; 

		for(int index = 0; index < files.length; index++) {
			if(StringUtils.isNullOrEmpty(filenameContains) || files[index].getName().contains(filenameContains)) {
				if(recentFile == null || recentFile.lastModified() <= files[index].lastModified()) {
					recentFile = files[index];
				}
			}						
		}

		return recentFile;
	}


	/**
	 * Utility method to get an array containing all the files, which were generated today after midnight
	 * @param dirPath folder path
	 * @return
	 * @throws IOException 
	 */
	public static File[] getFilesForCurrentDay(String dirPath) throws IOException  {
		return getFilesForCurrentDay(getFile(dirPath), StringUtils.EMPTY_BLANK_STRING).toArray(new File[1]);
	}


	/**
	 * Utility method to get an array containing all the files, which were generated today after midnight
	 * @param dirPath folder path
	 * @return
	 */
	public static File[] getFilesForCurrentDay(File dirPath)  {
		return getFilesForCurrentDay(dirPath, StringUtils.EMPTY_BLANK_STRING).toArray(new File[1]);
	}

	/**
	 * Utility method to get an array containing all the files, which were generated today after midnight
	 * @param dirPath folder path
	 * @param filenameContains file name pattern, not applied if {@link StringUtils}.isNullOrEmpty(filenameContains) returns true
	 * @return
	 */
	public static File[] getFilesForCurrentDay(File dirPath, String fileNameContains, boolean returnArrayFile)  {
		return getFilesForCurrentDay(dirPath, StringUtils.EMPTY_BLANK_STRING).toArray(new File[1]);
	}

	/**
	 * Renaming the existing file to have new path name
	 * @param oldName - existing name 
	 * @param newName - new file name
	 * @return
	 */
	public static boolean renameFilePath(String oldName, String newName) {
		try {
			File file = getFile(oldName);
			file.renameTo(getFile(newName));
		} catch(IOException ex) {
			return false;
		}
		
		return true;
	}


	/**
	 * Utility method to get an array containing all the files, which were generated today after midnight
	 * @param dirPath folder
	 * @param filenameContains file name pattern, not applied if {@link StringUtils}.isNullOrEmpty(filenameContains) returns true
	 * @return
	 */
	public static List<File> getFilesForCurrentDay(File dirPath, String filenameContains) {
		File[] files = dirPath.listFiles();
		ArrayList<File> currentDayFiles = new ArrayList<File>();

		String currentDate = DateUtils.format(DateUtils.DATE_FORMAT_ddMyyyy) + DateUtils.TIME_MIDNIGHT_hhmmss;				
		long currentDateTime = DateUtils.parseDate(currentDate, DateUtils.DATE_FORMAT_ddMyyyyhhmmss).getTime();				

		for(int index = 0; index < files.length; index++) {
			if((StringUtils.isNullOrEmpty(filenameContains) || files[index].getName().contains(filenameContains)) 
					&& files[index].lastModified() > currentDateTime) {				
				currentDayFiles.add(files[index]);
			}						
		}

		return currentDayFiles;		
	}

	/**
	 * Retrieves all the properties key stored at the <b>filePath</b> as key Set.
	 * @param file
	 * @param propertyDelimiter : delimiter by which the the key,value is separated in the properties file.
	 * @return
	 * @throws Exception 
	 */
	public static Set<String> getPropertiesSet(File file, String propertyDelimiter) throws Exception {

		return getPropertiesMap(file, propertyDelimiter).keySet();
	}
}
