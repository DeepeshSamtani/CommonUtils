package com.harman.rtnm.samsung.commonutils.exception;

/**
 * It is thrown when the parsed record from the feed file is NOT VALID.
 *
 */
@SuppressWarnings("serial")
public class InvalidRecordException extends Exception {

	public InvalidRecordException() {
		super();
	}
	
	/**
	 * @param message : reason for marking record as invalid
	 */
	public InvalidRecordException(String message) {
		super(message);
	}
}
