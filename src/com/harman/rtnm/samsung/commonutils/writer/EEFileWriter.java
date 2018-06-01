package com.harman.rtnm.samsung.commonutils.writer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.harman.rtnm.samsung.commonutils.model.IndexTable;
import com.harman.rtnm.samsung.commonutils.model.Row;
import com.harman.rtnm.samsung.commonutils.util.IOUtils;
import com.harman.rtnm.samsung.commonutils.util.StringUtils;
import com.harman.rtnm.samsung.commonutils.writer.NpmFileWriter;

/**
 * It writes the data into the file. Along
 * with writing the data, it does the indexing of 
 *
 */
public class EEFileWriter {
	
	private NpmFileWriter npmFileWriter = null;
	private IndexTable<String, List<Long>> masterIndexTable = new IndexTable<String, List<Long>>(); 
	private IndexTable<Object, Object> indexTable = new IndexTable<Object, Object>();	
	
	/**
	 * 
	 * 
	 * @param outputFilePath
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public EEFileWriter(String outputFilePath) throws FileNotFoundException, IOException {
		this(IOUtils.getFile(outputFilePath), false, false);
		
	}
	
	/**
	 * 
	 * 
	 * @param outputFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public EEFileWriter(File outputFile, boolean appendMode, boolean writeLengthyString) throws FileNotFoundException, IOException {
		npmFileWriter = new NpmFileWriter(outputFile, appendMode, writeLengthyString ? NpmFileWriter.WRITE_LENTHY_SIZED_STRINGS : NpmFileWriter.WRITE_REGULAR_SIZED_STRINGS);
	}

	/**
	 * 
	 * 
	 * @param outputFilePath
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public EEFileWriter(String outputFilePath, boolean appendMode, boolean writeLengthyString) throws FileNotFoundException, IOException {
		this(IOUtils.getFile(outputFilePath), appendMode, writeLengthyString);
		
	}
	
	/**
	 * 
	 * 
	 * @param outputFile
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public EEFileWriter(File outputFile) throws FileNotFoundException, IOException {
		this(outputFile, false, false);
	}

	
	/**
	 * 
	 * 
	 * @param row
	 * @param key
	 * @throws IOException
	 */
	public void write(String row, String key) throws IOException {
		if(!StringUtils.isNullOrEmpty(row)) {
			npmFileWriter.writeAndIndex(row, key, indexTable);
			
			Long index = (Long)indexTable.get(key);
			List<Long> listOfIndices = masterIndexTable.get(key);
			if(listOfIndices == null) {
				listOfIndices = new ArrayList<Long>();
				masterIndexTable.put(key, listOfIndices);
			}
			
			listOfIndices.add(index);
		}
	}
	
	/**
	 * 
	 * 
	 * @param row
	 * @param key
	 * @throws IOException
	 */
	public void write(Row row) throws IOException {
		write(row.toString(), row.uniqueKey());
	}
	
	/**
	 * 
	 * @return
	 */
	public Map<String, List<Long>> getIndexMapAggregated() {
		return this.masterIndexTable;
	}
	
	/*
	 * close the file stream
	 */
	public void close() {
		IOUtils.close(npmFileWriter);
	}
}
