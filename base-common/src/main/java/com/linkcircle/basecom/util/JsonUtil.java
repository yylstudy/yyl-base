package com.linkcircle.basecom.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/3/22 9:29
 */
@Slf4j
public class JsonUtil {
    private static ObjectMapper mapper = new ObjectMapper();
    static {
        //输出所有属性
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        //空对象转json不报错
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //禁止序列化日期为timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //禁止遇到未知属性抛出异常
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //空字符串为null
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        //决定parser将是否允许解析使用Java/C++ 样式的注释（包括'/'+'*' 和'//' 变量）。
        //由于JSON标准说明书上面没有提到注释是否是合法的组成，所以这是一个非标准的特性；
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS,true);
        // 允许属性名称没有引号
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        //允许单引号
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //时间转化器
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        //雪花ID失真
        javaTimeModule.addSerializer(Long.class, ToStringSerializer.instance);
        mapper.registerModule(javaTimeModule);
    }

    /**
     * 对象转Json格式字符串
     * @param obj 对象
     * @return Json格式字符串
     */
    public static <T> String toJSONString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对象转Json格式字符串(格式化的Json字符串)
     * @param obj 对象
     * @return 美化的Json格式字符串
     */
    public static <T> String toJSONStringPretty(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return obj instanceof String ? (String) obj : mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 字符串转换为自定义对象
     * @param json 要转换的字符串
     * @param clazz 自定义对象的class对象
     * @return 自定义对象
     */
    public static <T> T parseObject(String json, Class<T> clazz){
        if(!StringUtils.hasText(json) || clazz == null){
            return null;
        }
        try {
            return clazz.equals(String.class) ? (T) json : mapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 字符串转换为自定义对象
     * @param json 要转换的字符串
     * @param parametrized 自定义对象的class对象
     * @param parameterClass 自定义对象的class对象上的泛型
     * @param <T>
     * @return
     */
    public static <T> T parseObject(String json, Class<?> parametrized, Class<?>... parameterClass){
        if(StringUtils.isEmpty(json) || parametrized == null){
            return null;
        }
        JavaType javaType = mapper.getTypeFactory().constructParametricType(parametrized, parameterClass);
        try {
            return mapper.readValue(json, javaType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * 字符串转换为自定义对象
     * @param json 要转换的字符串
     * @param typeReference
     * @param <T>
     * @return
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference){
        if(StringUtils.isEmpty(json) || typeReference == null){
            return null;
        }
        try {
            return  mapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * json字符串转List
     * @param json
     * @param parameterClasses
     * @param <T>
     * @return
     */
    public static <T> List<T> parseList(String json, Class<T> parameterClasses){
        return parseObject(json,List.class,parameterClasses);
    }


    /**
     * json字符串转数组
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T[] parseArray(String json, Class<T> clazz){
        try{
            if(StringUtils.isEmpty(json) || clazz == null){
                return null;
            }
            ArrayType arrayType = mapper.getTypeFactory().constructArrayType(clazz);
            return mapper.readValue(json,arrayType);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 对象转map
     * @param obj
     * @return
     */
    public static Map<String,Object> parseMap(Object obj){
        try{
            if(obj == null){
                return null;
            }
            return mapper.convertValue(obj,Map.class);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }
}
