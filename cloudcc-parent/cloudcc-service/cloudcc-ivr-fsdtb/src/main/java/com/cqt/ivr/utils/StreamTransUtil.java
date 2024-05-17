package com.cqt.ivr.utils;

import lombok.Cleanup;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigInteger;

public class StreamTransUtil {


    /**
     * 字符串转16进制
     * @param str
     * @return
     */
    public static String convertStringToHex(String str) {
        StringBuilder stringBuilder = new StringBuilder();

        char[] charArray = str.toCharArray();

        for (char c : charArray) {
            String charToHex = Integer.toHexString(c);
            stringBuilder.append(charToHex);
        }
        return stringBuilder.toString();
    }

    /**
     * @param hexString 16进制格式的字符串
     * @return 转换后的字节数组
     * @auther ck
     * @time 2019-05-24
     * <p>
     * 16进制的字符串表示转成字节数组
     **/
//    public static byte[] toByteArray(String hexString) {
//        //if (StringUtil.isEmpty(hexString))
//        //	throw new IllegalArgumentException("this hexString must not be empty");
//
//        hexString = hexString.toLowerCase();
//        final byte[] byteArray = new byte[hexString.length() / 2];
//        int k = 0;
//        for (int i = 0; i < byteArray.length; i++) {//因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
//            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
//            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
//            byteArray[i] = (byte) (high << 4 | low);
//            k += 2;
//        }
//        return byteArray;
//    }

    /**
     * @param byteArray 需要转换的字节数组
     * @return 16进制表示格式的字符串
     * @auther ck
     * @time 2019-05-24
     * <p>
     * 字节数组转成16进制表示格式的字符串
     **/
    public static String toHexString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1){
            throw new IllegalArgumentException("this byteArray must not be null or empty");
        }
        final StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteArray.length; i++) {
            if ((byteArray[i] & 0xff) < 0x10){//0~F前面不零
                hexString.append("0");
            }
            hexString.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return hexString.toString().toLowerCase();
    }

    public static String binary(byte[] bytes){
        return new BigInteger(1, bytes).toString(8);// 这里的1代表正数
    }
    /**
     * @auther ck
     * @time 2019-05-25
     * <p>
     * byte数组转换为二进制字符串,每个字节以","隔开
     **/
    public static String byteArrToBinStr(byte[] b) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            result.append(Long.toString(b[i] & 0xff, 2) + ",");
        }
        return result.toString().substring(0, result.length() - 1);
    }

    /**
     * @auther ck
     * @time 2019-05-25
     * <p>
     * 二进制字符串转换为byte数组,每个字节以","隔开
     **/
    public static byte[] binStrToByteArr(String binStr) {
        String[] temp = binStr.split(",");
        byte[] b = new byte[temp.length];
        for (int i = 0; i < b.length; i++) {
            b[i] = Long.valueOf(temp[i], 2).byteValue();
        }
        return b;
    }

    /**
     * @param buf 需要转换的字节数组
     * @return 转换后的字节流
     * @auther ck
     * @time 2019-05-24
     * <p>
     * 字节数组转成字节流
     **/
    public static final InputStream byte2Input(byte[] buf) {
        return new ByteArrayInputStream(buf);
    }

    /**
     * @param inStream 需要转换的字节流
     * @return 转换后的字节数组
     * @auther ck
     * @time 2019-05-24
     * <p>
     * 字节流转成字节数组
     **/
    public static final byte[] input2byte(InputStream inStream)
            throws IOException {
        @Cleanup
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

//    public static void main(String[] args) {
//        InputStream inputStream = null;
//        BufferedReader br = null;
//        String path = "d:/unboundException.wav";
//        try {
//            inputStream = new FileInputStream(path);
//            byte[] imgBytes = input2byte(inputStream);
//
//            String data= StreamTransUtil.toHexString(imgBytes);
//
//            byte[] bytes = StreamTransUtil.toByteArray(data);
//
//            //FileUtils.writeFile("d:/20200318.wav",bytes);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    /**
     * 将文件转成字符串
     * @param
     * @return
     */
    public static  String getFileSwitchStr(MultipartFile file){
        String data="";
        try {
            byte[] imgBytes = input2byte(file.getInputStream());
             data= StreamTransUtil.toHexString(imgBytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

}
