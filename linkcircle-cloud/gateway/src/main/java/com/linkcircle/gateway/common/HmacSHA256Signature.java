package com.linkcircle.gateway.common;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 参考 aliyun oss
 * @createTime 2023/7/12 10:54
 */

public class HmacSHA256Signature {
    private static final String DEFAULT_ENCODING = "UTF-8";

    private static final String ALGORITHM = "HmacSHA256";

    private static final String VERSION = "1";

    private static final Object LOCK = new Object();

    private static Mac macInstance;

    public static HmacSHA256Signature create(){
        return new HmacSHA256Signature();
    }

    public String getAlgorithm() {
        return ALGORITHM;
    }

    public String getVersion() {
        return VERSION;
    }
    public String computeSignature(String key, String data) {
        try {
            byte[] signData = sign(key.getBytes(DEFAULT_ENCODING), data.getBytes(DEFAULT_ENCODING), macInstance,
                    LOCK, ALGORITHM);
            return new String(Base64.encodeBase64(signData));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported algorithm: " + DEFAULT_ENCODING, ex);
        }
    }

    protected byte[] sign(byte[] key, byte[] data, Mac macInstance, Object lock, String algorithm) {
        try {
            // Because Mac.getInstance(String) calls a synchronized method, it
            // could block on
            // invoked concurrently, so use prototype pattern to improve perf.
            if (macInstance == null) {
                synchronized (lock) {
                    if (macInstance == null) {
                        macInstance = Mac.getInstance(algorithm);
                    }
                }
            }
            Mac mac;
            try {
                mac = (Mac) macInstance.clone();
            } catch (CloneNotSupportedException e) {
                // If it is not clonable, create a new one.
                mac = Mac.getInstance(algorithm);
            }
            mac.init(new SecretKeySpec(key, algorithm));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Unsupported algorithm: " + algorithm, ex);
        } catch (InvalidKeyException ex) {
            throw new RuntimeException("Invalid key: " + key, ex);
        }
    }
}
