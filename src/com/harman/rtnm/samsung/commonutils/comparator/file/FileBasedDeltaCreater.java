package com.harman.rtnm.samsung.commonutils.comparator.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.harman.rtnm.samsung.commonutils.constants.Constants;
import com.harman.rtnm.samsung.commonutils.model.IndexTable;
import com.harman.rtnm.samsung.commonutils.model.Row;
import com.harman.rtnm.samsung.commonutils.parser.csv.NpmFileParser;
import com.harman.rtnm.samsung.commonutils.util.IOUtils;
import com.harman.rtnm.samsung.commonutils.util.StringUtils;
import com.harman.rtnm.samsung.commonutils.writer.NpmFileWriter;


/**
 * It is used to compare the records in two different files and gives the delta out of it. It assumes that one of the file is written using the {@link NpmFileWriter}
 * with indexing API. The objects MUST have hashcode, equals and toString implementation.</br></b></br>
 * <b>How it works!!</b></br>
 * {@link FileBasedDeltaCreater} accepts a {@link List} of records in <b>createDeltaFromList(list, boolean)</b> method. Each and every record in the provided list is 
 * looked-up in the {@link IndexTable}. </br></br>
 * 1. if the records does not exist in the index table, then its a NEW record</br>
 * 2. if it exists, then based on equality decides if the records should be considered for final output</br>
 * 3. Left out records in the {@link IndexTable} are something not received as we speak.
 * 
 *
 */
public class FileBasedDeltaCreater extends DeltaCreator {
	private static final Logger LOGGER =  Logger.getLogger(FileBasedDeltaCreater.class);

	/**
	 * This is like a bit map which will hold value TRUE for the columns 
	 * whihc can be ignored while doing the comparison for finding MOD records.
	 */
	protected boolean[] ignorableColumnIndexes = new boolean[128];

	private int transTypeCol = 0;

	protected IndexTable<Object, Object> recordsIndexTable;
	private Map<Object, HashMap<Object, Long>> mapParentKeyChildrenIndexes;
	private Map<Object, Long> mapChildrenKeyIndexes;

	private File outputFile;
	private File indexTableFile;
	private File validRecordsFile = null;

	private String keyToYesterdayValidFile;
	private String eofPattern = StringUtils.END_OF_FILE_PATTERN;

	protected NpmFileParser fileParserMasterIndexTableRecords;
	private NpmFileParser fileParserChildrenRecords;
	private NpmFileWriter outputFileWriter;

	ArrayList<String> outputRecordsList = new ArrayList<String>(500);

	/**
	 * Constructor to create the comparator backed by given index table of records
	 * @param indexTable : map containing the index for a given record key 
	 * @param keyToYesterdayValidFile : the key which was used to store valid file path in index table
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public FileBasedDeltaCreater(File indexFile, String keyToYesterdayValidFile) {
		if(indexFile == null || StringUtils.isNullOrEmpty(keyToYesterdayValidFile)) {
			throw new NullPointerException(" Index table file or previous day's valid file cannot be null ");
		}
		mapParentKeyChildrenIndexes = new HashMap<Object, HashMap<Object,Long>>(1000);
		mapChildrenKeyIndexes = new LinkedHashMap<Object, Long>(100);
		this.indexTableFile = indexFile;
		this.keyToYesterdayValidFile = keyToYesterdayValidFile;
		initIndexTable();
		initDefaultIgnorableColumnIndexes();
	}

	public void setTransTypeColumnIndex(int index) {
		if(index < 0) {
			throw new IllegalArgumentException(" in-correct trans type column index ");
		}
		this.transTypeCol = index;
	}

	/**
	 * Set the file containing all records for children/dependents
	 * @param file
	 * @throws FileNotFoundException 
	 */
	public void setChildrenRecordsFile(File file) throws FileNotFoundException {
		if(file == null) {
			throw new NullPointerException(" file containing children records cannot be null");
		}

		fileParserChildrenRecords = new NpmFileParser(file, StringUtils.SYMBOL_CIDLA);
	}

