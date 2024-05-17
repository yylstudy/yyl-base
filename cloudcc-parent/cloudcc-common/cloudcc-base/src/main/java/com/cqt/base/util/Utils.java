package com.cqt.base.util;

import cn.hutool.core.util.StrUtil;

import java.util.Map;

/**
 * @author linshiqiang
 * date 2023-06-18 19:42:00
 */
public class Utils {

    /**
     * 格式化文本，使用 ${varName} 占位<br>
     * map = {a: "aValue", b: "bValue"} format("${a} and ${b}", map) ---=》 aValue and bValue
     *
     * @param template   文本模板，被替换的部分用 ${key} 表示
     * @param map        参数值对
     * @param ignoreNull 是否忽略 {@code null} 值，忽略则 {@code null} 值对应的变量不被替换，否则替换为""
     * @return 格式化后的文本
     * @since 5.7.10
     */
    public static String format(CharSequence template, Map<?, ?> map, boolean ignoreNull) {
        if (null == template) {
            return null;
        }
        if (null == map || map.isEmpty()) {
            return template.toString();
        }

        String template2 = template.toString();
        String value;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            value = StrUtil.utf8Str(entry.getValue());
            if (null == value && ignoreNull) {
                continue;
            }
            template2 = StrUtil.replace(template2, "${" + entry.getKey() + "}", value);
        }
        return template2;
    }
}
