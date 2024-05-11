package com.linkcircle.system.util;

import cn.hutool.crypto.symmetric.SM4;
import com.linkcircle.system.common.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.Base64;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/2/27 17:29
 */
@Slf4j
public class PasswordUtil {
    private static final String PASSWORD_SALT_FORMAT = "linkcircle_%s_admin_$^&*";
    private static final String SM4_KEY = "1024abcd1024abcd1024abcd1024abcd";
    /**
     * 后端加密密码
     */
    public static String getEncryptPwd(String password) {
        return DigestUtils.md5Hex(String.format(PASSWORD_SALT_FORMAT, password));
    }

    /**
     * 前端密码解密
     * @param data
     * @return
     */
    public static String decrypt(String data) {
        try {
            // 第一步： Base64 解码
            byte[] base64Decode = Base64.getDecoder().decode(data);
            // 第二步： SM4 解密
            SM4 sm4 = new SM4(hexToBytes(SM4_KEY));
            return sm4.decryptStr(new String(base64Decode));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CommonConstant.EMPTY_PASSWORD;
        }
    }
    /**
     * 16 进制串转字节数组
     *
     * @param hex 16进制字符串
     * @return byte数组
     */
    public static byte[] hexToBytes(String hex) {
        int length = hex.length();
        byte[] result;
        if (length % 2 == 1) {
            length++;
            result = new byte[(length / 2)];
            hex = "0" + hex;
        } else {
            result = new byte[(length / 2)];
        }
        int j = 0;
        for (int i = 0; i < length; i += 2) {
            result[j] = hexToByte(hex.substring(i, i + 2));
            j++;
        }
        return result;
    }
    /**
     * 16 进制字符转字节
     *
     * @param hex 16进制字符 0x00到0xFF
     * @return byte
     */
    private static byte hexToByte(String hex) {
        return (byte) Integer.parseInt(hex, 16);
    }

    public static void main(String[] args) {
        System.out.println(getEncryptPwd("123456"));
    }
}