	/**
	 * Setter for initializing the parser to read yesterday's valid records file
	 * @param file
	 * @throws FileNotFoundException
	 */
	private void setPreviousDayValidFile(File file) throws FileNotFoundException {
		if(file == null) {
			throw new NullPointerException(" Previous day file to be compared cannot  be null ");
		}

		fileParserMasterIndexTableRecords = new NpmFileParser(file, StringUtils.SYMBOL_CIDLA);
	}

	/**
	 * Set the final output file where all the delta records should be written
	 * @param file
	 * @param outputHeader
	 * @throws IOException
	 */
	public void setOutputFile(File file, String outputHeader) throws IOException {
		if(file == null) {
			throw new NullPointerException(" Output file cannot be null ");
		}

		this.outputFile = file;
		outputFileWriter = new NpmFileWriter(outputFile);
		outputFileWriter.write(outputHeader+StringUtils.NEW_LINE);
	}

	/**
	 * initializing the Index table by unmarshaling the {@link IndexTable} 
	 */
	@SuppressWarnings("unchecked")
	private void initIndexTable() {
		try {
			if(indexTableFile.length() == 0) {				
				recordsIndexTable = new IndexTable<Object, Object>();
			} else {
				recordsIndexTable = (IndexTable<Object, Object>)IOUtils.unmarshalingObject(indexTableFile);
				recordsIndexTable.initProcessedRecordsContainer();
				validRecordsFile = (File)recordsIndexTable.get(keyToYesterdayValidFile);
				setPreviousDayValidFile(validRecordsFile);
				recordsIndexTable.remove(keyToYesterdayValidFile);
			}

		} catch (Exception e) {
			LOGGER.error(" Exception occured while setting the index table ", e);
			throw new RuntimeException("Exception occured while initalizing the index table");
		}	
	}

	/**
	 * The mapping from Parent to child has to be saved by the client api. So 
	 * @param mapParentChildMapping
	 */
	public void setParentKeyChildIndexesMapping(Map<Object, HashMap<Object, Long>> mapParentChildMapping) {
		if(mapParentChildMapping == null) {
			this.mapParentKeyChildrenIndexes = new HashMap<Object, HashMap<Object, Long>>(1000);
			return;
		}
		this.mapParentKeyChildrenIndexes = mapParentChildMapping;
	}

	/**
	 * initialize all the ignorable indexes to false
	 */
	protected void initDefaultIgnorableColumnIndexes() {
		for(int i=0; i < ignorableColumnIndexes.length; i++) {
			ignorableColumnIndexes[i] = false;
		}
	}

	/**
	 * Creates the delta from the provided list of records and writes it to the final output file
	 * @param list
	 * @param isLastList
	 */
	public void createDeltaFromList(List<Object> list, boolean isLastList) {		
		try {
			Iterator<Object> iteratorCurrentRecords = list.iterator();

			while(iteratorCurrentRecords.hasNext()) {	

				Object record = iteratorCurrentRecords.next();
				String currentRecord  = record.toString().replace(StringUtils.NEW_LINE, StringUtils.EMPTY_BLANK_STRING);
				int transType = compare(record, null);

				if(transType != IGNORE) {
					//modifying the coressponding device record and adding to delta file
					addToOutputList(getDeviceRecord(record, Constants.TRANS_TYPE_MODIFY, true));

				}

				if(transType == NEW) {
					LOGGER.debug(" New records added. ");					
					addToOutputList(currentRecord);					
				} else if(transType == MOD) {
					LOGGER.debug(" MOD records added. ");

					currentRecord = setTransType(Constants.TRANS_TYPE_MODIFY, currentRecord);
					addToOutputList(currentRecord);
				}

				//recordsIndexTable.remove(record); //before delta issue in PROD
				deleteRecordFromIndexTable(record);
			}
			writeToOutputFile(isLastList);

		} catch(Exception e) { 
			LOGGER.error("Error occurred while processing the delta for list size : " + list.size() +". Is last list ? "+ isLastList, e);
		}
	}

