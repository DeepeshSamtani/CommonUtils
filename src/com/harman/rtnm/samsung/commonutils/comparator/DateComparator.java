package com.harman.rtnm.samsung.commonutils.comparator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.harman.rtnm.samsung.commonutils.util.StringUtils;

public class DateComparator implements Comparator<String> {

	SimpleDateFormat dateFormatter = null;
	
	public DateComparator(String pattern) {
		if(StringUtils.isNullOrEmpty(pattern)) {
			throw new IllegalArgumentException(" date format pattern is null ");
		}
		dateFormatter = new SimpleDateFormat(pattern);
	}

	@Override
	public boolean compare(String date1, String date2) {
		try {
			Date d1 = dateFormatter.parse(date1);
			Date d2 = dateFormatter.parse(date2);
			
			return (d1.compareTo(d2) >= 0) ? true : false;
		} catch (ParseException e) {
			System.out.println(e);
			return false;
		}
	}
}
