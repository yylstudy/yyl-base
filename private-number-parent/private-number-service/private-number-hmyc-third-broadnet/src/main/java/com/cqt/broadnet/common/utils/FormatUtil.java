package com.cqt.broadnet.common.utils;

import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author linshiqiang
 * date:  2023-06-06 10:09
 */
public class FormatUtil {

    /**
     * 格式化号码为标准号码 去86等
     * TODO 固话
     */
    public static String getNumber(String number) {
        if (StrUtil.isEmpty(number)) {
            return number;
        }
        if (ReUtil.isMatch(PatternPool.MOBILE, number)) {
            return number.startsWith("86") ? StrUtil.removePrefix(number, "86") : number;
        }
        return number;
    }

    /**
     * 格式化号码为号码格式遵循国际电信联盟定义的E.164标准
     */
    public static String getNumber164(String number) {
        if (StrUtil.isEmpty(number)) {
            return number;
        }
        if (ReUtil.isMatch(PatternPool.MOBILE, number)) {
            return number.startsWith("86") ? number : "86" + number;
        }
        return number;
    }
}