	/**
	 * The columns indexes which are to be ignored while comparing two objects(Device, subelement etc.)
	 * while creating the MOD delta records
	 * @param ignoreColumns
	 */
	public void setIgnorableColumns(int[] ignoreColumns) {
		for(int column : ignoreColumns) {
			ignorableColumnIndexes[column] = true;
		}
	}

	/**
	 * Set the end of file pattern to embeded in the output file.
	 * @param eof
	 */
	public void setEOFPattern(String eof){
		if(StringUtils.isNullOrEmpty(eof)) {
			return;
		}
		this.eofPattern = eof;
	}

	/**
	 * This method is required as the output file writer is opened up 
	 * in this class and writting in parallel is not possible.
	 * Eventualy this utility method will help in adding the records 
	 * in the output file even though it's not part of the delta
	 * processing logic.
	 * @param record
	 */
	public void addToOutputList(String record) {
		if(!StringUtils.isNullOrEmpty(record)) {
			record = record.replace(StringUtils.NEW_LINE, StringUtils.EMPTY_BLANK_STRING);
			outputRecordsList.add(record);
		}
	}

	/**
	 * Writting records to the final NPM output file 
	 * @param isLastList
	 * @throws IOException
	 * TODO: transtype should be injected by the client code
	 */
	protected void writeToOutputFile(boolean isLastList) throws IOException {			
		if(isLastList) {
			Iterator<Object> deleteRecordsIterator = recordsIndexTable.keySet().iterator();
			while(deleteRecordsIterator.hasNext()) {
				Object delRecord = deleteRecordsIterator.next();
				String deletedRecord = getRecordAtIndex(fileParserMasterIndexTableRecords, delRecord);
				deletedRecord  = setTransType(Constants.TRANS_TYPE_DELETE, deletedRecord);

				//this line was not present before the critical delta issue in production
				if(!deletedRecord.startsWith(Constants.SUBELEMENT_TYPE_VALUE_DEVICE)) {
					String deviceRecord = getDeviceRecord(delRecord, Constants.TRANS_TYPE_MODIFY, false); 
					if(!recordsIndexTable.isRecordProcesed(deviceRecord)) {
						addToOutputList(deviceRecord);
						recordsIndexTable.processedRecord(deviceRecord);
					}

				} else if(recordsIndexTable.isRecordProcesed(delRecord)) {
					continue;
				}

				outputRecordsList.add(deletedRecord);			

				deleteChildrens(delRecord);
				deleteRecordsIterator.remove();
			}

			processChildren();		
			outputRecordsList.add(eofPattern);
		}

		LOGGER.info("Output delta list size : " + outputRecordsList.size() + ", is last list ? " + isLastList);
		Iterator<String> iterator = outputRecordsList.iterator();

		while(iterator.hasNext()) {
			String record = (iterator.next()+StringUtils.NEW_LINE);

			//replace all the spaces with blank string, if any
			record = record.replaceAll(StringUtils.EMPTY_SPACE_STRING+StringUtils.SYMBOL_CIDLA, StringUtils.SYMBOL_CIDLA);
			outputFileWriter.write(record);
		}

		outputRecordsList.clear();
	}

	/**
	 * Adding all the children which are required to be processed after the deletion of those which have their parent marked DEL in
	 * the output file.
	 * @throws IOException
	 */
	private void processChildren() throws IOException {
		Iterator<Long> childRecordsIterator = mapChildrenKeyIndexes.values().iterator();				
		while(childRecordsIterator.hasNext()) {
			Long child = childRecordsIterator.next();
			String childRecord = getRecordAtIndex(fileParserChildrenRecords, child);				
			outputRecordsList.add(childRecord);
		}
	}

	/**
	 * This is to set the indexes of ONLY those Children records which are 
	 * required to be processed for the current day. Other indexes of children
	 * had been already saved as part of their mapping against the 
	 * successor. 
	 */

	public void addCurrentChildrenIndexes(Object childKey, Long childIndex) throws IllegalStateException {
		if(mapChildrenKeyIndexes == null) {
			throw new IllegalStateException("A file handle to read the child records is not set.");
		}
		mapChildrenKeyIndexes.put(childKey, childIndex);
	}

