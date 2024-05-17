package com.cqt.ivr.utils;

import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * String字符串处理
 *
 * @author xys
 * @version 2017-9-19
 */

public final class StringUtil {


//    public final static String pool = "pool_";
//    public static final String FSBCSMP4 = "fsbcsmp4";
//    public static final String NJICCPGUI = "njiccpgui";
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(StringUtil.class);


    private StringUtil() {

    }

    /**
     * @Description(获取request内容) @param request
     * @return
     */
    public static String getReqMsg(HttpServletRequest request) {
        InputStream in;
        StringBuffer json = null;
        try {
            in = request.getInputStream();
            json = new StringBuffer();
            byte[] b = new byte[4096];
            for (int n; (n = in.read(b)) != -1;) {
                json.append(new String(b, 0, n));
            }
            String msg = json.toString();
            if (msg.indexOf("text=\"") > -1) {
                msg = msg.substring(0, msg.indexOf("text=\"") + 5) + "\\\""
                        + msg.substring(msg.indexOf("text=\"") + 6, msg.indexOf("\"\"")) + "\\\"\""
                        + msg.substring(msg.indexOf("\"\"") + 2, msg.length());
            }
            return msg;
        } catch (Exception e) {
            return "";
        }
    }

    public static String Encrypt(String strSrc, String encName) {
        MessageDigest md = null;
        String strDes = null;
        byte[] bt = strSrc.getBytes();
        try {
            md = MessageDigest.getInstance(encName);
            md.update(bt);
            strDes = bytes2Hex(md.digest()); // to HexString
        } catch (NoSuchAlgorithmException e) {
            log.error("Encrypt异常:",e);
            return null;
        }
        return strDes;
    }

    public static String bytes2Hex(byte[] bts) {
        String des = "";
        String tmp = null;
        for (int i = 0; i < bts.length; i++) {
            tmp = (Integer.toHexString(bts[i] & 0xFF));
            if (tmp.length() == 1) {
                des += "0";
            }
            des += tmp;
        }
        return des;
    }

