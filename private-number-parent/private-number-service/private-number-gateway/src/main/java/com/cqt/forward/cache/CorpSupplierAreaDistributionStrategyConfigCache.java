package com.cqt.forward.cache;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.util.CopyOnWriteMap;
import com.cqt.model.corpinfo.dto.SupplierWeight;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/7/25 17:47
 * 企业-供应商地市号码分配策略缓存
 */
public class CorpSupplierAreaDistributionStrategyConfigCache {

    private static final CopyOnWriteMap<String, List<SupplierWeight>> CACHE = new CopyOnWriteMap<>();

    public static synchronized void putAll(Map<String, List<SupplierWeight>> map) {
        CACHE.putAll(map);
        for (Map.Entry<String, List<SupplierWeight>> entry : CACHE.entrySet()) {
            String key = entry.getKey();
            List<SupplierWeight> list = map.get(key);
            if (CollUtil.isEmpty(list)) {
                CACHE.remove(key);
            }
        }
    }

    public static CopyOnWriteMap<String, List<SupplierWeight>> all() {
        return CACHE;
    }

    public static void clear() {
        CACHE.clear();
    }

    /**
     * 获取供应商 权重信息
     *
     * @param vccId        企业id
     * @param areaCode     地市编码
     * @param businessType 业务模式
     * @return 供应商 权重信息
     */
    public static Optional<List<SupplierWeight>> getSupplierList(String vccId, String areaCode, String businessType) {
        String key = vccId + StrUtil.COLON + areaCode + StrUtil.COLON + businessType.toUpperCase();
        List<SupplierWeight> supplierWeightList = CACHE.get(key);
        if (CollUtil.isEmpty(supplierWeightList)) {
            return Optional.empty();
        }

        return Optional.of(supplierWeightList);
    }

    public static int size() {
        return CACHE.size();
    }

}
