package com.harman.rtnm.samsung.commonutils.logger;

import org.apache.log4j.FileAppender;

import com.harman.rtnm.samsung.commonutils.util.DateUtils;


public class LoggingFileAppender extends FileAppender {

	@Override
	public void setFile(String file)
	{
		String loggingFile = file;
		if (loggingFile.indexOf("%timestamp") > 0) {
			loggingFile = file.replaceAll("%timestamp", DateUtils.getCurrentTimestamp());
		}

		super.setFile(loggingFile);
	}
}
