package com.cqt.sms.util;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.InetAddress.getLocalHost;

/**
 * @author youngder
 */
@Log4j2
public class StringUtil {

    public static final int SMS_ONE=70;
    public static final int SMS_MULTI_ONE=67;



    /**
     * @param response
     */
    public static void responToClient(HttpServletResponse response, String data) {
        try {
            OutputStream outputStream = response.getOutputStream();
            //response.setHeader("content-type", "text/html;charset=UTF-8");// 通过设置响应头控制浏览器以UTF-8的编码显示数据，如果不加这句话，那么浏览器显示的将是乱码
            response.setHeader("content-type", "application/json");// 通过设置响应头控制浏览器以UTF-8的编码显示数据，如果不加这句话，那么浏览器显示的将是乱码
            byte[] dataByteArr = data.getBytes("UTF-8");// 将字符转换成字节数组，指定以UTF-8编码进行转换
            outputStream.write(dataByteArr);// 使用OutputStream流向客户端输出字节数组
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //获取本地ip
    public static String getLocalIpStr() {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ipaddress = inetAddress.getHostAddress().toString();
                            if (!ipaddress.contains("::") && !ipaddress.contains("0:0:") && !ipaddress.contains("fe80")) {
                                ip = ipaddress;
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ip = "127.0.0.1";
            log.error ("获取本地ip失败",ex);
        }
        return ip;
    }

    /**
     * 修改人：	@author youngder
     * 修改时间：	2018-12-06 下午02:14:52
     * 修改备注：   判断字符串不为空
     */
    public static boolean isNotEmpty(String str) {
        if (str == null || "".equals(str) || str.length() == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 修改人：	@author fat boy y
     * 修改时间：	2018-12-06 下午02:14:52
     * 修改备注：   判断字符串为空
     */
    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str) || str.length() == 0) {
            return true;
        } else {
            return false;
        }
    }









    /**
     * 生成sign
     * @param params json报文转map
     * @param vccId  企业标识
     * @param secretKey 密钥
     * @return
     */
    public static String createSign(Map<String, Object> params, String vccId, String secretKey) {
        params.remove("appkey");
        params.remove("sign");
        params.remove("vcc_id");
        List<String> paramList = new ArrayList<>();
        params.forEach((key, value) -> {
            if (ObjectUtil.isNotEmpty(value)) {
                paramList.add(key + "=" + StrUtil.removeAll(value.toString(), CharUtil.CR, CharUtil.LF, CharUtil.SPACE));
            }
        });
        paramList.add("secret_key=" + secretKey);
        return SecureUtil.md5(String.join("&", paramList)).toUpperCase();
    }

    /**
     * 计算短信条数
     * rule:
     * 1、短信字数<=70个字数，按照70个字数一条短信计算
     * 2、短信字数>70个字数，即为长短信，按照67个字数记为一条短信计算
     * **/
    public static int getSmsNumber(String smsContent){
        int length=smsContent.length();
        if (length<=SMS_ONE){
            return 1;
        }
        int smsNumber=length/SMS_MULTI_ONE+1;
        log.info("共{}条短信，{}个字符",smsNumber,length);
        return smsNumber;
    }
}
