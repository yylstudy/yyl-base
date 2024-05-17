package com.cqt.common.util;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.ThirdConstant;
import com.cqt.common.enums.*;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 工具处理类
 *
 * @author dingsh
 * @date 2022/07/27
 */
public class ThirdUtils {


    /**
     * (hdh) GNFlag 平台字段转化
     */
    public static String modelExchange(Integer model) {
        if (ThirdConstant.MODEL_AB.equals(model)) {
            return "11";
        }
        return "00";
    }

    /**
     * (hdh)号码判断
     **/
    public static String numberType(String number) {
        if (Pattern.matches(ThirdConstant.REGEX_95, number)) {
            return ThirdConstant.NUMBER_95;
        } else if (Pattern.matches(ThirdConstant.REGEX_400, number)) {
            return ThirdConstant.NUMBER_400;
        } else if (Pattern.matches(ThirdConstant.REGEX_FIXED, number)) {
            return ThirdConstant.NUMBER_FIXED;
        }
        return ThirdConstant.NUMBER_XH;
    }

    /**
     * (hdh) 86
     */
    public static String parseNumber(String number, String type, Integer areaCode) {
        if (ThirdConstant.REGEX_XH.equals(type)) {
            return "86" + number;
        } else if (ThirdConstant.REGEX_FIXED.equals(type)) {
            return "86" + areaCode + number;
        }
        return "86" + number;
    }

    /**
     * 添加秒数
     **/
    public static Date plusSeconds(LocalDateTime localDateTime, long seconds) {

        localDateTime = localDateTime.plusSeconds(seconds);
        return asDate(localDateTime);

    }

