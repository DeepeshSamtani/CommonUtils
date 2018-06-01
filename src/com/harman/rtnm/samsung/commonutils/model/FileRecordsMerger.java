package com.harman.rtnm.samsung.commonutils.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.harman.rtnm.samsung.commonutils.comparator.Comparator;
import com.harman.rtnm.samsung.commonutils.parser.csv.NpmFileParser;
import com.harman.rtnm.samsung.commonutils.util.IOUtils;
import com.harman.rtnm.samsung.commonutils.util.StringUtils;
import com.harman.rtnm.samsung.commonutils.writer.NpmFileWriter;


/**
 * TODO: comments
 *
 */
public class FileRecordsMerger {

	private static final Logger LOGGER = Logger.getLogger(FileRecordsMerger.class);

	protected NpmFileWriter fileWriterOutput;
	protected NpmFileParser fileParserInput;

	private File directoryInput;
	private String[] directoriesInput;
	private String[] filesOutput;


	//default constructor
	public FileRecordsMerger() {

	}

	/**
	 * Cosntructor to initialize {@link FileRecordsMerger} instance using file directoryInput object
	 * @param directoryInput
	 * @throws IOException  
	 */
	public FileRecordsMerger(String[] directoryPath, String[] outputFilePath) throws IOException {
		if(directoryPath == null || outputFilePath == null || (directoryPath.length != outputFilePath.length)) {
			throw new NullPointerException("Either input directory or output directory is null Or output file path is missing for 1 or more input directories ");
		}

		this.directoriesInput = directoryPath;
		this.filesOutput = outputFilePath;
	}

	/**
	 * @return the directoriesInput
	 */
	public String[] getDirectoriesInput() {
		return directoriesInput;
	}

	/**
	 * @param directoriesInput the directoriesInput to set
	 */
	public void setDirectoriesInput(String[] directoriesInput) {
		this.directoriesInput = directoriesInput;
	}

	/**
	 * @return the filesOutput
	 */
	public String[] getFilesOutput() {
		return filesOutput;
	}

	/**
	 * @param filesOutput the filesOutput to set
	 */
	public void setFilesOutput(String[] filesOutput) {
		this.filesOutput = filesOutput;
	}


	/**
	 * Writing all the records into the required single output file 
	 * @param listOfRows
	 * @throws IOException
	 */
	private void writetoOutputFile(List<String> listOfRows) throws IOException {
		for(String record : listOfRows) {
			fileWriterOutput.write(record+StringUtils.NEW_LINE);
		}
	}

	/**
	 * Writting the record to the required mereged output file.
	 * @param row
	 * @throws IOException
	 */
	private void writetoOutputFile(String row) throws IOException { 
		fileWriterOutput.write(row+StringUtils.NEW_LINE);
	}

	/**
	 * Merge the records into one single output file for the given set of directories and the designated 
	 * output files which are 
	 * @param outputFile
	 * @param outputFileDelimiter
	 * @return
	 * @throws IOException
	 */
	public void mergeRecords() throws IOException {
		boolean skipHeader = false;

		for(int index=0; index < directoriesInput.length; index++) {
			skipHeader = false;
			directoryInput = IOUtils.getFile(directoriesInput[index]);
			fileWriterOutput = new NpmFileWriter(IOUtils.getFile(filesOutput[index]));
			List<String> listOfFilesPath = IOUtils.getListOfFilesPathAtDirectory(directoryInput);
			for(String filePath : listOfFilesPath) {
				fileParserInput = new NpmFileParser(filePath);
				ArrayList<String> rowsBatch = fileParserInput.getNextRowsBatchAsText();				
				while(rowsBatch.size() > 0) {
					if(skipHeader) {
						rowsBatch.remove(0);
					}
					skipHeader = true;
					writetoOutputFile(rowsBatch);
					rowsBatch = fileParserInput.getNextRowsBatchAsText();
				}

				IOUtils.close(fileParserInput);
			}
			IOUtils.close(fileWriterOutput);
		}
		releaseResources();
	}

	/**
	 * Merge the records into one single output file for the given set of directories and the designated 
	 * output files will contain the records  
	 * @param comparator
	 * @param delimiter
	 * @param sortColumnIndex
	 * @param keyColumns
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void mergeRecords(Comparator comparator, String delimiter, int sortColumnIndex, int[] keyColumns) throws Exception {
		boolean skipHeader = false;
		HashMap<String, String> sortedMap = new HashMap<String, String>();

		for(int index=0; index < directoriesInput.length; index++) {
			skipHeader = false;
			directoryInput = IOUtils.getFile(directoriesInput[index]);
			fileWriterOutput = new NpmFileWriter(IOUtils.getFile(filesOutput[index]));
			List<String> listOfFilesPath = IOUtils.getListOfFilesPathAtDirectory(directoryInput);

			for(String filePath : listOfFilesPath) {
				fileParserInput = new NpmFileParser(filePath);
				ArrayList<String> rowsBatch = fileParserInput.getNextRowsBatchAsText();

				while(rowsBatch.size() > 0) {
					if(skipHeader) {
						rowsBatch.remove(0);
					}
					skipHeader = true;
					for(String row : rowsBatch) {
						try {
							if(StringUtils.isNullOrEmpty(row)) { continue; }

							String key = "";
							String[] record = row.split(delimiter, -1);
							/*
							 * 1-2 or two files are having some special char which are causing the
							 * issue, couldn't see any problem with the reading pointers and logic.
							 * Work's for all the input files accross products except 1-2 ICG input files
							 * Putting this hack to ignore corrupted data.
							 */
							if(record.length < keyColumns[keyColumns.length-1] || record.length < sortColumnIndex) { continue; } 

							{
								if(keyColumns.length > 1) {
									for(int i : keyColumns) {
										key+= StringUtils.trimIt(record[i]);
									}
								} else {
									key = record[keyColumns[0]]; 
								}
							}

							String previousValue = sortedMap.get(key);
							String currentValue = record[sortColumnIndex];

							if(StringUtils.isNullOrEmpty(previousValue)) {
								sortedMap.put(key, currentValue);	
								writetoOutputFile(row);
							}
							//ignore the record if the its latest copy has been already written into the output file.
							else if(comparator.compare(previousValue, currentValue)) {							
								sortedMap.put(key, currentValue);	
								writetoOutputFile(row);
							}
						} catch(Throwable e) {
							LOGGER.warn(" Receieved corruptted data while merging the records, please check the files in the input directory and removed the compressed or corrupted data files.", e);
						}
					}
					rowsBatch = fileParserInput.getNextRowsBatchAsText();
				}

				sortedMap.clear();
				IOUtils.close(fileParserInput);
			}
			IOUtils.close(fileWriterOutput);
		}
		releaseResources();
	}

	/**
	 * Close the underlying reader and writer for releasing resources.
	 */
	private void releaseResources() {
		IOUtils.close(fileWriterOutput);
		IOUtils.close(fileParserInput);
	}

}
