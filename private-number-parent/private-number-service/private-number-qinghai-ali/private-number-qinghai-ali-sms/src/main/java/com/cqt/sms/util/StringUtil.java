package com.cqt.sms.util;

import lombok.extern.log4j.Log4j2;

/**
 * @author youngder
 */
@Log4j2
public class StringUtil {

    public static final int SMS_ONE = 70;
    public static final int SMS_MULTI_ONE = 67;

    /**
     * 计算短信条数
     * rule:
     * 1、短信字数<=70个字数，按照70个字数一条短信计算
     * 2、短信字数>70个字数，即为长短信，按照67个字数记为一条短信计算
     **/
    public static int getSmsNumber(String smsContent) {
        int length = smsContent.length();
        if (length <= SMS_ONE) {
            return 1;
        }
        int smsNumber = length / SMS_MULTI_ONE + 1;
        log.info("共{}条短信，{}个字符", smsNumber, length);
        return smsNumber;
    }
}