	/**
	 * Setting the trantype for a record to be written in the final output file
	 * @param transType
	 * @param record
	 * @param transTypeColIndex
	 * @return
	 */
	protected String setTransType(String transType, String record) {
		String row[] = StringUtils.toStringArray(record, StringUtils.SYMBOL_CIDLA, true);
		row[transTypeCol] = transType;

		return StringUtils.arrayToString(row, StringUtils.SYMBOL_CIDLA, true);			
	}

	/**
	 * <li>return  0, if its a new record 
	 * <li>return  1, if the records has to be deleted from the inventory
	 * <li>return  2, if the existing records has beeen modified
	 * <li>return -1, if existing but no change in the record
	 * @param todayRecord - today's record
	 * @param yesterdayRecord - yesterday's record
	 * @return
	 */ 
	@Override
	public int compare(Object todayRecord, Object yesterdayRecord) {
		try {
			if(recordsIndexTable.containsKey(todayRecord)) {
				String previousRecord = getRecordAtIndex(fileParserMasterIndexTableRecords, todayRecord);
				LOGGER.trace("Previous record : " + previousRecord);

				String currentRecord = (String)todayRecord.toString();
				LOGGER.trace("Current record : " + currentRecord);

				if(StringUtils.containEqualValues(previousRecord.split(StringUtils.SYMBOL_CIDLA, -1), currentRecord.split(StringUtils.SYMBOL_CIDLA), ignorableColumnIndexes)) {
					return IGNORE;
				}
				return MOD;
			} else {
				return NEW;
			}
		} catch(Exception e) {
			LOGGER.error("Exception occured while comparing two records : ", e);

		}
		return MOD;
	}

	/**
	 * Close all the underlying readers and writers
	 */
	public void close() {
		IOUtils.close(fileParserChildrenRecords);
		IOUtils.close(fileParserMasterIndexTableRecords);		
		IOUtils.close(outputFileWriter);
	}

	/**
	 * Adding the records which are dependent on the deletion of other records. 
	 * This will be used by the client which knows the relationship between the parent-child keys.
	 * {@link FileBasedDeltaCreater} will be using it for deleting the children in case the parent 
	 * is marked as deleted and <b>reporting the left out children records in the output file as it 
	 * is(no change in transtype)</b>.
	 * @param parentKey
	 * @param childKey
	 * @param childIndex
	 * @return handle to the parent-child mapping tree
	 */
	public Map<Object, HashMap<Object, Long>> addParentKeyChildRecordIndex(Object parentKey, Object childKey, Long childIndex) {
		updateParent(parentKey, childKey);
		if(mapParentKeyChildrenIndexes.containsKey(parentKey)) {
			mapParentKeyChildrenIndexes.get(parentKey).put(childKey, childIndex);
			return mapParentKeyChildrenIndexes;
		}

		HashMap<Object, Long> listChildren = new HashMap<Object, Long>();
		listChildren.put(childKey, childIndex);
		mapParentKeyChildrenIndexes.put(parentKey, listChildren);
		return mapParentKeyChildrenIndexes;
	}

	/**
	 * This is to ensure that dupes are not being reported to downstream
	 * @param parentKey
	 * @param childKey
	 */
	private void updateParent(Object parentKey, Object childKey) {

		if(mapParentKeyChildrenIndexes.containsKey(parentKey) && 
				mapParentKeyChildrenIndexes.get(parentKey).containsKey(childKey)) {
			return;
		}

		Iterator<HashMap<Object, Long>> iterator = mapParentKeyChildrenIndexes.values().iterator();
		while(iterator.hasNext()) {
			if(iterator.next().remove(childKey) != null) {
				break;
			}
		}
	}

