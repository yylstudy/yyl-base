package com.cqt.unicom.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.dc.encrypt4.Base64Utils;
import com.koalii.svs.SvsSign;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 安全实现工具类
 *
 * @author yelan
 *
 */

public class GuardUtil {

	private static final String encoding = "UTF-8";
	private static final String AES_CBC_PKC_ALG = "AES/CBC/PKCS5Padding";
	private static final byte[] AES_IV = initIV(AES_CBC_PKC_ALG);
	private static final String RSA_ECB_PKCS1 = "RSA/ECB/PKCS1Padding";

	// 宝付需求所加参数
	/** 1024bit 加密块 大小 */
	public final static int ENCRYPT_KEYSIZE = 117;
	/** 1024bit 解密块 大小 */
	public final static int DECRYPT_KEYSIZE = 128;

	/** */
	/**
	 * 加密算法RSA
	 */
	public static final String KEY_ALGORITHM = "RSA";
	/** */
	/**
	 * 签名算法 SHA256WithRSA
	 */
	public static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";
	/** */
	/**
	 * 签名算法 SHA1WithRSA
	 */
	public static final String SIGNATURE_SHA1 = "SHA1WithRSA";

	private static final Log log = LogFactory.getLog(GuardUtil.class);

	/**
	 * 初始化向量
	 *
	 * @param aesCbcPkcAlg
	 * @return
	 */
	private static byte[] initIV(String aesCbcPkcAlg) {
		Cipher cp;
		try {
			cp = Cipher.getInstance(aesCbcPkcAlg);
			int blockSize = cp.getBlockSize();
			byte[] iv = new byte[blockSize];
			for (int i = 0; i < blockSize; ++i) {
				iv[i] = 0;
			}
			return iv;

		} catch (Exception e) {
			int blockSize = 16;
			byte[] iv = new byte[blockSize];
			for (int i = 0; i < blockSize; ++i) {
				iv[i] = 0;
			}
			return iv;
		}
	}

	/**
	 * 将二进制转换成16进制(不区分大小写)
	 *
	 * @param buf
	 * @return
	 */
	public static String parseByte2Hex(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	/**
	 * 将16进制字符串转为转换成字节数组
	 */
	public static byte[] hex2Bytes(String source) {
		byte[] sourceBytes = new byte[source.length() / 2];
		for (int i = 0; i < sourceBytes.length; i++) {
			sourceBytes[i] = (byte) Integer.parseInt(source.substring(i * 2,
					i * 2 + 2), 16);
		}
		return sourceBytes;
	}

	/**
	 * sha-256加密字符串
	 *
	 * @param str
	 * @return base64
	 */
	public static String HashSHA256encrypt(String str) {
		MessageDigest md;
		String mdStr = null;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(str.getBytes(encoding));
			mdStr = parseByte2Hex(md.digest());
			mdStr = Base64Utils.encode(mdStr.getBytes(encoding));
		} catch (Exception e) {
			log.error("SHA-256加密异常", e);
		}
		return mdStr;
	}

	/**
	 * aes解密
	 *
	 * @param content
	 *            待解密内容
	 * @param password
	 *            解密密钥
	 * @return
	 */
	public static byte[] decrypt4Base64(String content, String password) {
		try {
			byte[] byteContent = Base64Utils.decode(content);
			byte[] enCodeFormat = password.getBytes(encoding);
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance(AES_CBC_PKC_ALG);// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(AES_IV));// 初始化
			byte[] result = cipher.doFinal(byteContent);
			return result; // 加密
		} catch (Exception e) {
			log.error("AES解密异常", e);
		}
		return null;
	}

