package com.cqt.common.util;

import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.cqt.common.constants.SystemConstant;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author linshiqiang
 * date 2022/3/4 14:04
 */
@Slf4j
public class AuthUtil {

    public static String createSign(TreeMap<String, Object> params, String vccId, String secretKey) {
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
     * 重新设置ts和sign
     */
    public static String resetSign(ObjectMapper objectMapper, PrivateCorpBusinessInfoDTO businessInfoDTO, String pushData) {
        try {
            TreeMap<String, Object> treeMap = objectMapper.readValue(pushData, new TypeReference<TreeMap<String, Object>>() {
            });
            treeMap.put(SystemConstant.TS, System.currentTimeMillis());
            String sign = AuthUtil.createSign(treeMap, businessInfoDTO.getVccId(), businessInfoDTO.getSecretKey());
            treeMap.put(SystemConstant.SIGN, sign);
            return objectMapper.writeValueAsString(treeMap);
        } catch (JsonProcessingException e) {
            log.error("pushData json: {}, 解析失败: ", pushData, e);
            return pushData;
        }
    }
}
