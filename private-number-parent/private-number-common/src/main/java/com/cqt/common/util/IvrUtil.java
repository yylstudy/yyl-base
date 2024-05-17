package com.cqt.common.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;

import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/2/21 10:18
 */
public class IvrUtil {

    public static String getIvrName(String ivrCode, Map<String, String> audioCache) {
        if (StrUtil.isEmpty(ivrCode)) {
            return "";
        }
        // 从语音文件代码字典中获取语音文件名
        String voice = Convert.toStr(audioCache.get(ivrCode), "");
        if (StrUtil.isNotEmpty(voice)) {
            return voice;
        }
        return ivrCode;
    }
}
