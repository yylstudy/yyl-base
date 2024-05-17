package com.cqt.sdk.client.util;

import com.cqt.model.common.CloudCallCenterProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.AlgorithmParameterSpec;


/**
 * @author Xienx
 * @date 2023-07-04 17:33:17:33
 */
@Slf4j
@Component
public class AESUtil {
    private static final String AES_NAME = "AES";
    // 加密模式
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    private final CloudCallCenterProperties properties;

    public AESUtil(CloudCallCenterProperties properties) {
        this.properties = properties;
    }

    /**
     * 加密
     *
     * @param content 待加密内容
     * @return String
     */
    public String encrypt(String content) {
        byte[] result = null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            String key = properties.getSecretInfo().getKey();
            String iv = properties.getSecretInfo().getIv();
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_NAME);
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, paramSpec);
            result = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("AES加密异常: ", e);
        }
        return Base64.encodeBase64String(result);
    }

    /**
     * 解密
     *
     * @param content content
     * @return String
     */
    public String decrypt(String content) {
        try {
            String key = properties.getSecretInfo().getKey();
            String iv = properties.getSecretInfo().getIv();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), AES_NAME);
            AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.DECRYPT_MODE, keySpec, paramSpec);
            return new String(cipher.doFinal(Base64.decodeBase64(content)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("AES解密异常: ", e);
        }
        return StringUtils.EMPTY;
    }
}
