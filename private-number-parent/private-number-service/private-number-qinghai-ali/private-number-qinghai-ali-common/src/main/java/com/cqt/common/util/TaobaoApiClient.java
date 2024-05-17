package com.cqt.common.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.cqt.model.properties.TaobaoApiProperties;
import com.taobao.api.internal.util.TaobaoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * @author linshiqiang
 * date:  2023-01-28 10:28
 */
@Slf4j
@Component
@EnableConfigurationProperties({TaobaoApiProperties.class})
@RequiredArgsConstructor
public class TaobaoApiClient {

    private final TaobaoApiProperties taobaoApiProperties;

    private static final String SIGN_METHOD_MD5 = "md5";
    private static final String SIGN_METHOD_HMAC = "hmac";
    private static final String CHARSET_UTF8 = "utf-8";
    private static final String CONTENT_ENCODING_GZIP = "gzip";

    /**
     * 请求接口
     *
     * @param method        方法名
     * @param requestParams 业务参数
     * @return 结果
     */
    public Optional<String> callApi(String method, Map<String, String> requestParams) throws IOException {
        log.info("taobao requestParams: {}", requestParams);
        try (HttpResponse httpResponse = HttpRequest.post(taobaoApiProperties.getRequestUrl())
                .timeout(taobaoApiProperties.getTimeout())
                .formStr(getRequestParams(method, requestParams))
                .execute()) {
            if (httpResponse.isOk()) {
                log.error("taobao api: {} responseResult: {}", taobaoApiProperties.getRequestUrl(), httpResponse.body());
                return Optional.ofNullable(httpResponse.body());
            }
            log.error("taobao api: {} Request return not 200: {}", taobaoApiProperties.getRequestUrl(), httpResponse);
        }
        return Optional.empty();
    }

    public Optional<String> callApi(String url, String method, Map<String, String> requestParams) throws IOException {
        log.info("taobao requestParams: {}", requestParams);
        String reqUrl = StrUtil.isNotEmpty(url) ? url : taobaoApiProperties.getRequestUrl();
        Map<String, String> stringMap = getRequestParams(method, requestParams);
        log.info("请求taobao参数: {}", JSON.toJSONString(stringMap, true));
        try (HttpResponse httpResponse = HttpRequest.post(reqUrl)
                .timeout(taobaoApiProperties.getTimeout())
                .formStr(stringMap)
                .execute()) {
            if (httpResponse.isOk()) {
                log.info("taobao api: {} responseResult: {}", reqUrl, httpResponse.body());
                return Optional.ofNullable(httpResponse.body());
            }
            log.error("taobao api: {} Request return not 200: {}", reqUrl, httpResponse);
        }
        return Optional.empty();
    }

    private Map<String, String> getRequestParams(String method, Map<String, String> businessParams) throws IOException {
        Map<String, String> params = new HashMap<>(16);
        // 公共参数
        params.put("method", method);
        params.put("app_key", taobaoApiProperties.getAppKey());
        params.put("timestamp", DateUtil.now());
        params.put("format", taobaoApiProperties.getFormat());
        params.put("partner_id", "cqt");
        params.put("v", taobaoApiProperties.getVersion());
        params.put("sign_method", taobaoApiProperties.getSignMethod());
        // 业务参数
        params.putAll(businessParams);
        // 签名参数
        String sign = TaobaoUtils.signTopRequest(params, null, taobaoApiProperties.getAppSecret(), taobaoApiProperties.getSignMethod());
        params.put("sign", sign);
        return params;
    }

    /**
     * 对TOP请求进行签名。
     */
    private String signTopRequest(Map<String, String> params, String secret, String signMethod) throws IOException {
        // 第一步：检查参数是否已经排序
        String[] keys = params.keySet().toArray(new String[0]);
        Arrays.sort(keys);

        // 第二步：把所有参数名和参数值串在一起
        StringBuilder query = new StringBuilder();
        if (SIGN_METHOD_MD5.equals(signMethod)) {
            query.append(secret);
        }
        for (String key : keys) {
            String value = params.get(key);
            if (isNotEmpty(key) && isNotEmpty(value)) {
                query.append(key).append(value);
            }
        }

        // 第三步：使用MD5/HMAC加密
        byte[] bytes;
        if (SIGN_METHOD_HMAC.equals(signMethod)) {
            bytes = encryptHMAC(query.toString(), secret);
        } else {
            query.append(secret);
            bytes = encryptMD5(query.toString());
        }

        // 第四步：把二进制转化为大写的十六进制
        return byte2hex(bytes);
    }

