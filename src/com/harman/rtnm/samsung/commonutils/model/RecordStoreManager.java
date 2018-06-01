package com.harman.rtnm.samsung.commonutils.model;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.harman.rtnm.samsung.commonutils.parser.csv.NpmFileParser;
import com.harman.rtnm.samsung.commonutils.util.IOUtils;
import com.harman.rtnm.samsung.commonutils.util.StringUtils;
import com.harman.rtnm.samsung.commonutils.writer.NpmFileWriter;


/**
 * RecordStoreManager can be used to perform operations like storing of record into a file
 * and reteriving the record later. The stored records can be directly merged too with the 
 * other input file using this api.
 *
 */
public final class RecordStoreManager {

	private static final Logger LOGGER = Logger.getLogger(RecordStoreManager.class);

	private Map<String, NpmFileWriter> storeWriters = new HashMap<String, NpmFileWriter>();

	NpmFileParser reader = null;

	/*
	 * Constructor
	 */
	public RecordStoreManager() {

	}


	/**
	 * Store the record into the file at the provided path
	 * @param filePath
	 * @param record
	 * @throws Exception 
	 */
	public synchronized void storeRecord(String filePath, String record) throws Exception {
		NpmFileWriter writer = getFileWriter(filePath, false);
		writer.write(record);
	}

	/**
	 * Retrieve records stored in the file at the provided path
	 * @return
	 */
	public List<String[]> retrieveRecords(String filePath, String delimiter) throws Exception {
		//close the existing reader 
		if(reader != null) {
			IOUtils.close(reader);
		}
		reader = new NpmFileParser(filePath, delimiter);
		return reader.getAllRows();
	}

	/**
	 * Retrieves the records from the provided store file and adds it to the existing file.
	 * It adds all the records at the end of the existing file.
	 * @param currentFilePath
	 * @param recordStoreFilePath
	 * @throws Exception
	 */
	public synchronized void retrieveRecordsAndMerge(String currentFilePath, String recordStoreFilePath) throws Exception {
		//close the existing reader 
		if(reader != null) {
			IOUtils.close(reader);
		}

		NpmFileWriter writer = getFileWriter(recordStoreFilePath, true);
		reader = new NpmFileParser(currentFilePath);
		/*hack to skip the header of the file. 
			Its picked up from the config file and column indexes are generated based on that.
		 */
		reader.getNextRow(); 

		List<String> listRecords= reader.getNextRowsBatchAsText();
		while(listRecords.size() > 0) {
			Iterator<String> iteratorRecords = listRecords.iterator();
			while(iteratorRecords.hasNext()) {
				String record = iteratorRecords.next();
				writer.write(record +  StringUtils.NEW_LINE);
			}

			listRecords = reader.getNextRowsBatchAsText();
		}	

		IOUtils.close(writer);	
		IOUtils.close(reader);


		//delete the existing file
		LOGGER.info("Deleted the current input file without retained records ? " + new File(currentFilePath).delete());
		//renaming the file
		LOGGER.info("Renamed the file with current and retained records merged ? " + IOUtils.getFile(recordStoreFilePath).renameTo(new File(currentFilePath)));

	}


	/**
	 * Returns the file writer for the specified file from the pool if existing, otherwise new instance of 
	 * {@link NpmFileWriter} is created with provided file path. 
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	private NpmFileWriter getFileWriter(String filePath, boolean appendMode) throws Exception {

		NpmFileWriter writer = storeWriters.get(filePath);
		if(writer == null || writer.isClosed()) {
			writer = new NpmFileWriter(filePath, appendMode);
			storeWriters.put(filePath, writer);
		}

		return writer;
	}	

	/**
	 * Closed the RSM and the underlying streams readers / writers
	 */
	public synchronized void close() {
		Iterator<NpmFileWriter> writers = storeWriters.values().iterator();
		while(writers.hasNext()) {
			IOUtils.close(writers.next());
		}
		IOUtils.close(reader);
		storeWriters.clear();
	}

	/**
	 * Ensuring to release all resources before this gets GCed
	 */
	public void finalize() {
		close();
	}
}
