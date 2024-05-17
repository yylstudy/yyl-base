package com.cqt.sdk.client.strategy.client.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.cqt.base.util.ValidationUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author linshiqiang
 * date:  2023-07-12 9:25
 */
public abstract class AbstractClientChecker {

    /**
     * 转对象
     *
     * @param requestBody json
     * @param valueType   对象类型
     * @param <T>         对象类型
     * @return 对象
     * @throws JsonProcessingException json转化异常
     */
    public <T> T convert(String requestBody, Class<T> valueType) throws JsonProcessingException {
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        T t = objectMapper.readValue(requestBody, valueType);
        // 参数校验
        ValidationUtil.validate(t);
        return t;
    }

    /**
     * 转对象
     *
     * @param requestBody json
     * @param valueType   对象类型
     * @param <T>         对象类型
     * @return 对象
     * @throws JsonProcessingException json转化异常
     */
    public <T> T convert(String requestBody, Class<T> valueType, Class<?>... groups) throws JsonProcessingException {
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        T t = objectMapper.readValue(requestBody, valueType);
        // 参数校验
        ValidationUtil.validate(t, groups);
        return t;
    }
}
