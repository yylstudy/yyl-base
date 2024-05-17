package com.cqt.cdr.util;

import com.alibaba.nacos.common.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {
    public static String tables(String tablename, String yearmonth, String companycode) {
        StringBuilder str = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(tablename)
                .append("_")
                .append(companycode)
                .append("_")
                .append(yearmonth)
                .append(" LIKE ")
                .append("cloudcc_tmptable." + tablename)
                .append(";");
        return str.toString();
    }

    public static String date2Str(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("yyyyMMddHHmmss").format(date);
    }

    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds 精确到秒的字符串
     * @param format
     * @return
     */
    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return null;
        }
        if (format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds + "000")));
    }

    /**
     * 替换掉号码前缀
     *
     * @param num 号码
     * @return
     */
    public static String replaceNumPerfix(String num) {
        try {
            if (StringUtils.isEmpty(num)) {
                return num;
            }
            if (num.substring(0, 4).equals("+861")) {
                num = num.replaceFirst("\\+861", "1");
            }
            if (num.substring(0, 6).equals("+00861")) {
                num = num.replaceFirst("\\+00861", "1");
            }
            if (num.substring(0, 3).equals("+01")) {
                num = num.replaceFirst("\\+01", "1");
            }
            if ( num.substring(0, 3).equals("+86")) {
                num = num.replaceFirst("\\+86", "0");
            }
            if (num.substring(0, 5).equals("+0086")) {
                num = num.replaceFirst("\\+0086", "0");
            }
            if (num.substring(0, 2).equals("+0")) {
                num = num.replaceFirst("\\+0", "0");
            }
        } catch (Exception e) {
            return num;
        }
        return num;
    }
}
