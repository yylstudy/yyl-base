package com.linkcircle.ss;


import org.springframework.util.Base64Utils;

import javax.crypto.Cipher;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021/11/25 11:13
 */

public class F {
    public static String decryptPassword(String passwordStr,String publicKeyStr){
        if(publicKeyStr==null||publicKeyStr.equals("")){
            throw new RuntimeException("publicKey为空，请指定publicKey");
        }
        PublicKey publicKey = getPublicKey(publicKeyStr);
        try{
            String password = decrypt(publicKey,passwordStr);
            return password;
        }catch (Exception e){
            throw new RuntimeException("数据库密码解密失败");
        }
    }



    public static PublicKey getPublicKey(String publicKeyText) {
        try {
            byte[] publicKeyBytes = Base64Utils.decodeFromString(publicKeyText);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(
                    publicKeyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "SunRsaSign");
            return keyFactory.generatePublic(x509KeySpec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to get public key", e);
        }
    }



    public static String decrypt(PublicKey publicKey, String cipherText)
            throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        try {
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            // 因为 IBM JDK 不支持私钥加密, 公钥解密, 所以要反转公私钥
            // 也就是说对于解密, 可以通过公钥的参数伪造一个私钥对象欺骗 IBM JDK
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            RSAPrivateKeySpec spec = new RSAPrivateKeySpec(rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());
            Key fakePrivateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
            cipher = Cipher.getInstance("RSA"); //It is a stateful object. so we need to get new one.
            cipher.init(Cipher.DECRYPT_MODE, fakePrivateKey);
        }

        if (cipherText == null || cipherText.length() == 0) {
            return cipherText;
        }

        byte[] cipherBytes = Base64Utils.decodeFromString(cipherText);
        byte[] plainBytes = cipher.doFinal(cipherBytes);

        return new String(plainBytes);
    }
}
