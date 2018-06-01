package com.harman.rtnm.samsung.commonutils.parser.csv;

import java.io.IOException;
import java.util.List;

public interface IParser {

	List<String[]>  getNextRowsBatchAsColumnArray() throws Exception;
	
	List<String[]> getAllRows() throws Exception ;
	
	void close() throws IOException;
}
