package com.repush.util;

import cn.hutool.core.util.StrUtil;

/**
 * @program: tx-mt-hide-num-parent
 * @description: 如何识别和替换字符串中的 Emoji 表情
 * @author: yy
 * @create: 2023-11-05 23:33
 **/


public class EmojiUtils {

    /**
     *
     * @param text
     * @return
     */
    public static String replaceEmojis(String text) {
        if(StrUtil.isBlank (text)){
            return text;
        }
        StringBuilder sb = new StringBuilder();
        int length = text.length();
        int codepoint;

        for (int offset = 0; offset < length; offset += Character.charCount(codepoint)) {
            codepoint = text.codePointAt(offset);

            if (Character.charCount(codepoint) > 1) {
                // 如果当前字符是 Emoji 表情，则跳过
                continue;
            }

            // 进行其他文本处理操作，比如移除特定字符

            sb.appendCodePoint(codepoint);
        }

        return sb.toString();
    }


    public static String removeFourChar(String content) {

        byte[] conbyte = content.getBytes();

        for (int i = 0; i < conbyte.length; i++) {

            if ((conbyte[i] & 0xF8) == 0xF0) {

                for (int j = 0; j < 4; j++) {

                    conbyte[i+j]=0x30;

                }

                i += 3;

            }

        }

        content = new String(conbyte);
        return content.replaceAll("0000", "");

    }
}