    /*
     * 将时间转换为时间戳
     */
    public static String dateToStamp(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = simpleDateFormat.parse(s);
            long ts = date.getTime();
            res = String.valueOf(ts);
        } catch (ParseException e) {
            res = "";
        }
        return res;
    }

    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }


    /**
     * check the given CharSequence is neither {@code null} nor of length 0.
     *
     *
     * <pre>
     * 	StringTool.hasLength(null) = false
     * StringTool.hasLength("") = false
     * StringTool.hasLength(" ") = true
     * StringTool.hasLength("Hello") = true
     * </pre>
     *
     * @param str the String to check (may be {@code null})
     * @return {@code true} if the String is not null and has length
     */
    public static boolean hasLength(final CharSequence str) {
        return (str != null && str.length() > 0);
    }

    /**
     * check str whether a String real value . contains at least one
     * non-whitespace character , and length greate then 0 and str not
     * {@code null} return {@code true}.
     *
     * @param str the String to check (may be {@code null})
     *            // * @see #hasTest(CharSequence)
     */
    public static boolean hasCharSequence(final String str) {
        return CharSequence((CharSequence) str);
    }

    /**
     * check str whether a CharSequence real value . contains at least one
     * non-whitespace character , and length greate then 0 and str not
     * {@code null} return {@code true}
     *
     *
     * <pre>
     * StringTool.hasText(null) = false
     * StringTool.hasText("") = false
     * StringTool.hasText(" ") = false
     * StringTool.hasText("12345") = true
     * StringTool.hasText(" 12345 ") = true
     * </pre>
     *
     * @param str the CharSequence to check (may be {@code null})
     * @return if CharSequence has real value , contains at least one
     * non-whitespace character , and length greate then 0 and str not
     * {@code null} return {@code true}
     */
    public static boolean CharSequence(final CharSequence str) {
        if (!hasLength(str)) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * if str is neither {@code null} nor of length 0. trim the string in front
     * of the str
     *
     *
     * <pre>
     * StringTool.trimLeadingWhitespace(" a b c ") = "a b c ";
     * </pre>
     *
     * @param str the String to trim (may be {@code null})
     * @return the trimmed String
     * @see Character#isWhitespace(char)
     */
    public static String trimLeadingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    /**
     * if str is neither {@code null} nor of length 0. trim the end of the str.
     *
     *
     * <pre>
     * StringTool.trimTrailingWhitespace(" a b c ") = " a b c";
     * </pre>
     *
     * @param str the String to trim (may be {@code null})
     * @return the trimmed String
     * @see Character#isWhitespace(char)
     */
    public static String trimTrailingWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * @param @param str @param @return @throws
     * @Title: trimWhitespace @Description: TODO 去字符串前后空格
     *
     * <pre>
     * StringTool.trimWhitespace(" abc ") = "abc"
     * StringTool.trimWhitespace("abc ") = "abc"
     * StringTool.trimWhitespace(" abc") = "abc"
     *         </pre>
     */
    public static String trimWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
            sb.deleteCharAt(0);
        }
        while (sb.length() > 0 && Character.isWhitespace(sb.charAt(sb.length() - 1))) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * if str is neither {@code null} nor of length 0. trim all of the str.
     *
     *
     * <pre>
     * StringTool.trimTrailingWhitespace(" a b c ") = "abc";
     * </pre>
     *
     * @param str the String to trim (may be {@code null})
     * @return the trimmed String
     * //	 * @see StringTool#hasLength(String)
     * @see Character#isWhitespace(char)
     */
    public static String trimAllWhitespace(String str) {
        if (!hasLength(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        int index = 0;
        while (sb.length() > index) {
            if (Character.isWhitespace(sb.charAt(index))) {
                sb.deleteCharAt(index);
            } else {
                index++;
            }
        }
        return sb.toString();
    }

    /**
     * str Check by regStr string
     *
     * @param str    the String to matcher (may be {@code null})
     * @param regStr Regular Expression
     * @return verification of the string
     * @see #{@link java.util.regex.Matcher}
     * @see #{@link java.util.regex.Pattern}
     */
    public static boolean match(String str, String regStr) {
        Pattern pattern = Pattern.compile(regStr, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * seacrch regStr from str , if str has the same as regStr , replace it to
     * new Str StringTools.replaceAll("abcabcabc","a","d") = "dbcdbcdbc";
     *
     * @param str    the String to search (may be {@cade null})}
     * @param regStr Regular Expression (may be {@cade null})
     * @param newStr replace the String to str (may be {@code null})
     * @return Replacement string
     * @see #{@link java.util.regex.Matcher}
     * @see #{@link java.util.regex.Pattern}
     */
    public static String replaceAll(String str, String regStr, String newStr) {
        Pattern pattern = Pattern.compile(regStr, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll(newStr);
    }

    /**
     * Split text as required of the regStr
     *
     * @param text   the String to split is (may be {@code null})
     * @param regStr Segmentation marked (may be {@code null})
     * @return String[]
     * @see #{@link java.util.regex.Pattern}
     */
    public static String[] split(String text, String regStr) {
        Pattern pattern = Pattern.compile(regStr, Pattern.CASE_INSENSITIVE);
        return pattern.split(text);
    }

    public static void log(String message) {
        //		System.out.println("CurrentId : " + Thread.currentThread().getId() + " At " + DateUtil.getDateTypeAFormat() + " " + message);
    }

    /**
     * @author xinson
     * @Title: getDate
     */
    public static String getDate(String format){
        Date date = new Date();
        if (UT.zstr(format)){
            format = "yyyyMMddHHmmss";
        }
        SimpleDateFormat sdf=new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 修改人：	@author ck
     * 修改时间：2017-9-4  3:16:52
     * 修改备注：  判断字符串是否为空
     */
    public static boolean isNotEmpty(String str) {
        if (str == null || "".equals(str) || str.length() == 0) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * 修改人：	@author ck
     * 修改时间：2017-9-4  3:16:52
     * 修改备注：  判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str) || str.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取acrcallid
     * @param callUUID
     * @param callStartTime
     * @return
     */
    public static String getAcrCallId(String callUUID, String callStartTime) {
        StringBuffer sb = new StringBuffer();
        sb.append("C" + callStartTime + "AC" + callUUID.replaceAll("-", "").substring(0, 16));
        return sb.toString();

    }

    /**
     * 获取Streamnumber
     * @param callUUID
     * @param callEndTime
     * @return
     */
    public static String getStreamnumber(String callUUID, String callEndTime) {
        StringBuffer sb = new StringBuffer();
        sb.append(callEndTime + "AC" + callUUID.replaceAll("-", "").substring(0, 16));
        return sb.toString();

    }

    /**
     * 获取acrcallid
     * @param callUUID
     * @param callStartTime
     * @return
     */
    public static String getSid(String callUUID, String callStartTime) {
        StringBuffer sb = new StringBuffer();
        sb.append("S" + callStartTime + callUUID.replaceAll("-", "").substring(0, 22));
        return sb.toString();

    }


    /**
     * @author youngder
     * @Title: getDate
     * @Description: 生成指定格式的时间
     */
    public static String getRegDate(String regular) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(regular);
        return sdf.format(date);
    }

    /**
     * 获取当前时间  年月日时分秒
     *
     * @return
     */
    public static String getTime() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 判断是否为直辖市
     *
     * @param province
     * @return
     */
    public static boolean isUnder(String province) {
        if ("北京".equals(province) || "天津".equals(province) || "上海".equals(province) || "重庆".equals(province)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取当前时间  年月日时分秒
     *
     * @return
     */
    public static String getLastTenMin() {
        Date now = new Date();
        Date now_10 = new Date(now.getTime() - 600000); //10分钟前的时间
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//可以方便地修改日期格式
        String nowTime_10 = dateFormat.format(now_10);
        return nowTime_10;
    }


    /**
     * 获取当前时间的前一个小时
     */
    public static String beforeOneHourToNowDate() {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) - 1);
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        return df.format(calendar.getTime());
    }

    public static String getDate14FromDate(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String str = dateFormat.format(date);
        return str;
    }


    /**
     * @author ck
     * 去除所有空格
     */
    public static String removeSpace(String str) {
        if (isNotEmpty(str)) {
            return str.replace(" ", "");
        } else {
            return "";
        }
    }

    /**
     * @author ck
     * @Title: getRegDateadd8
     * @Description: 生成当前时间8小时后的时间
     */
    public static String getRegDateadd8(long time, long distance, String regular) {
        long currentTime = time;
        currentTime += distance;
        Date date = new Date(currentTime);
        SimpleDateFormat sdf = new SimpleDateFormat(regular);
        return sdf.format(date);
    }

    /**
     * @return
     * @Description(去掉换行，空格，制表符) @param dest
     */
    public static String deleteN(String dest) {
        Pattern p = Pattern.compile("\\s");
        Matcher m = p.matcher(dest);
        dest = m.replaceAll("");
        return dest;
    }


    public static String getTime(String user_time,String formart) {
        String re_time = null;
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfs = new SimpleDateFormat(formart);
        Date d;
        try {
            d = sdf.parse(user_time);
            long l = d.getTime();
            re_time = String.valueOf(l);
//            re_time = str.substring(0, 10);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            log.error("getTime异常:",e);
        }
        long lcc_time = Long.valueOf(re_time);
//        re_StrTime = sdfs.format(new Date(lcc_time * 1000L));
        re_StrTime = sdfs.format(new Date(lcc_time));
        return re_StrTime;
    }

    public static String getEffectTime(String user_time,String formart, Integer effctTime) {
        String re_time = null;
        String re_StrTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfs = new SimpleDateFormat(formart);
        Date d;
        try {
            d = sdf.parse(user_time);
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            //这是几个小时之后
            c.add(Calendar.HOUR_OF_DAY, effctTime);
            Date tomorrow = c.getTime();

            long l = tomorrow.getTime();
            re_time = String.valueOf(l);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            log.error("getEffectTime异常:",e);
        }
        long lcc_time = Long.valueOf(re_time);
        re_StrTime = sdfs.format(new Date(lcc_time));
        return re_StrTime;
    }

    public static String getTime(String user_time) {
        String re_time = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d;
        try {
            d = sdf.parse(user_time);
            long l = d.getTime();
            re_time = String.valueOf(l);
//            re_time = str.substring(0, 10);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            log.error("getTime异常:",e);
        }
//        long lcc_time = Long.valueOf(re_time) * 1000L;
        long lcc_time = Long.valueOf(re_time);
        return lcc_time + "";
    }

    /**
     * 时间戳转换成日期格式字符串
     * @param seconds 精确到秒的字符串
     * @param format
     * @return
     */
    public static String timeStamp2Date(String seconds,String format) {
        if(seconds == null || seconds.isEmpty() || seconds.equals("null")){
            return "";
        }
        if(format == null || format.isEmpty()){
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds+"000")));
    }
    /**
     * 日期格式字符串转换成时间戳
     * @param date_str 字符串日期
     * @param format 如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2TimeStamp(String date_str,String format){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date_str).getTime()/1000);
        } catch (Exception e) {
            log.error("date2TimeStamp异常:",e);
        }
        return "";
    }

    /**
     * 取得当前时间戳（精确到秒）
     * @return
     */
    public static String timeStamp(){
        long time = System.currentTimeMillis();
        String t = String.valueOf(time/1000);
        return t;
    }

    /**
     * @author xinson
     * @Title: caseTime
     * @Description: 获取yyyy-MM-dd
     */
    public static String caseTimeyyyyMMdd(){
        Date date = new Date();
        String changeDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
        return changeDate;
    }

    /**
     * @author xinson
     * @Title: caseTime
     * @Description: 获取昨天yyyy-MM-dd
     */
    public static String caseTimeyyyyMMddYd(){
        Date date=new Date();//取时间

        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);

        calendar.add(calendar.DATE,-1);//把日期往前减少一天，若想把日期向后推一天则将负数改为正数

        date=calendar.getTime();
        String changeDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
        return changeDate;
    }


    /**
     * 获取上个月月份
     * @return
     */
    public static final String getLastMonth() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMM");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        // 设置为当前时间
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        // 设置为上一个月
        //calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        date = calendar.getTime();
        return format.format(date);
    }

    /**
     * 修改人：	@author Lyhace
     * 修改时间：	2016-2-24 下午03:10:46
     * 修改备注：  判断是否为数字
     */
    public static boolean isNumber(String str) {
        if(str == null || "".equals(str)){
            return false;
        }else{
            for (int i = 0; i < str.length(); i++) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
    }
}
