package com.linkcircle.basecom.desensitization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.linkcircle.basecom.annotation.WebSensitive;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 脱敏序列化器
 * @createTime 2024/4/12 17:15
 */

public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private Class<? extends SensitiveStrategy> strategy;
    /**
     * 脱敏器映射
     */
    private static Map<Class, SensitiveStrategy> sensitiveStrategyMap = new ConcurrentHashMap<>();
    @Override
    public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        SensitiveStrategy sensitiveStrategy = getSensitiveStrategy(strategy);
        jsonGenerator.writeString(sensitiveStrategy.handle(value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        WebSensitive annotation = beanProperty.getAnnotation(WebSensitive.class);
        if (Objects.nonNull(annotation)&&Objects.equals(String.class, beanProperty.getType().getRawClass())) {
            this.strategy = annotation.strategy();
            return this;
        }
        return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
    }

    /**
     * 获取脱敏策略
     * @return
     */
    private static SensitiveStrategy getSensitiveStrategy(Class<? extends SensitiveStrategy> clazz){
        return sensitiveStrategyMap.computeIfAbsent(clazz,t-> {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
