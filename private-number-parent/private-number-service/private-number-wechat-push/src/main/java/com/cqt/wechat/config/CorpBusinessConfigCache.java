package com.cqt.wechat.config;

import com.cqt.common.util.CopyOnWriteMap;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;

import java.util.Map;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/7/25 17:47
 * 企业业务配置信息缓存
 */
public class CorpBusinessConfigCache {

    private static final Map<String, PrivateCorpBusinessInfoDTO> CACHE = new CopyOnWriteMap<>();

    public static void put(String vccId, PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO) {
        CACHE.put(vccId, privateCorpBusinessInfoDTO);
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
     * 该企业的地市 是否调用
     *
     * @param vccId    企业id
     * @param areaCode 地市编码
     * @return
     */
    public static boolean containsThirdAreaCodeKey(String vccId, String areaCode) {

        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = get(vccId);
        if (!businessInfoOptional.isPresent()) {
            return false;
        }

        PrivateCorpBusinessInfoDTO businessInfoDTO = businessInfoOptional.get();
        Map<String, String> thirdAreaCode = businessInfoDTO.getThirdAreaCode();
        Optional<Map<String, String>> mapOptional = Optional.ofNullable(thirdAreaCode);
        if (!mapOptional.isPresent()) {
            return false;
        }
        String key = vccId + ":" + areaCode;

        return thirdAreaCode.containsKey(key);
    }

    public static Optional<String> getThirdSupplierId(String vccId, String areaCode) {

        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = get(vccId);
        if (!businessInfoOptional.isPresent()) {
            return Optional.empty();
        }

        PrivateCorpBusinessInfoDTO businessInfoDTO = businessInfoOptional.get();
        Map<String, String> thirdAreaCode = businessInfoDTO.getThirdAreaCode();
        Optional<Map<String, String>> mapOptional = Optional.ofNullable(thirdAreaCode);
        if (!mapOptional.isPresent()) {
            return Optional.empty();
        }
        String key = vccId + ":" + areaCode;
        return Optional.ofNullable(thirdAreaCode.get(key));
    }

    public static int size() {
        return CACHE.size();
    }

    public static Map<String, PrivateCorpBusinessInfoDTO> all() {
        return CACHE;
    }

}
