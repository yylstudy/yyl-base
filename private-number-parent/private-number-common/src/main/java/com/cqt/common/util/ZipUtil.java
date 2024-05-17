package com.cqt.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author linshiqiang
 * @date 2021/9/30 15:21
 */
public class ZipUtil {

    public static String compress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out);
        gzip.write(str.getBytes());
        gzip.close();
        return out.toString("ISO-8859-1");
    }

    public static String uncompress(String str) throws IOException {
        if (str == null || str.length() == 0) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(str
                .getBytes(StandardCharsets.ISO_8859_1));
        GZIPInputStream gunzip = new GZIPInputStream(in);
        byte[] buffer = new byte[256];
        int n;
        while ((n = gunzip.read(buffer)) >= 0) {
            out.write(buffer, 0, n);
        }
        // toString()使用平台默认编码，也可以显式的指定如toString("GBK")
        return out.toString();
    }

    public static void main(String[] args) throws IOException {
        String json = "{\"areaCode\":\"0591\",\"bindId\":\"cqt-axb-1443467294229856256\",\"cityCode\":\"0591\",\"createTime\":\"2021-09-30 14:46:35\",\"enableRecord\":\"0\",\"expiration\":\"14400000\",\"expireTime\":\"2022-03-16 06:46:35\",\"maxDuration\":7200,\"requestId\":\"6ef3066b-f127-48ce-99bc-7a5e81abbeb7\",\"sign\":\"39942030-7998-431e-946e-8c71857c52ce\",\"telA\":\"18649760218\",\"telB\":\"18060555106\",\"telX\":\"13016739764\",\"ts\":\"1632984395865\",\"updateTime\":\"2021-09-30 14:46:35\",\"userData\":\"11111111111111111\",\"wholearea\":\"0\"}";
        String compress = compress(json);
        System.out.println(compress);
        System.out.println(compress.length());
        System.out.println("===========================");
        String uncompress = uncompress(compress);
        System.out.println(uncompress);
        System.out.println(uncompress.length());
    }
}