	/**
	 * aes加密
	 *
	 * @param content
	 *            需要加密的内容
	 * @param password
	 *            加密密码
	 * @return
	 */
	public static String encrypt4Base64(String content, String password) {
		try {
			byte[] bytePwd = password.getBytes(encoding);
			SecretKeySpec key = new SecretKeySpec(bytePwd, "AES");
			Cipher cipher = Cipher.getInstance(AES_CBC_PKC_ALG);// 创建密码器
			byte[] byteContent = content.getBytes(encoding);
			cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(AES_IV));// 初始化
			byte[] result = cipher.doFinal(byteContent);
			return Base64Utils.encode(result); // 加密
		} catch (Exception e) {
			log.error("AES加密异常", e);
		}
		return null;
	}

	public static void main(String[] args) {
		String pw = "sEDRoVZio71mBtjz";
		String s = "{\"serviceType\":\"voice\",\"releaseCause\":1,\"inboundCaller\":\"13840214982\",\"dtmfValue\":\"\",\"outboundCallee\":\"\",\"releaseDir\":2,\"appId\":\"IT72mRyV0qWfGiSY36DvudNqXoemNVEd\",\"q850Code\":23,\"startTime\":\"2023-11-17 09:50:14\",\"endTime\":\"2023-11-17 09:50:21\",\"id\":\"SIPvwVRqYNh-Xo3EnWaAYqT1@mgcf02-jl-ims-chinaunicom-cn\",\"inboundCallee\":\"13050558514\",\"outboundCaller\":\"\",\"timestamp\":1700185821590}";
		String s1 = encrypt4Base64(s, pw);
		String en = "O+4xIF9x3IYc5IsAanGdWHHU3u7ID3FESTPQ4TSwccRU07PuhN0uEJj24iRuQWJ6Ft62UA2zTTuR\n" +
				"w/4p3YJflf4ips3Aa16LakkCi1BLxPsoCeG83u/DTAt0syhNoxsB4UL0Q7AMy5/A12Vgosko8wPA\n" +
				"pG1ebJ4kRIG0jTYYQX+Odq2zdqWfBhKtTl78QfTj2/dmAI7dSs1a5fPjtENRnMBpscVe8c7nUtap\n" +
				"KjWYxpvjk+J0DUP9LLycJGE/2+89VVNgH00I+lwNsFruT8eMDaVGcfDDXA4FePwqm3J5KkcNPOjW\n" +
				"n7uO8kpkXkrik0iUEhMTnTSF3cWSI+6Jj7rqgpG6c6k/49mXb8QbdiWwvZXqKK3Z8afbB3pG16pG\n" +
				"zDmqgsuz9OI0yoeM/yNHh20KJo3vS0VmnsJYaBoxv69RYp1M23/vXt6UhrllHcENhfbH51RDP/Tv\n" +
				"YdMT7DtMaTXSEnIFgHUiyGmNyRZe1rBazMhj+yoPm1Em0FRZbbsKMVU5";
		String s2 = new String(decrypt4Base64(en, pw));
		System.out.println(s2);
	}

	/**
	 * rsa解密sdk上送key
	 *
	 * @param content
	 *            加密rsaBase64串
	 * @param privateKey
	 *            rsa私钥
	 * @return
	 */
	public static byte[] decryptByPrivateKey4Pkcs5(byte[] content,
			String privateKey) {
		Cipher c = null;
		byte[] decryptedData = null;
		try {
			byte[] priByte = Base64Utils.decode(privateKey);

			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(priByte);
			KeyFactory keyF = KeyFactory.getInstance("RSA");
			RSAPrivateKey pk = (RSAPrivateKey) keyF.generatePrivate(keySpec);

			c = Cipher.getInstance(RSA_ECB_PKCS1);
			c.init(Cipher.DECRYPT_MODE, pk);

			byte[] contentB = Base64Utils.decode(new String(content, encoding));
			int inputLen = contentB.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;
			byte[] cache;
			int i = 0;
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > 128) {
					cache = c.doFinal(contentB, offSet, 128);
				} else {
					cache = c.doFinal(contentB, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * 128;
			}
			decryptedData = out.toByteArray();
			out.close();

		} catch (Exception e) {
			log.error("OpenAPI解析随机密钥异常", e);
			return null;
		}
		return decryptedData;
	}

	/**
	 * rsa解密sdk上送key
	 *
	 * @param content
	 *            加密rsaBase64串
	 * @param privateKey
	 *            rsa私钥
	 * @return
	 */
	public static byte[] decryptByPrivateKey4Pkcs52048(byte[] content,
			String privateKey) {
		Cipher c = null;
		byte[] decryptedData = null;
		try {
			byte[] priByte = Base64Utils.decode(privateKey);

			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(priByte);
			KeyFactory keyF = KeyFactory.getInstance("RSA");
			RSAPrivateKey pk = (RSAPrivateKey) keyF.generatePrivate(keySpec);

			c = Cipher.getInstance(RSA_ECB_PKCS1);
			c.init(Cipher.DECRYPT_MODE, pk);

			byte[] contentB = Base64Utils.decode(new String(content, encoding));
			int inputLen = contentB.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;
			byte[] cache;
			int i = 0;
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > 128) {
					cache = c.doFinal(contentB, offSet, 128);
				} else {
					cache = c.doFinal(contentB, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * 128;
			}
			decryptedData = out.toByteArray();
			out.close();

		} catch (Exception e) {
			log.error("OpenAPI解析随机密钥异常", e);
			return null;
		}
		return decryptedData;
	}

	/**
	 * rsa解密sdk上送key
	 *
	 * @param content
	 *            加密rsaBase64串
	 * @param privateKey
	 *            rsa私钥
	 * @return
	 */
	public static byte[] decryptByPrivateKey4Pkcs5(byte[] content,
			PrivateKey privateKey) {
		Cipher c = null;
		byte[] decryptedData = null;
		try {
			c = Cipher.getInstance(RSA_ECB_PKCS1);
			c.init(Cipher.DECRYPT_MODE, privateKey);

			byte[] contentB = Base64Utils.decode(new String(content, encoding));
			int inputLen = contentB.length;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int offSet = 0;
			byte[] cache;
			int i = 0;
			while (inputLen - offSet > 0) {
				if (inputLen - offSet > 128) {
					cache = c.doFinal(contentB, offSet, 128);
				} else {
					cache = c.doFinal(contentB, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * 128;
			}
			decryptedData = out.toByteArray();
			out.close();

		} catch (Exception e) {
			log.error("OpenAPI解析随机密钥异常", e);
			return null;
		}
		return decryptedData;
	}

	/** */
	/**
	 * <p>
	 * 公钥加密
	 * </p>
	 *
	 * @param data
	 *            源数据
	 * @param publicKey
	 *            公钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static String encryptByPublicKey4Pkcs5(byte[] data, String publicKey)
			throws Exception {
		byte[] keyBytes = Base64Utils.decode(publicKey);
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		RSAPublicKey publicK = (RSAPublicKey) keyFactory
				.generatePublic(x509KeySpec);
		// 对数据加密
		Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1);
		cipher.init(Cipher.ENCRYPT_MODE, publicK);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > 128) {
				cache = cipher.doFinal(data, offSet, 128);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * 128;
		}
		byte[] encryptedData = out.toByteArray();

		out.close();
		return Base64Utils.encode(encryptedData);
	}

	/**
	 * <p>
	 * 公钥加密
	 * </p>
	 *
	 * @param data
	 *            源数据
	 * @param publicKey
	 *            公钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static String encryptByPublicKey4Pkcs5(byte[] data,
			PublicKey publicKey) throws Exception {

		// 对数据加密
		Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1);
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > 128) {
				cache = cipher.doFinal(data, offSet, 128);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * 128;
		}
		byte[] encryptedData = out.toByteArray();

		out.close();
		return Base64Utils.encode(encryptedData);
	}

	/**
	 * <p>
	 * 用私钥对信息生成数字签名
	 * </p>
	 *
	 * @param data
	 *            已加密数据
	 * @param privateKey
	 *            私钥(BASE64编码)
	 *
	 * @return
	 * @throws Exception
	 */
	public static String sign(byte[] data, String privateKey) throws Exception {
		byte[] keyBytes = Base64Utils.decode(privateKey);
		PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateK);
		signature.update(data);
		return Base64Utils.encode(signature.sign());
	}

	/**
	 * <p>
	 * 用私钥对信息生成数字签名
	 * </p>
	 *
	 * @param data
	 *            已加密数据
	 * @param privateKey
	 *            私钥(BASE64编码)
	 *
	 * @return
	 * @throws Exception
	 */
	public static String sign(byte[] data, PrivateKey privateKey)
			throws Exception {

		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateKey);
		signature.update(data);
		return Base64Utils.encode(signature.sign());
	}

	/**
	 * <p>
	 * 校验数字签名 SHA1
	 * </p>
	 *
	 * @param data
	 *            已加密数据
	 * @param publicKey
	 *            公钥(BASE64编码)
	 * @param sign
	 *            数字签名
	 *
	 * @return
	 * @throws Exception
	 *
	 */
	public static boolean verifyBySHA1(byte[] data, PublicKey publicKey,
			String sign) throws Exception {
		Signature signature = Signature.getInstance(SIGNATURE_SHA1);
		signature.initVerify(publicKey);
		signature.update(data);
		return signature.verify(Base64Utils.decode(sign));
	}

	/** */
	/**
	 * <p>
	 * 校验数字签名
	 * </p>
	 *
	 * @param data
	 *            已加密数据
	 * @param publicKey
	 *            公钥(BASE64编码)
	 * @param sign
	 *            数字签名
	 *
	 * @return
	 * @throws Exception
	 *
	 */
	public static boolean verify(byte[] data, String publicKey, String sign)
			throws Exception {
		byte[] keyBytes = Base64Utils.decode(publicKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		PublicKey publicK = keyFactory.generatePublic(keySpec);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(publicK);
		signature.update(data);
		return signature.verify(Base64Utils.decode(sign));
	}

	/**
	 * <p>
	 * 校验数字签名
	 * </p>
	 *
	 * @param data
	 *            已加密数据
	 * @param publicKey
	 *            公钥(BASE64编码)
	 * @param sign
	 *            数字签名
	 *
	 * @return
	 * @throws Exception
	 *
	 */
	public static boolean verify(byte[] data, PublicKey publicKey, String sign)
			throws Exception {
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(publicKey);
		signature.update(data);
		return signature.verify(Base64Utils.decode(sign));
	}

	/**
	 * 报文验签。
	 *
	 * @param signed
	 *            银行的签名
	 * @param unsigned
	 *            未签名的源报文
	 * @return 验签是否成功
	 */
	public static boolean verify(String signed, String unsigned,
			PublicKey publicKey) {
		Base64 base64 = new Base64();
		boolean valid = false;
		// String unsigned2;
		try {
			// unsigned2 = new
			// String(base64.encode(unsigned.getBytes(encoding)));
			byte sourceData[] = unsigned.getBytes(encoding);
			// byte sourceData[] = unsigned2.getBytes(encoding);

			// base64解密
			byte[] sigData = base64.decode(signed);
			// 初始化签名
			Signature sig = Signature.getInstance("SHA256withRSA");
			sig.initVerify(publicKey);
			sig.update(sourceData);

			// 验签
			valid = sig.verify(sigData);
		} catch (Exception e) {
			System.out.println("[PointExpressRsaCertifier]签名异常");
		}

		return valid;
	}

	/**
	 *
	 * @param data
	 * @param signature
	 * @param algorithm
	 * @param publicKey
	 * @return
	 * @throws Exception
	 */
	public static boolean verify(byte[] data, byte[] signature,
			String algorithm, PublicKey publicKey) throws Exception {
		Signature sig = Signature.getInstance(algorithm);
		sig.initVerify(publicKey);
		sig.update(data);
		return sig.verify(signature);
	}

	/**
	 * 随机数
	 *
	 * @return
	 */
//	public static String getRandom() {
//		StringBuffer sb = new StringBuffer();
//		Random random = new Random();
//		for (int i = 0; i < 16; i++) {
//			sb.append("1234567890qwertyuiopasdfghjklzxcvbnm".charAt(random
//					.nextInt("1234567890qwertyuiopasdfghjklzxcvbnm".length())));
//		}
//		return sb.toString();
//	}

	/**
	 * 格尔加签
	 * reqDataStr 待加签的请求报文体reqData明文
	 * certPath 证书路径
	 * certKey 证书密码
	 * isH5 是H5调用还是API调用
	 * @return
	 */
	public static String SVSinitAPI(String reqDataStr, String certPath, String certKey, boolean isH5) {
		TimeUtil tu = null;
		if (log.isInfoEnabled()) {
			log.info("SVSinitAPI………………start");
			log.info("待加签的请求报文体reqData明文:\n" + reqDataStr);
			tu = new TimeUtil();
			tu.startRec();
		}
		JSONObject signJson = null;

		JSONObject reqDataJson = JSONObject.parseObject(reqDataStr, Feature.OrderedField);
		SvsSign signer = new SvsSign();
		try {
			signJson = new JSONObject();
			FileInputStream fin = new FileInputStream(certPath);
			signer.initSignCertAndKey(fin, certKey);

			String signStr = null;
			if (isH5) { //如果是H5则需要转换一下
				signStr = HashSHA256encrypt(reqDataJson.toJSONString());
			} else { //退款
				signStr = reqDataJson.toJSONString();
			}
			assert signStr != null;
			String signData = signer.signData(signStr.getBytes(
					encoding));
			String signCert = signer.getEncodedSignCert();


			System.out.println("签名值signData:\n" + signData);
			System.out.println("证书公钥signCert:\n" + signCert);

			signJson.put("signData", signData);
			signJson.put("signCert", signCert);

			if (log.isInfoEnabled()) {
				assert tu != null;
				tu.endRec();
				log.info(tu.output("SVSinitAPI………………end"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			assert signJson != null;
			signJson.put("returnCode", "070002");
			signJson.put("returnMsg", "加签失败");
		}

		return signJson.toJSONString();
	}
}
