package com.harman.rtnm.samsung.commonutils.parser.csv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.harman.rtnm.samsung.commonutils.util.IOUtils;
import com.harman.rtnm.samsung.commonutils.util.StringUtils;


/**
 * NpmMultiFileParser is capable of reading/parsing multiple file
 * with a single instance and useful wrapper when it is required to read 
 * multiple files of same structure within a single directory.
 * 
 * The files will be read in the same sequence in which those reside
 * int the directory or specified by the api user.
 * 
 * </br></br>This class is fail safe as it does not forces on really
 * providing a file path(s) and returns every time an empty list.
 *
 *
 */
public class NpmMultiFileParser implements IParser {

	private static final Logger LOGGER = Logger.getLogger(NpmMultiFileParser.class);

	private NpmFileParser fileParser = null;

	private List<File> pathsList = null;

	private String delimiter;

	private boolean dataExists = true;

	private int parserCount = -1;

	/**
	 * Instantiating {@link NpmMultiFileParser} with the directory
	 * in which all the files reside. 
	 * @param directory
	 */
	public NpmMultiFileParser(File directory, String delimiter) {
		if(directory == null || !directory.isDirectory()) {
			dataExists = false;
		} else {
			this.pathsList = Arrays.asList(directory.listFiles());
			this.delimiter = delimiter;
		}
	}

	public NpmMultiFileParser(String directoryPath, String delimiter) throws IOException {
		if(StringUtils.isNullOrEmpty(directoryPath)) {
			this.dataExists = false;
		} else {
			this.pathsList = Arrays.asList(IOUtils.getFile(directoryPath).listFiles());
			this.delimiter = delimiter;
		}

	}

	/**
	 * Instantiating {@link NpmMultiFileParser} with the list of 
	 * all the pathsList  
	 * @param files : list of files to be parsed
	 */
	public NpmMultiFileParser(List<File> files, String delimiter) {
		if(files == null || files.size() == 0) {
			this.dataExists = false;
		} else {
			this.pathsList = files;
			this.delimiter = delimiter;
		}
	}

	/**
	 * Utility method to get the fileParser for the next available file
	 * in the list/directory, if any.
	 * @return
	 * @throws IOException
	 */
	private NpmFileParser nextParser() throws IOException {
		parserCount++;
		if(parserCount < this.pathsList.size()) {
			if(fileParser == null) {
				fileParser = new NpmFileParser(this.pathsList.get(parserCount), delimiter);
			} else {
				fileParser = fileParser.createReader(this.pathsList.get(parserCount), delimiter);
			}
		} else {
			fileParser = null;
		}

		return fileParser;
	}

	/**
	 * Get all the records in the given directory or folder.
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String[]> getAllRows() throws Exception {

		ArrayList<String[]> listOfRecords = new ArrayList<String[]>();
		
		ArrayList<String[]>  records = getNextRowsBatchAsColumnArray();
		while(records.size() > 0) {
			listOfRecords.addAll(records);			
			records = getNextRowsBatchAsColumnArray();
		}
	
		return listOfRecords;
	}
	
	/**
	 * Every call to this method will return the number of lines as String array.</br>
	 * @return List of String[] 
	 * @throws Exception
	 */
	public ArrayList<String[]> getNextRowsBatchAsColumnArray() throws Exception {
		fileParser = nextParser();
		ArrayList<String[]> records = new ArrayList<String[]>();
		LOGGER.debug(" Data exists : " + dataExists);
		if(dataExists && fileParser != null) {
			records = fileParser.getNextRowsBatchAsColumnArray();
			if(records.size() == 0) {
				fileParser = nextParser();
				if(fileParser != null) {
					records = fileParser.getNextRowsBatchAsColumnArray();
				}
			}
			
			LOGGER.debug(" records list : " + records.size() + " first data : " + records.get(0));
		} else {
			LOGGER.debug(" No more data exists, please check the config for directory path or the directory having the input files." );
		}		
		return records;
	}

	/**
	 * Closing the underlying {@link NpmFileParser}
	 */
	public void close() {
		IOUtils.close(this.fileParser);
	}
}
