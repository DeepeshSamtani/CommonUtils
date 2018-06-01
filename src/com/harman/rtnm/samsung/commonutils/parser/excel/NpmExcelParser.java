package com.harman.rtnm.samsung.commonutils.parser.excel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.harman.rtnm.samsung.commonutils.util.IOUtils;
import com.harman.rtnm.samsung.commonutils.util.StringUtils;
import com.harman.rtnm.samsung.commonutils.writer.NpmFileWriter;


public class NpmExcelParser {

	private static final Logger LOGGER = Logger.getLogger(NpmExcelParser.class);

	private NpmFileWriter fileWriter = null;
	private HSSFWorkbook excelWorkbook = null;
	
	public NpmExcelParser(String filePath) throws FileNotFoundException, IOException {
		if(filePath == null ) {
			throw new IllegalArgumentException("file object is null");
		}
		excelWorkbook = new HSSFWorkbook(new FileInputStream(filePath));
	}

	public String[] getNextRow() {
		
		return null;
	}

	public void generateCSVForExcelSheetAt(int sheetIndex, String outputFilePath) {
		StringBuffer data = new StringBuffer();

		try {
			fileWriter = new NpmFileWriter(IOUtils.getFile(outputFilePath));
			HSSFSheet sheet = excelWorkbook.getSheetAt(sheetIndex);
			Cell cell;
			Row row; //This is POI Row Api, don't get confused with model.Row pojo
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					cell = cellIterator.next();
					data.append(getCellStringValue(cell));					
				}

				data.append(StringUtils.NEW_LINE);
				fileWriter.write(data.toString());
				data.setLength(0);
			}
			LOGGER.info(" Successfully converted the excel to CSV files. ");
		} catch (IOException e) {
			LOGGER.error("Exception occurred while converting the excel to csv ", e);
		} finally {
			IOUtils.close(fileWriter);
		}
	}
	
	/**
	 * Get value of current cell as String
	 * @param cel
	 * @return
	 */
	private String getCellStringValue(Cell cel) {
		switch (cel.getCellType()) {
		case Cell.CELL_TYPE_BOOLEAN:
			return String.valueOf(cel.getBooleanCellValue());
		case Cell.CELL_TYPE_NUMERIC:
			return String.valueOf(cel.getNumericCellValue());
		case Cell.CELL_TYPE_STRING:
			return cel.getStringCellValue();
		case Cell.CELL_TYPE_BLANK:
			return StringUtils.EMPTY_BLANK_STRING;
		default:
			return cel.toString();
		}
	}
	
	/**
	 * Generating multiple CSV in a single method call
	 * @param outputCsvPaths
	 */
	public void generateMultiCSVForExcel(String[] outputCsvPaths) {
		for(int index = 1; index < excelWorkbook.getNumberOfSheets(); index++) {
			generateCSVForExcelSheetAt(index, outputCsvPaths[index]);			
		}		
	}
}
