package com.linkcircle.basecom.handler.defaultHandler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkcircle.basecom.handler.SignHandler;
import com.linkcircle.basecom.util.HmacSHA256Signature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 签名校验中默认appKey处理器，这里是不校验
 * @createTime 2024/3/27 15:40
 */
@Slf4j
public class DefaultSignHandler implements SignHandler {

    @Value("${sign.appSecret:cqt@1234}")
    private String appSecret;

    private static ObjectMapper objectMapper = new ObjectMapper();
    static{
        //值为空不参与序列化
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * appkey校验，这里不校验
     * @param appKey
     */
    @Override
    public void checkAppKey(String appKey) {

    }

    /**
     * 签名计算
     * @param paramMap
     * @return
     */
    @Override
    public String sign(Map<String, String> paramMap) {
        try{
            String jsonStr = objectMapper.writeValueAsString(paramMap);
            String sign = HmacSHA256Signature.create().computeSignature(appSecret,jsonStr);
            return sign;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
