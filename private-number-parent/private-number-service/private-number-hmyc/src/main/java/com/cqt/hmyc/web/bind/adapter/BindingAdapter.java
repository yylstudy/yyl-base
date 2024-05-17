package com.cqt.hmyc.web.bind.adapter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.cqt.hmyc.config.exception.ParamsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * @date 2022/3/18 9:41
 * 接口参数适配器
 */
@Component
public class BindingAdapter<T> {

    private final static ObjectMapper MAPPER = new ObjectMapper();

    private final static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 接口参数适配
     * {
     * 	"AXB": {
     * 		"whole_area": "wholearea"
     *        },
     * 	"AXE": {
     * 		"whole_area": "wholearea",
     * 		"ayb_audio_a_call_x": "audio_a_call_x",
     * 		"ayb_audio_a_called_x": "audio_a_called_x",
     * 		"ayb_audio_b_call_x": "audio_b_call_x",
     * 		"ayb_audio_b_called_x": "audio_b_called_x"
     *    }
     * }
     * @param params 接口入参
     * @param adapterMapOptional 适配器信息
     * @param clazz 类
     * @return 实体
     */
    @SuppressWarnings("all")
    public T change(Map<String, Object> params, Optional<Map<String, String>> adapterMapOptional, Class<T> clazz) {

        if (!adapterMapOptional.isPresent()) {
            T t = MAPPER.convertValue(params, clazz);
            validator(t);
            return t;
        }
        Map<String, String> adapterMap = adapterMapOptional.get();
        for (Map.Entry<String, String> entry : adapterMap.entrySet()) {

            // 通用字段
            String key = entry.getKey();
            // 定制字段
            String value = entry.getValue();

            Object obj = params.get(value);
            if (ObjectUtil.isNotEmpty(obj)) {
                params.put(key, obj);
            }
        }
        T t = MAPPER.convertValue(params, clazz);
        validator(t);
        return t;
    }

    private void validator(T t) {
        Set<ConstraintViolation<T>> validate = VALIDATOR.validate(t);
        if (CollUtil.isNotEmpty(validate)) {
            Optional<ConstraintViolation<T>> violationOptional = validate.stream().findFirst();
            if (violationOptional.isPresent()) {
                throw new ParamsException(violationOptional.get().getMessage());
            }
        }
    }

}
