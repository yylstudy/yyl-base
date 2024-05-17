package com.cqt.cdr.cloudccsfaftersales.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;


@Slf4j
public class SFUtils {
    //配置接口访问密钥  生产环境配置zstoken   测试环境配置testtoken
    public static String testtoken = "4c5d80e607cd9e26ac8fd954c9fdbab7";
    public static String zstoken = "E532ADC266440127B76DA10129CD6386";
    public static String token = zstoken;

    /**
     * 顺丰加密校验
     */
    public static Boolean checkexpress(String expresskey) {
        Boolean flag = false;
        Calendar calendar = Calendar.getInstance();
        calendar.add(calendar.SECOND, 1);
        for (int i = 0; i < 1800; i++) {
            calendar.add(calendar.SECOND, -1);
            String timestamp = DateUtils.getDate14FromDate(calendar.getTime());
            String checkpw = Sha256.Encrypt("sf-express", token, timestamp);
            if (expresskey.equals(checkpw)) {
                flag = true;
                break;
            }
        }
        calendar.add(calendar.SECOND, 1800);
        calendar.add(calendar.SECOND, -1);
        for (int i = 0; i < 1800; i++) {
            calendar.add(calendar.SECOND, 1);
            String timestamp = DateUtils.getDate14FromDate(calendar.getTime());
            String checkpw = Sha256.Encrypt("sf-express", token, timestamp);
            if (expresskey.equals(checkpw)) {
                flag = true;
                break;
            }
        }
        return flag;
    }


    /**
     * 修改备注：   获取request中的json字符串
     * 陈星宇
     */
    public static String getJsonByRequest(HttpServletRequest request) {
        String receiveJsonStr = null;
        BufferedReader br = null;
        StringBuffer stringBuff = null;
        try {
            br = new BufferedReader(new InputStreamReader((ServletInputStream) request.getInputStream(), "UTF-8"));
            stringBuff = new StringBuffer();
            String line = null;

            while ((line = br.readLine()) != null) {
                stringBuff.append(line);
            }
        } catch (Exception e) {
            log.error("获取请求报文失败！:", e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.error("BufferedReader关闭失败:", e);
                }
            }
        }
        if (null != stringBuff) {
            receiveJsonStr = stringBuff.toString();
        }
        log.info("获取到的请求报文：" + receiveJsonStr);
        return receiveJsonStr;
    }


    public static Boolean cheackseaid(String str) {
        for (int i = 0; i < str.length(); i++) {
            String bb = str.substring(i, i + 1);
            boolean cc = java.util.regex.Pattern.matches("[\u4E00-\u9FA5]", bb);
            if (cc == true) {
                return cc;
            }
        }
        return false;
    }
}
