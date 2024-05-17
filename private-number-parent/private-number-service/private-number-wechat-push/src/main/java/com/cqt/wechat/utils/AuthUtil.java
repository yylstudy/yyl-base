package com.cqt.wechat.utils;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author hlx
 * @date 2022-03-24
 */
@Slf4j
public class AuthUtil {

    /**
     * 对象加签名
     *
     * @param instance 实例
     * @param <T>      泛型带sign属性的类
     */
    public static <T> void addSignToInstance(T instance, String secretKey) {
        Class clazz = instance.getClass();
        try {
            Field field = clazz.getDeclaredField("sign");
            field.setAccessible(true);
            String objString = JSON.toJSONString(instance);
            TreeMap<String, Object> objectMap = JSON.parseObject(objString, TreeMap.class);
            String sign = createSign(objectMap, secretKey);
            field.set(instance, sign);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("添加sign异常！");
        }
    }


    /**
     * 新增sign
     *
     * @param params    属性map
     * @param secretKey 秘钥
     * @return 加密sign
     */
    private static String createSign(Map<String, Object> params, String secretKey) {
        params.remove("appkey");
        params.remove("sign");
        params.remove("vcc_id");
        List<String> paramList = new ArrayList<>();
        params.forEach((key, value) -> {
            if (ObjectUtil.isNotEmpty(value)) {
                paramList.add(key + "=" + StrUtil.removeAll(value.toString(), CharUtil.CR, CharUtil.LF, CharUtil.SPACE));
            }
        });
        paramList.add("secret_key=" + secretKey);
        return SecureUtil.md5(String.join("&", paramList)).toUpperCase();
    }

    /**
     * 签名验证测试
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(System.currentTimeMillis());
        String json = "{\"appkey\":\"3578\",\"bind_id\":\"cqt-2302515069679174387179521648124124284\",\"call_result\":1,\"call_type\":10,\"called\":\"15655619536\",\"caller\":\"17774986586\",\"current_time\":\"2022-03-24 20:18:25\",\"event\":\"hangup\",\"ext\":\"\",\"record_id\":\"85f8e31c-abee-4a4e-8231-fe55ade406e4\",\"sign\":\"D0AE37DF300A50E62369EAA25AB267E1\",\"tel_x\":\"18652984017\",\"ts\":1648171778837}";
        TreeMap treeMap = JSON.parseObject(json, TreeMap.class);
        String sign = createSign(treeMap, "ec5a114833fd448db0ae884bd42a6f02");
        treeMap.put("sign", sign);
        System.out.println(checkSign("ec5a114833fd448db0ae884bd42a6f02", treeMap));
    }

    public static boolean checkSign(String secretKey, TreeMap paramsMap) {
        if (ObjectUtil.isEmpty(paramsMap.get("ts"))) {
            throw new RuntimeException("ts 为空!");
        }
        // 前后5分钟
        long ts = Convert.toLong(paramsMap.get("ts"));
        DateTime dateTime = DateUtil.date(ts);
        long between = DateUtil.between(dateTime, DateUtil.date(), DateUnit.MINUTE, true);

        if (between > 5) {
            throw new RuntimeException("sign 已过期!");
        }
        // 验签
        String userSign = Convert.toStr(paramsMap.get("sign"));
        if (StrUtil.isEmpty(userSign)) {
            throw new RuntimeException("sign 不存在!");
        }
        String createSign = createSign(paramsMap, secretKey);
        boolean equals = userSign.equals(createSign);
        if (!equals) {
            throw new RuntimeException("sign 验证不通过!");
        }
        return true;
    }
}
