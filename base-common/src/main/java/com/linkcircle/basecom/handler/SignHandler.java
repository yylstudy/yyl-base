package com.linkcircle.basecom.handler;

import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 签名校验中appKey处理器
 * @createTime 2024/3/26 13:57
 */

public interface SignHandler {
    /**
     * 检查appKey
     * @param appKey
     */
    void checkAppKey(String appKey);

    /**
     * 签名计算
     * @param paramMap
     * @return
     */
    String sign(Map<String, String> paramMap);
}
