package com.cqt.cdr.cloudccsfaftersales.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.springframework.util.StringUtils;

/**
 * 
 * @file    : DateUtils.java
 * @author  : 
 * @date    : 2008-9-18
 * @corp    : CINtel
 * @version : 1.0
 * @update  : pangzhigang at 2009-10-28
 */
public final class DateUtils {
	public static String date14Format = "yyyyMMddHHmmss";
	public static String date8Format = "yyyyMMdd";
	public static String time6Format = "HHmmss";

	public static final int ERA = 0;
    public static final int YEAR = 1;
    public static final int MONTH = 2;
    public static final int WEEK_OF_YEAR = 3;
    public static final int WEEK_OF_MONTH = 4;
    public static final int DATE = 5;
    public static final int DAY_OF_MONTH = 5;
    public static final int DAY_OF_YEAR = 6;
    public static final int DAY_OF_WEEK = 7;
    public static final int DAY_OF_WEEK_IN_MONTH = 8;
    public static final int AM_PM = 9;
    public static final int HOUR = 10;
    public static final int HOUR_OF_DAY = 11;
    public static final int MINUTE = 12;
    public static final int SECOND = 13;
    public static final int MILLISECOND = 14;
    public static final int ZONE_OFFSET = 15;
    public static final int DST_OFFSET = 16;
    public static final int FIELD_COUNT = 17;
    public static final int SUNDAY = 1;
    public static final int MONDAY = 2;
    public static final int TUESDAY = 3;
    public static final int WEDNESDAY = 4;
    public static final int THURSDAY = 5;
    public static final int FRIDAY = 6;
    public static final int SATURDAY = 7;
    public static final int JANUARY = 0;
    public static final int FEBRUARY = 1;
    public static final int MARCH = 2;
    public static final int APRIL = 3;
    public static final int MAY = 4;
    public static final int JUNE = 5;
    public static final int JULY = 6;
    public static final int AUGUST = 7;
    public static final int SEPTEMBER = 8;
    public static final int OCTOBER = 9;
    public static final int NOVEMBER = 10;
    public static final int DECEMBER = 11;
    public static final int UNDECIMBER = 12;

	public static Date getDateFromDate14(String date14) {
		if(date14 == null) {
			return null;
		}
		
		SimpleDateFormat format = new SimpleDateFormat(date14Format);
		
		try {
			return format.parse(date14);
		}
		catch (ParseException e) {
			//throw new RuntimeException(e);
			return null;
		}
	}
	public static Date getDateFromDate8Time6(String date8, String time6) {
		if(date8 == null||time6 == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(date14Format);
		try {
			return format.parse(date8+time6);
		}
		catch (ParseException e) {
			//throw new RuntimeException(e);
			return null;
		}
	}
	
	public static Date getDateFromDate8(String date8) {
		if(date8 == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(date8Format);
		try {
			return format.parse(date8);
		}
		catch (ParseException e) {
			//throw new RuntimeException(e);
			return null;
		}
	}
	
	public static Date getDateFromString(String date8, String outputFormat) {
		if(date8 == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(outputFormat);
		try {
			return format.parse(date8);
		}
		catch (ParseException e) {
			//throw new RuntimeException(e);
			return null;
		}
	}
	
	public static Date getDateFromTime6(String time6) {
		if(time6 == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(time6Format);
		try {
			return format.parse(time6);
		}
		catch (ParseException e) {
			//throw new RuntimeException(e);
			return null;
		}
	}

	public static String getDate14FromDate(Date date) {
		if(date == null) {
			return null;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(date14Format);
		String str = dateFormat.format(date);
		return str;
	}
	
	public static String getDate8FromDate(Date date) {
		if(date == null) {
			return null;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(date8Format);
		return dateFormat.format(date);
	}
	
	public static String getStringFromDate(Date date, String outputFormat) {
		if(date == null) {
			return null;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(outputFormat);
		return dateFormat.format(date);
	}

	public static String getTime6FromDate(Date date) {
		if(date == null) {
			return null;
		}
		SimpleDateFormat timeFormat = new SimpleDateFormat(time6Format);
		return timeFormat.format(date);
	}

	public static String dateToStr(Date date, String formatStr) {
		if(date == null) {
			return null;
		}
		SimpleDateFormat timeFormat = new SimpleDateFormat(formatStr);
		return timeFormat.format(date);
	}
	
	public static Date strToDate(String str, String formatStr) {
		if(str == null) {
			return null;
		}
		SimpleDateFormat format = new SimpleDateFormat(formatStr);
		try {
			return format.parse(str);
		}
		catch (ParseException e) {
			//throw new RuntimeException(e);
			return null;
		}
	}
	
	/**
	 * Get current time and format it to string yyyyMMddHHmmss
	 * 
	 * @return time string
	 */
	public final static String getCurrentTimeString(String format) {
		return getCalculateTimeStr(format, 0, 0);
	}

	public final static String getCurrentTimeString() {
		return getCalculateTimeStr("yyyyMMddHHmmss", 0, 0);
	}
	
    /**
     * 
     * @param format
     * @param field
     * @param amount
     * @return
     */
	public final static String getCalculateTimeStr(String format, int field, int amount) {
		Calendar cal = Calendar.getInstance();
		if(amount != 0) {
			cal.add(field, amount);
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(cal.getTime());
	}
	
	/**
	 * getCalculateTimeStr
	 * 
	 * @param date
	 * @param format
	 * @param field
	 * @param amount
	 * @return
	 */
	public final static String getCalculateTimeStr(Date date, String format, int field, int amount) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		//
		if(amount != 0) {
			cal.add(field, amount);
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(cal.getTime());
	}
	
	/**
	 * 
	 * @param dateStr
	 * @param inputFmt
	 * @param outputFmt
	 * @return
	 */
	public final static String formatTransfer(String dateStr, String inputFmt, String outputFmt) {
		Date date = strToDate(dateStr, inputFmt);
		return dateToStr(date, outputFmt);
	}
	/**
	 * getCalendarByDate
	 * @param date
	 * @return
	 * @author pangzhigang
	 */
	public final static Calendar getCalendarByDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}
    
    public final static String buildMonthTblName(String tblName, String dateTimeStr) {
        StringBuffer monthTblBuffer = new StringBuffer(tblName);
        if (StringUtils.hasText(dateTimeStr) && dateTimeStr.length() >= 6) {
            String currentMonth = DateUtils.getCurrentTimeString("yyyyMM");
            String reqMonth = dateTimeStr.substring(0, 6);
            if(!currentMonth.equals(reqMonth)) {
                monthTblBuffer.append("_").append(reqMonth);
            }
        }
        return monthTblBuffer.toString();
    }
    
    public final static String timeStamp2DtStr(String timeStamp, String dateFmtStr) {
        String rtnStr = "";
        if(StringUtils.hasLength(timeStamp)) {
            long longTimeStamp = 0;
            try {
                longTimeStamp = Long.valueOf(timeStamp);
                rtnStr = timeStamp2DtStr(longTimeStamp, dateFmtStr);
            }
            catch(Exception ex) {
                
            }
        }
        return rtnStr;
    }
    
    public final static String timeStamp2DtStr(long timeStamp, String dateFmtStr) {
        String rtnStr = "";
        if(timeStamp > 0) {
            SimpleDateFormat dtFmt = new SimpleDateFormat(dateFmtStr);
            rtnStr = dtFmt.format(timeStamp * 1000L);
        }
        return rtnStr;
    }
}
