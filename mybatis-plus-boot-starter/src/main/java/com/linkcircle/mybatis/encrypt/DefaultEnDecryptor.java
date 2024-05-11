package com.linkcircle.mybatis.encrypt;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.DES;
import com.linkcircle.mybatis.constant.CommonConstant;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/11 16:47
 */

public class DefaultEnDecryptor implements EnDecryptor {
    @Override
    public String encrypt(String value) {
        DES des = SecureUtil.des(CommonConstant.DEFAULT_PASSWORD.getBytes());
        return des.encryptHex(value);
    }
    @Override
    public String decrypt(String value) {
        DES des = SecureUtil.des(CommonConstant.DEFAULT_PASSWORD.getBytes());
        return des.decryptStr(value);
    }

    public static void main(String[] args) {
        DefaultEnDecryptor defaultEnDecryptor = new DefaultEnDecryptor();
        String str = defaultEnDecryptor.encrypt("15255178553");
        System.out.println(str);
        System.out.println(defaultEnDecryptor.decrypt(str));
    }
}