	/**
	 * Marks all the corresponding children records as deleted <b>(TRANS_TYPE = "DEL")</b> in the output file.
	 * This is handled using the Maps data structure. As of now i don't see the requirement of deleting too many children from too many different files. 
	 * If that becomes the case,then Data Structure holding the parent records is required to be converted into sort of a threaded tree 
	 * and should hold all the children. And those api(DS) should be available at the Comparison logic level. 
	 * @param keyParent
	 * @throws IOException
	 */
	private void deleteChildrens(Object keyParent) throws IOException {
		Iterator<Object> iteratorChildrenKeys = null;
		if(mapParentKeyChildrenIndexes.get(keyParent) == null) {
			return;
		}

		iteratorChildrenKeys = mapParentKeyChildrenIndexes.get(keyParent).keySet().iterator();
		while(iteratorChildrenKeys.hasNext()) {
			Object childKey = iteratorChildrenKeys.next();
			Long childIndex = mapChildrenKeyIndexes.get(childKey);
			String deleteChildRecord = getRecordAtIndex(fileParserChildrenRecords, childIndex);
			deleteChildRecord  = setTransType(Constants.TRANS_TYPE_DELETE, deleteChildRecord);
			outputRecordsList.add(deleteChildRecord);
			mapChildrenKeyIndexes.remove(childKey);
		}		
		mapParentKeyChildrenIndexes.remove(keyParent);
	}

	/**
	 * Returns the record residing on the file system at the specified index for the key in the index table.
	 * @param key
	 * @throws IOException 
	 */
	protected String getRecordAtIndex(NpmFileParser fileParser, Object key) throws IOException {		
		//		this.getRecordAtIndex(fileParser, (Long)recordsIndexTable.get(key));
		return fileParser.getNextRowAsStringAt((Long)recordsIndexTable.get(key));	
	}

	/**
	 * Returns the record residing on the file system at the specified index.
	 * @param key
	 * @throws IOException 
	 */
	private String getRecordAtIndex(NpmFileParser fileParser, Long index) throws IOException {
		return fileParser.getNextRowAsStringAt(index);	
	}


	private String getDeviceRecord(Object interphaceRecord, String transType, boolean removeRecord)  {
		Row keyRecord = null;
		Long index = null;
		try {
			if(interphaceRecord != null && interphaceRecord instanceof Row) {
				String subelementType = ((Row) interphaceRecord).getColumnValue(Constants.KEY_SUBELEMENT_TYPE);
				if (Constants.SUBELEMENT_TYPE_VALUE_DEVICE.equals(subelementType)) {				
					return StringUtils.EMPTY_BLANK_STRING;
				}

				/*
				 * Creating the copy of the existing interphace object so that 
				 * it does not end up modifying the original current record, 
				 * which is required to be stored into the current days IndexTable
				 * for future processing.
				 */
				Row tempInterphaceRecord = new Row((Row) interphaceRecord);
				tempInterphaceRecord.setColumnValue(Constants.KEY_INTERFACE, "");
				tempInterphaceRecord.setColumnValue(Constants.KEY_SUBELEMENT_TYPE, "");
				
				//creating device record key out of the given interphace record
				keyRecord = new Row(((Row) tempInterphaceRecord));
				if(removeRecord) {
					index = (Long) recordsIndexTable.remove(keyRecord);
				} else {
					index = (Long) recordsIndexTable.get(keyRecord);
				}

				if(index != null) {
					String deviceRecord = getRecordAtIndex(fileParserMasterIndexTableRecords, index);
					LOGGER.debug(" Device record fetched : " + deviceRecord);
					deviceRecord = setTransType(transType, deviceRecord);
					return deviceRecord;
				}
			} 
		} catch (Exception e) {
			LOGGER.error(" No corressponding device record fetched. ", e);
		}

		return StringUtils.EMPTY_BLANK_STRING;		
	}

	/**
	 * The deletion of the device record happpens in a slightly different way 
	 * than the interphace record.
	 * TODO: more comments 
	 * @param record
	 * @return
	 */
	private boolean deleteRecordFromIndexTable(Object record) {		
		if(record != null && record instanceof Row) {
			Row row = (Row) record;
			String subelementType = row.getColumnValue(Constants.KEY_SUBELEMENT_TYPE); 
			if (Constants.SUBELEMENT_TYPE_VALUE_DEVICE.equals(subelementType)) {
				recordsIndexTable.processedRecord(record);
				return false;
			}
		}

		return (recordsIndexTable.remove(record) != null);
	}
}
