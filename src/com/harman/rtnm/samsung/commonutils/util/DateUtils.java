package com.harman.rtnm.samsung.commonutils.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class to support date and time manipulations
 *
 */
public final class DateUtils {
	private static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";
	private static final String SIMPLE_DATE_FORMAT = "ddMMyyyy";
	
	
	private static SimpleDateFormat timeStampFormatter = new SimpleDateFormat(TIMESTAMP_FORMAT);
	private static SimpleDateFormat simpleDateFormatter = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
	private static SimpleDateFormat simpleDayFormatter = new SimpleDateFormat("dd");
	private static SimpleDateFormat simpleMonthFormatter = new SimpleDateFormat("MM");
	private static SimpleDateFormat simpleYearFormatter = new SimpleDateFormat("yyyy");

	public static final String DATE_FORMAT_yyyyMMdd = "yyyyMMdd";
	public static final String DATE_FORMAT_ddMyyyy = "dd-M-yyyy";
	public static final String DATE_FORMAT_ddMyyyyhhmmss = "dd-M-yyyy hh:mm:ss";
	
	/**
	 * Time 00:01 AM
	 */
	public static final String TIME_MIDNIGHT_hhmmss = " 00:01:00";
	
	/**
	 * Stricly no no, why you need when all the methods are declared as static
	 */
	private DateUtils() {
		
	}
	
	/**
	 * Appends the current timestamp in _yyyymmddhhmmss format e.g _20150722022550 and return the string
	 * @param str
	 * @return
	 */
	public static String appendCurrentTimestamp(String str) {

		return StringUtils.isNullOrEmpty(str) ? (timeStampFormatter.format(new Date())) : (str+"_"+timeStampFormatter.format(new Date()));		
	}

	/**
	 * Appends the current date in _ddMMyyyy format e.g. _22022015 format and return
	 * @param str
	 * @return
	 */
	public static String appendCurrentDate(String str) {
		return StringUtils.isNullOrEmpty(str) ? (simpleDateFormatter.format(new Date())) : (str+"_"+simpleDateFormatter.format(new Date()));		
	}

	/**
	 * Appends the current date in given format and return
	 * @param str
	 * @param dateFormat the pattern to be used for formatting the date
	 * @return
	 */
	public static String appendCurrentDate(String str, String dateFormat) {
		if(StringUtils.isNullOrEmpty(dateFormat)) {
			return str;
		}
		
		SimpleDateFormat simpleDateFormatter = new SimpleDateFormat(dateFormat);
		return StringUtils.isNullOrEmpty(str) ? (simpleDateFormatter.format(new Date())) : (str+"_"+simpleDateFormatter.format(new Date()));
	}
	
	/**
	 * Returns the current timestamp in yyyymmddhhmmss format e.g 20150722022550
	 * @return
	 */
	public static String getCurrentTimestamp() {
		return appendCurrentTimestamp("");
	}

	/**
	 * Returns the current date in yyyymmdd format e.g. 20150222 
	 * @return
	 */
	public static String getCurrentDate() {
		return appendCurrentDate("");
	}

	/**
	 * Generic utility method for formatting the provided date with the provided pattern
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(Date date, String pattern) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(date);
	}
	
	
	/**
	 * Generic utility method for formatting the provided date with the provided pattern
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(String date, String pattern) {
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		return formatter.format(date);
	}
	
	
	/**
	 * Generic utility method for formatting the curent date with the provided pattern
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(String pattern) {		
		return format(new Date(), pattern);
	}

	/**
	 * Pars the string for retrieving the 
	 * @param str
	 * @return 
	 */
	public static Date parseDate(String str) {
		Date date = null;
		try {
			date = simpleDateFormatter.parse(str);
		} catch(ParseException ex) {
			return null;
		}
		return date;
	}
	
	/**
	 * Uitlity method to parse the give date String as per the specified pattern
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static Date parseDate(String date, String pattern) {
		Date d = null;
		try {
			SimpleDateFormat sFormat = new SimpleDateFormat(pattern);
			d = sFormat.parse(date);
		} catch(ParseException ex) {
			ex.printStackTrace();
			return null;
		}
		return d;
	}

	/**
	 * Get day of the current date
	 * @return
	 */
	public static String getCurrentDay() {
		return simpleDayFormatter.format(new Date());

	}
	
	/**
	 * Get day of the provided date
	 * @param date
	 * @return
	 */
	public static String getDay(Date date) {
		return simpleDayFormatter.format(date);
	}
	
	/**
	 * Get month of the current date
	 * @return
	 */
	public static String getCurrentMonth() {
		return simpleMonthFormatter.format(new Date());
	}
	
	/**
	 * Get month of the provided date
	 * @param date
	 * @return
	 */
	public static String getMonth(Date date) {
		return simpleMonthFormatter.format(date);
	}
	
	/**
	 * Get year of the current date
	 * @return
	 */
	public static String getCurrentYear() {
		return simpleYearFormatter.format(new Date());
	}
	
	/**
	 * Get year of the provided date
	 * @param date
	 * @return
	 */
	public static String getYear(Date date) {
		return simpleYearFormatter.format(date);
	}
	
	/**
	 * Get current date in ddMMyyyy format with delimiter appended in between
	 * @param delimiter
	 * @return
	 */
	public static String getCurrentDate(String delimiter) {
		return getCurrentDay()+delimiter+getCurrentMonth()+delimiter+getCurrentYear();
	}
	
	/**
	 * Get time in millis till midnight of this day
	 * @return
	 */
	public static String getMidNightTimeAsString() {
		return String.valueOf(getMidNightTimeAsLong());
	}
	
	/**
	 * Get time in millies as {@link String}for fifteen minute interval
	 * @return
	 */
	public static String getIntervalfifteenMinutesAsString(int hour, int quadrant) {		
		return String.valueOf(getIntervalfifteenMinutesAsLong(hour, quadrant));
	}
	
	/**
	 * Get time in millis as long till midnight of this day
	 * @return
	 */
	public static long getMidNightTimeAsLong() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		return cal.getTimeInMillis();
	}
	
	/**
	 * Get time in millies for fifteen minute interval
	 * @return
	 */
	public static long getIntervalfifteenMinutesAsLong(int hour, int quadrant) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 15);
		cal.set(Calendar.SECOND, 0);
		
		return cal.getTimeInMillis();
	}
	
	/**
	 * Get current time in millis as {@link String}
	 * @return
	 */
	public static String getCurrentTimeInMillis() {
		return String.valueOf(System.currentTimeMillis());
	}
	
	
}