    /**
     * LocalDateTime2Date
     **/
    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }


    /**
     * acrCallId 创建
     **/
    public static String acrCallId(String startTime) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        return "C" + startTime + uuid;

    }

    /**
     * videoCallFlag 构建
     **/
    public static String videoCallFlag(String recordUrl, Integer callDuration) {
        if (StringUtils.isNotBlank(recordUrl)) {
            return String.valueOf(callDuration);
        }
        return "0";
    }

    /**
     * (通话)结束码和结束理由
     **/
    public static CallResultCodeEnum callResultCode(String finishState) {
        if ("1".equals(finishState)) {
            return CallResultCodeEnum.one;
        }
        if ("2".equals(finishState)) {
            return CallResultCodeEnum.one;
        }
        if ("3".equals(finishState)) {
            return CallResultCodeEnum.nine;
        }
        if ("4".equals(finishState)) {
            return CallResultCodeEnum.four;
        }
        if ("5".equals(finishState)) {
            return CallResultCodeEnum.two;
        }
        if ("11".equals(finishState)) {
            return CallResultCodeEnum.five;
        }
        if ("12".equals(finishState)) {
            return CallResultCodeEnum.seven;
        }
        if ("13".equals(finishState)) {
            return CallResultCodeEnum.eleven;
        }
        if ("14".equals(finishState)) {
            return CallResultCodeEnum.six;
        }
        return CallResultCodeEnum.ninety_nine;
    }

    /**
     * (通话)结束码和结束理由
     **/
    public static HdhResultCodeEnum hdhResultCode(String finishState) {
        if ("1".equals(finishState)) {
            return HdhResultCodeEnum.one;
        }
        if ("2".equals(finishState)) {
            return HdhResultCodeEnum.two;
        }
        if ("3".equals(finishState)) {
            return HdhResultCodeEnum.three;
        }
        if ("4".equals(finishState)) {
            return HdhResultCodeEnum.four;
        }
        if ("5".equals(finishState)) {
            return HdhResultCodeEnum.five;
        }
        if ("6".equals(finishState)) {
            return HdhResultCodeEnum.six;
        }
        if ("7".equals(finishState)) {
            return HdhResultCodeEnum.seven;
        }
        if ("8".equals(finishState)) {
            return HdhResultCodeEnum.eight;
        }
        if ("9".equals(finishState)) {
            return HdhResultCodeEnum.nine;
        }
        if ("10".equals(finishState)) {
            return HdhResultCodeEnum.ten;
        }
        if ("11".equals(finishState)) {
            return HdhResultCodeEnum.eleven;
        }
        if ("12".equals(finishState)) {
            return HdhResultCodeEnum.twelve;
        }
        if ("13".equals(finishState)) {
            return HdhResultCodeEnum.thirteen;
        }
        if ("14".equals(finishState)) {
            return HdhResultCodeEnum.fourteen;
        }
        if ("21".equals(finishState)) {
            return HdhResultCodeEnum.twenty_one;
        }
        return HdhResultCodeEnum.fifty;
    }

    /**
     * (通话)结束码和结束理由
     **/
    public static ResultCodeEnum cnResultCode(Integer finishState) {
        if (finishState == 1) {
            return ResultCodeEnum.one;
        }
        if (finishState == 2) {
            return ResultCodeEnum.two;
        }
        if (finishState == 3) {
            return ResultCodeEnum.three;
        }
        if (finishState == 4) {
            return ResultCodeEnum.four;
        }
        if (finishState == 5) {
            return ResultCodeEnum.five;
        }
        if (finishState == 6) {
            return ResultCodeEnum.six;
        }
        if (finishState == 7) {
            return ResultCodeEnum.seven;
        }
        if (finishState == 8) {
            return ResultCodeEnum.eight;
        }
        if (finishState == 9) {
            return ResultCodeEnum.nine;
        }
        if (finishState == 10) {
            return ResultCodeEnum.ten;
        }
        if (finishState == 11) {
            return ResultCodeEnum.eleven;
        }
        if (finishState == 12) {
            return ResultCodeEnum.twelve;
        }
        if (finishState == 91) {
            return ResultCodeEnum.ninetyOne;
        }
        if (finishState == 20) {
            return ResultCodeEnum.twenty;
        }
        return ResultCodeEnum.ninetyNine;
    }

    /**
     * (短信)结束码和结束理由
     **/
    public static SmsResultCodeEnum callSmsResultCode(String finishState) {
        if ("1".equals(finishState)) {
            return SmsResultCodeEnum.zero;
        }
        if ("19".equals(finishState)) {
            return SmsResultCodeEnum.three;
        }
        if ("20".equals(finishState)) {
            return SmsResultCodeEnum.one;
        }
        if ("21".equals(finishState)) {
            return SmsResultCodeEnum.one;
        }
        return SmsResultCodeEnum.ninety_nine;
    }

    /**
     * (短信)结束码和结束理由
     **/
    public static HdhSmsResultCodeEnum hdhSmsResultCode(String finishState) {
        if ("1".equals(finishState)) {
            return HdhSmsResultCodeEnum.one;
        }
        if ("2".equals(finishState)) {
            return HdhSmsResultCodeEnum.two;
        }
        if ("3".equals(finishState)) {
            return HdhSmsResultCodeEnum.three;
        }
        if ("4".equals(finishState)) {
            return HdhSmsResultCodeEnum.four;
        }
        if ("5".equals(finishState)) {
            return HdhSmsResultCodeEnum.five;
        }
        if ("6".equals(finishState)) {
            return HdhSmsResultCodeEnum.six;
        }
        if ("7".equals(finishState)) {
            return HdhSmsResultCodeEnum.seven;
        }
        if ("8".equals(finishState)) {
            return HdhSmsResultCodeEnum.eight;
        }
        if ("9".equals(finishState)) {
            return HdhSmsResultCodeEnum.nine;
        }
        if ("19".equals(finishState)) {
            return HdhSmsResultCodeEnum.nineteen;
        }
        if ("20".equals(finishState)) {
            return HdhSmsResultCodeEnum.twenty;
        }

        return HdhSmsResultCodeEnum.twenty_one;

    }

    public static SmsResultCodeEnum smsResultCodeEnum(String smsResult) {
        if ("1".equals(smsResult)) {
            return SmsResultCodeEnum.one;
        }
        if ("2".equals(smsResult)) {
            return SmsResultCodeEnum.two;
        }
        if ("3".equals(smsResult)) {
            return SmsResultCodeEnum.three;
        }
        if ("4".equals(smsResult)) {
            return SmsResultCodeEnum.four;
        }
        if ("5".equals(smsResult)) {
            return SmsResultCodeEnum.five;
        }

        return SmsResultCodeEnum.ninety_nine;
    }

    /**
     * (hdh 时间为空 使用通话发生时间callTime)
     **/
    public static String parseTime(String time, String callTime) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        if (StringUtils.isNotBlank(time)) {
            return time + uuid;
        }
        return callTime + uuid;
    }

    public static String parseNetTime(String time, String callTime) {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        if (StringUtils.isNotBlank(time)) {

            String format = dateFormat.format(time);
            return format + uuid;
        }

        return dateFormat.format(callTime) + uuid;
    }

    /**
     * (hdh 时间为空 使用通话发生时间callTime)
     **/
    public static String getTime(String time, String callTime) {
        if (StringUtils.isNotBlank(time)) {
            return time;
        }
        return callTime;
    }

    /**
     * 判断number是否86开头,是的话去掉前两位
     **/
    public static String getNumberUn86(String number) {
        if (StrUtil.isEmpty(number)){
            return number;
        }
        if (number.startsWith("86")) {
            return number.substring(2);
        }
        return number;
    }

    /**
     * 14 位时间搓 转化yyyy-MM-dd HH:mm:ss
     **/
    public static String timeStampTranfer(String timeStamp, String format, String format2) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        LocalDateTime ldt = LocalDateTime.parse(timeStamp, dtf);
        DateTimeFormatter fa = DateTimeFormatter.ofPattern(format2);
        return ldt.format(fa);

    }

    /**
     * 14 位时间搓 转化yyyy-MM-dd HH:mm:ss
     **/
    public static Date string2Date(String date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ThirdConstant.yyyy_MM_dd_HH_mm_ss);
        try {
            return simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * date2String
     **/
    public static String date2String(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(ThirdConstant.yyyy_MM_dd_HH_mm_ss);

        String str = format.format(date);

        return str;
    }

    /**
     * yyyyMMddHHmmss转换yyyy-MM-dd HH:mm:ss
     */
    public static String strToDateFormat(String date) throws ParseException {
        if (StrUtil.isEmpty(date)){
            return date;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(ThirdConstant.yyyy_MM_dd_HH_mm_ss);
        formatter.setLenient(false);
        Date newDate = formatter.parse(date);
        formatter = new SimpleDateFormat(ThirdConstant.yyyyMMddHHmmss);
        return formatter.format(newDate);
    }

    public static String convert(String dateTime) {
        if (StrUtil.isEmpty(dateTime)) {
            return "";
        }
        DateTime time = DateUtil.parse(dateTime, ThirdConstant.yyyy_MM_dd_HH_mm_ss);
        return DateUtil.format(time, ThirdConstant.yyyyMMddHHmmss);
    }

    /**
     * 去除实体空字符串
     **/
    public static <T> T trimBlankString(T o) {
        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(o));
        Set<String> set = map.keySet();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object values = map.get(key);
            if (values instanceof String) {
                values = ((String) values).trim();
                if (values.equals("")) {
                    values = null;
                }
            }
            map.put(key, values);
        }
        o = JSON.parseObject(JSON.toJSONString(map), (Type) o.getClass());
        return o;
    }

    /**
     * 时间日期转换
     *
     * @param strDate 字符串yyyyMMddHHmmss
     * @return 字符串yyyy-MM-dd HH:mm:ss
     */
    public static String strToDateLong(String strDate) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat(ThirdConstant.yyyyMMddHHmmss).parse(strDate);
            String str = new SimpleDateFormat(ThirdConstant.yyyy_MM_dd_HH_mm_ss).format(date);
            return str;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
