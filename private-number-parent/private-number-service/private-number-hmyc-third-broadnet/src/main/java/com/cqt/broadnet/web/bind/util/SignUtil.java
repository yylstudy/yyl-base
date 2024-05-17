package com.cqt.broadnet.web.bind.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.TreeMap;

/**
 * 广电签名接口
 *
 * @author Xienx
 * @date 2023-05-26 16:13:16:13
 */
@Slf4j
@UtilityClass
public final class SignUtil {

    /**
     * 接口签名 (md5)
     *
     * @param json      接口请求参数
     * @param secretKey 秘钥
     * @return String 签名结果
     */
    public String createSign(String json, String secretKey) {
        TreeMap<String, Object> params = parseTreeMap(json);
        StringBuilder paramStr = new StringBuilder();

        paramStr.append(secretKey);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            paramStr.append(entry.getKey()).append(entry.getValue());
        }
        paramStr.append(secretKey);

        return SecureUtil.md5(paramStr.toString()).toUpperCase();
    }

    /**
     * 参数解析, 按参数名称的ascii码顺序排序
     */
    private TreeMap<String, Object> parseTreeMap(String json) {
        TreeMap<String, Object> params = new TreeMap<>();
        if (StrUtil.isNotEmpty(json)) {
            params = JSON.parseObject(json, new TypeReference<TreeMap<String, Object>>() {
            });
        }
        return params;
    }

    /**
     * 接口签名 (hmac_md5)
     *
     * @param json      接口请求参数
     * @param secretKey 秘钥
     * @return String 签名结果
     */
    public String createSignHmacMd5(String json, String secretKey) {
        TreeMap<String, Object> params = parseTreeMap(json);

        StringBuilder paramStr = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            paramStr.append(entry.getKey()).append(entry.getValue());
        }
        HMac hmac = SecureUtil.hmac(HmacAlgorithm.HmacMD5, secretKey);

        return hmac.digestHex(paramStr.toString()).toUpperCase();
    }

    /**
     * 接口签名 (hmac_sha256)
     *
     * @param json      接口请求参数
     * @param secretKey 秘钥
     * @return String 签名结果
     */
    public String createSignHmacSha256(String json, String secretKey,String extra) {
        TreeMap<String, Object> params = parseTreeMap(json);

        StringBuilder paramStr = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if ("extra".equals(entry.getKey())){
                paramStr.append(entry.getKey()).append(extra);
                continue;
            }
            paramStr.append(entry.getKey()).append(entry.getValue());
        }
        log.info("加密前参数："+ paramStr);
        HMac hmac = SecureUtil.hmac(HmacAlgorithm.HmacSHA256, secretKey);

        return hmac.digestHex(paramStr.toString()).toUpperCase();
    }

    public static void main(String[] args) {
        String s = "areaCode21expiration600extra{\"record\":1}requestId123445446789telA8613145844442telB8615674129338xNumber8619230714434";
        HMac hmac = SecureUtil.hmac(HmacAlgorithm.HmacSHA256, "e7a8fd9b4f7c6430d6c8ed00fe62f99f");
        System.out.println(hmac.digestHex(s).toUpperCase());

    }
}
