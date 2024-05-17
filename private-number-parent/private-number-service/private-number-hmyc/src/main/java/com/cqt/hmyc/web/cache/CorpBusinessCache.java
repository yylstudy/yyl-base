package com.cqt.hmyc.web.cache;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2022/2/22 14:47
 * 企业业务信息缓存
 */
@Slf4j
public class CorpBusinessCache {

    /**
     * key: vccId
     * 1天有效期
     */
    public final static Map<String, PrivateCorpBusinessInfoDTO> CORP_BUSINESS_INFO_CACHE = new ConcurrentHashMap<>(1024);

    public static void putAll(Map<String, PrivateCorpBusinessInfoDTO> map) {
        CORP_BUSINESS_INFO_CACHE.putAll(map);
        for (Map.Entry<String, PrivateCorpBusinessInfoDTO> entry : CORP_BUSINESS_INFO_CACHE.entrySet()) {
            String key = entry.getKey();
            PrivateCorpBusinessInfoDTO dto = map.get(key);
            if (ObjectUtil.isEmpty(dto)) {
                CORP_BUSINESS_INFO_CACHE.remove(key);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void put(String key, PrivateCorpBusinessInfoDTO businessInfo) {
        String bindingParamAdapter = businessInfo.getBindingParamAdapter();
        if (StrUtil.isNotEmpty(bindingParamAdapter)) {
            Map<String, Map<String, String>> map = JSON.parseObject(bindingParamAdapter, Map.class);
            businessInfo.setBindingParamAdapterMap(map);
        }
        CORP_BUSINESS_INFO_CACHE.put(key, businessInfo);
    }

    public static void remove(String key) {
        CORP_BUSINESS_INFO_CACHE.remove(key);
    }

    public static Optional<PrivateCorpBusinessInfoDTO> get(String key) {

        try {
            return Optional.ofNullable(CORP_BUSINESS_INFO_CACHE.get(key));
        } catch (Exception e) {
            log.error("corp business info cache get error", e);
        }
        return Optional.empty();
    }

    public static int size() {

        return CORP_BUSINESS_INFO_CACHE.size();
    }

    public static void clear() {

        CORP_BUSINESS_INFO_CACHE.clear();
    }
}
