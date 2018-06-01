package com.harman.rtnm.samsung.commonutils.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.harman.rtnm.samsung.commonutils.model.Row;
import com.harman.rtnm.samsung.commonutils.model.RowConfig;
import com.harman.rtnm.samsung.commonutils.parser.csv.NpmFileParser;
import com.harman.rtnm.samsung.commonutils.writer.NpmFileWriter;

/**
 * 
 * A utility class to merge the segregated data
 * for the single entity into a single row. 
 *
 */

public final class MergeUtil {


	/**
	 * Method to merge the segregated data. The information  of 
	 * @param config
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void mergeCounterData(MergeUtilConfig config) throws FileNotFoundException, IOException {

		/// file reader for reading the file, which has the segragated data
		NpmFileParser fileReader = new NpmFileParser(config.getInputFilePath(), config.getHeaderDelimiter(), true);

		//write for the writing the aggregated data into the final output file
		NpmFileWriter fileWriter = new NpmFileWriter(config.getOuptutFilePath());

		//the config for initializing the ROW object
		RowConfig configRow = new RowConfig(config.getOutputHeader(), config.getHeaderDelimiter());

		//the row object for holding the aggregated data
		Row rowOutput = new Row(configRow, false);
		rowOutput.setColumnDefaultValue(StringUtils.EMPTY_BLANK_STRING);

		Map<String, List<Long>> indexTable = config.getIndexHashMap();
		Iterator<String> keyIterator = indexTable.keySet().iterator();

		/*
		 * Iterate over all list of lines/index for aggregating the
		 * data into one single row object for a single entity
		 */
		while(keyIterator.hasNext()) {
			rowOutput.clear();

			List<Long> indexesSubelements = indexTable.get(keyIterator.next());
			for(Long index : indexesSubelements) {
				String[] record = fileReader.getNextRowAsArrayAt(index);
				for(int idx = 0; idx < record.length; idx++) {
					if(StringUtils.isNullOrEmpty(rowOutput.getColumnValue(idx))) {
						rowOutput.setColumnValue(idx, record[idx]);
					}
				}
			}

			fileWriter.write(rowOutput.toString());
		}

		IOUtils.close(fileWriter);
		IOUtils.close(fileReader);
		rowOutput.clear();
	}


}
