package com.cqt.ivr.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {
    public final static String YEAR_MONTH_DAY = "yyyy-MM-dd";
    public final static String HOUR_MINUTE_SECOND = "HH:mm:ss";


    /**
     * 根据日期获取星期几。0代表星期日，1到6分别代表星期一到星期六
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static String acquisitionWeek(String date) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFm = new SimpleDateFormat(YEAR_MONTH_DAY);
        Date d = dateFm.parse(date);
        calendar.setTime(d);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        return dayOfWeek + "";
    }

    /**
     * 判断时间是否在开始时间与结束时间之间
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param time      要比较的时间
     * @param format    时间类型
     * @return 结果
     * @throws ParseException
     */
    public static boolean validityTime(String startTime, String endTime, String time, String format) throws ParseException {
        //比较时间大小
        DateFormat df = new SimpleDateFormat(format);
        Date st = df.parse(startTime); //转换为 date 类型
        Date et = df.parse(endTime);
        Date t = df.parse(time);
        return t.getTime() >= st.getTime() && t.getTime() <= et.getTime();
    }

    /**
     * 比较日期是否在另一个日期的前面
     *
     * @param compare  要比较的时间
     * @param compared 被比较的时间
     * @param format   时间类型
     * @return 结果
     * @throws ParseException
     */
    public static boolean isFuture(String compare, String compared, String format) throws ParseException {
        //比较时间大小
        DateFormat df = new SimpleDateFormat(format);
        Date d = df.parse(compared);
        Date d2 = df.parse(compare);
        return d2.getTime() <= d.getTime();
    }
}
