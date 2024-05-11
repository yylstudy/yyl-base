package com.linkcircle.mybatis.encrypt;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 加解密器
 * @createTime 2024/4/11 16:38
 */

public interface EnDecryptor {
    /**
     * 加密
     * @param value
     * @return
     */
    String encrypt(String value);

    /**
     * 解密
     * @param value
     * @return
     */
    String decrypt(String value);
}
