package com.cqt.ivr.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @Title: JsonUtils
 * @Description: json工具类
 * @author youngder
 * @date   2017年5月9日上午2:10:53
 */
public class JsonUtil {

    // 定义jackson对象
    private static final ObjectMapper MAPPER = new ObjectMapper();


    /**
     * 将对象转换成json字符串。
     */
    public static String objectToJson(Object data) {
        try {
            String string = MAPPER.writeValueAsString(data);
            return string;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
