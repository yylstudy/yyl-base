package com.repush.util.crypt;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class HmacMD5 {
    public static String hmacMd5Key() {
        return null;
    }

    public static byte[] initHmacMD5Key() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("HmacMD5");
        SecretKey secretKey = generator.generateKey();
        byte[] key = secretKey.getEncoded();
        return key;
    }

    public static String hmacMd5(byte[] data, byte[] key) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, "HmacMD5");

            Mac mac = Mac.getInstance(secretKey.getAlgorithm());

            mac.init(secretKey);
            byte[] digest = mac.doFinal(data);

            return new HexBinaryAdapter().marshal(digest);
        } catch (Exception e) {
            return null;
        }
    }

    public static String hmacMd5(String data, String key) {
        try {
            return hmacMd5(data.getBytes("UTF-8"), key.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {

        try {
            System.out.println(HmacMD5.hmacMd5("ehome".getBytes("UTF-8"), "123456".getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // System.out.println( new String( HmacMD5.initHmacMD5Key(),
        // Charset.forName( "UTF-8" ) ) );

    }
}
