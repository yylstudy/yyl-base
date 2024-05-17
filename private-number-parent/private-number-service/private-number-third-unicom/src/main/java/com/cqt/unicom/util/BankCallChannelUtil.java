package com.cqt.unicom.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.cqt.unicom.properties.KeyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@Slf4j
@Component
public class BankCallChannelUtil {

	private final KeyProperties keyProperties;


	/**
	 * 银行调用联通，用于测试解密验签处理
	 *
	 */

	public String testBy1024(String reqMsg)
	{

		// 针对回调通知的解密验签处理
		// 模拟请求报文
		log.info("收到的通知请求报文:\n" + reqMsg);

		// 将请求字符串转换成json
		JSONObject reqJson = JSON.parseObject(reqMsg, Feature.OrderedField);
		// 获取密文随机密钥key
		String key = (String) reqJson.get("keyinfo");
		log.info("随机密钥key密文:\n" + key);

		// 用私钥解密随机密钥
		String decryptKey = null;
		try {
			decryptKey = new String(GuardUtil.decryptByPrivateKey4Pkcs5(key.getBytes(), keyProperties.getPrivateKey()),
					Common.CHARSET);
			log.info("随机密钥key明文:\n" + decryptKey);
		} catch (UnsupportedEncodingException e) {
			//
			log.info("解密随机密钥失败，不支持的的字符集:\n" + e);
		}

		// 获取请求头
		JSONObject reqHead = reqJson.getJSONObject("reqHead");
		log.info("请求报文头reqHead:\n" + reqHead);

		// 获取请求体
		String reqData = reqJson.getString("reqData");
		log.info("请求报文体reqData密文:\n" + reqData);
		// 解密请求体
		String decryptReqData = null;
		try {
			decryptReqData = new String(GuardUtil.decrypt4Base64(reqData, decryptKey), Common.CHARSET);
			log.info("请求报文体reqData明文:\n" + decryptReqData);
		} catch (UnsupportedEncodingException e) {
			log.info("解密密文失败，不支持的的字符集:\n" + e);
		}

		// 验证请求报文签名
		String signData = reqHead.getString("signData");
		boolean verify = false;
		try {
			assert decryptReqData != null;
			verify = GuardUtil.verify(decryptReqData.getBytes(), keyProperties.getPublicKey(), signData);
		} catch (Exception e1) {
			log.info("验签异常:\n" + e1);
		}
		log.info("是否验签成功:\n" + verify);

		return decryptReqData;

	}

}
