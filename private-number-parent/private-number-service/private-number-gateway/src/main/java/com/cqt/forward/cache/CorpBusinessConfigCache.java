package com.cqt.forward.cache;

import cn.hutool.core.util.ObjectUtil;
import com.cqt.common.util.CopyOnWriteMap;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.corpinfo.dto.SupplierWeight;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/7/25 17:47
 * 企业业务配置信息缓存
 */
public class CorpBusinessConfigCache {

    private static final CopyOnWriteMap<String, PrivateCorpBusinessInfoDTO> CACHE = new CopyOnWriteMap<>();

    public static void put(String vccId, PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO) {
        CACHE.put(vccId, privateCorpBusinessInfoDTO);
    }

    public static void putAll(Map<String, PrivateCorpBusinessInfoDTO> map) {
        CACHE.putAll(map);
        for (Map.Entry<String, PrivateCorpBusinessInfoDTO> entry : CACHE.entrySet()) {
            String key = entry.getKey();
            PrivateCorpBusinessInfoDTO dto = map.get(key);
            if (ObjectUtil.isEmpty(dto)) {
                CACHE.remove(key);
            }
        }
    }

    public static Optional<PrivateCorpBusinessInfoDTO> get(String vccId) {
        return Optional.ofNullable(CACHE.get(vccId));
    }

    public static void remove(String vccId) {
        CACHE.remove(vccId);
    }

    public static void clear() {
        CACHE.clear();
    }

    /**
     * 获取供应商 权重信息
     *
     * @param vccId    企业id
     * @param areaCode 地市编码
     * @return 供应商 权重信息
     */
    public static Optional<List<SupplierWeight>> getSupplierList(String vccId, String areaCode) {
        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = get(vccId);
        if (!businessInfoOptional.isPresent()) {
            return Optional.empty();
        }
        PrivateCorpBusinessInfoDTO businessInfoDTO = businessInfoOptional.get();
        Map<String, List<SupplierWeight>> supplierStrategy = businessInfoDTO.getSupplierStrategy();
        if (!Optional.ofNullable(supplierStrategy).isPresent()) {
            return Optional.empty();
        }

        return Optional.ofNullable(supplierStrategy.get(areaCode));
    }

    public static int size() {
        return CACHE.size();
    }

    public static CopyOnWriteMap<String, PrivateCorpBusinessInfoDTO> all() {
        return CACHE;
    }

}