    /**
     * 对字节流进行HMAC_MD5摘要。
     */
    private byte[] encryptHMAC(String data, String secret) throws IOException {
        byte[] bytes;
        try {
            SecretKey secretKey = new SecretKeySpec(secret.getBytes(CHARSET_UTF8), "HmacMD5");
            Mac mac = Mac.getInstance(secretKey.getAlgorithm());
            mac.init(secretKey);
            bytes = mac.doFinal(data.getBytes(CHARSET_UTF8));
        } catch (GeneralSecurityException gse) {
            throw new IOException(gse.toString());
        }
        return bytes;
    }

    /**
     * 对字符串采用UTF-8编码后，用MD5进行摘要。
     */
    private byte[] encryptMD5(String data) throws IOException {
        return encryptMD5(data.getBytes(CHARSET_UTF8));
    }

    /**
     * 对字节流进行MD5摘要。
     */
    private byte[] encryptMD5(byte[] data) throws IOException {
        byte[] bytes;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            bytes = md.digest(data);
        } catch (GeneralSecurityException gse) {
            throw new IOException(gse.toString());
        }
        return bytes;
    }

    /**
     * 把字节流转换为十六进制表示方式。
     */
    private String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }

    private String callApi(URL url, Map<String, String> params) throws IOException {
        String query = buildQuery(params, CHARSET_UTF8);
        byte[] content = {};
        if (query != null) {
            content = query.getBytes(CHARSET_UTF8);
        }

        HttpURLConnection conn = null;
        OutputStream out = null;
        String rsp = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Host", url.getHost());
            conn.setRequestProperty("Accept", "text/xml,text/javascript");
            conn.setRequestProperty("User-Agent", "top-sdk-java");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET_UTF8);
            out = conn.getOutputStream();
            out.write(content);
            rsp = getResponseAsString(conn);
        } finally {
            if (out != null) {
                out.close();
            }
            if (conn != null) {
                conn.disconnect();
            }
        }

        return rsp;
    }

    private String buildQuery(Map<String, String> params, String charset) throws IOException {
        if (params == null || params.isEmpty()) {
            return null;
        }

        StringBuilder query = new StringBuilder();
        Set<Map.Entry<String, String>> entries = params.entrySet();
        boolean hasParam = false;

        for (Map.Entry<String, String> entry : entries) {
            String name = entry.getKey();
            String value = entry.getValue();
            // 忽略参数名或参数值为空的参数
            if (isNotEmpty(name) && isNotEmpty(value)) {
                if (hasParam) {
                    query.append("&");
                } else {
                    hasParam = true;
                }

                query.append(name).append("=").append(URLEncoder.encode(value, charset));
            }
        }

        return query.toString();
    }

    private String getResponseAsString(HttpURLConnection conn) throws IOException {
        String charset = getResponseCharset(conn.getContentType());
        if (conn.getResponseCode() < 400) {
            String contentEncoding = conn.getContentEncoding();
            if (CONTENT_ENCODING_GZIP.equalsIgnoreCase(contentEncoding)) {
                return getStreamAsString(new GZIPInputStream(conn.getInputStream()), charset);
            } else {
                return getStreamAsString(conn.getInputStream(), charset);
            }
        } else {// Client Error 4xx and Server Error 5xx
            throw new IOException(conn.getResponseCode() + " " + conn.getResponseMessage());
        }
    }

    private String getStreamAsString(InputStream stream, String charset) throws IOException {
        try {
            Reader reader = new InputStreamReader(stream, charset);
            StringBuilder response = new StringBuilder();

            final char[] buff = new char[1024];
            int read = 0;
            while ((read = reader.read(buff)) > 0) {
                response.append(buff, 0, read);
            }

            return response.toString();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private String getResponseCharset(String ctype) {
        String charset = CHARSET_UTF8;

        if (isNotEmpty(ctype)) {
            String[] params = ctype.split(";");
            for (String param : params) {
                param = param.trim();
                if (param.startsWith("charset")) {
                    String[] pair = param.split("=", 2);
                    if (pair.length == 2) {
                        if (isNotEmpty(pair[1])) {
                            charset = pair[1].trim();
                        }
                    }
                    break;
                }
            }
        }

        return charset;
    }

    private boolean isNotEmpty(String value) {
        int strLen;
        if (value == null || (strLen = value.length()) == 0) {
            return false;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(value.charAt(i)))) {
                return true;
            }
        }
        return false;
    }
}
