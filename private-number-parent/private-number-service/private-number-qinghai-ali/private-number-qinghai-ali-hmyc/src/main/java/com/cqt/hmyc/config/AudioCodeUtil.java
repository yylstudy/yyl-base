package com.cqt.hmyc.config;

import cn.hutool.core.util.StrUtil;

/**
 * @author linshiqiang
 * date:  2023-01-28 15:42
 */
public class AudioCodeUtil {

    /**
     * 获取放音文件名
     */
    public static String getAudioFileName(String audioCode) {
        if (StrUtil.isEmpty(audioCode)) {
            return "";
        }
        String value = AudioCodeCache.get(audioCode);
        return StrUtil.isEmpty(value) ? audioCode : value;
    }
}
