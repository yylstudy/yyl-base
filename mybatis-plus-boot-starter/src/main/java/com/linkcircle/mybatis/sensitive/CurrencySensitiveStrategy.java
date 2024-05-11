package com.linkcircle.mybatis.sensitive;

import cn.hutool.core.util.DesensitizedUtil;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/12 10:50
 */

public class CurrencySensitiveStrategy {
    /**
     * 身份证号
     */
    public static class ChineseName implements SensitiveStrategy{
        @Override
        public String handle(String value) {
            return DesensitizedUtil.desensitized(value,DesensitizedUtil.DesensitizedType.CHINESE_NAME);
        }
    }

    /**
     * 身份证号
     */
    public static class IdCard implements SensitiveStrategy{
        @Override
        public String handle(String value) {
            return DesensitizedUtil.desensitized(value,DesensitizedUtil.DesensitizedType.ID_CARD);
        }
    }

    /**
     * 固话
     */
    public static class FixedPhone implements SensitiveStrategy{
        @Override
        public String handle(String value) {
            return DesensitizedUtil.desensitized(value,DesensitizedUtil.DesensitizedType.FIXED_PHONE);
        }
    }

    /**
     * 手机号
     */
    public static class MobilePhone implements SensitiveStrategy{
        @Override
        public String handle(String value) {
            return DesensitizedUtil.desensitized(value,DesensitizedUtil.DesensitizedType.MOBILE_PHONE);
        }
    }

    /**
     * 地址
     */
    public static class Address implements SensitiveStrategy{
        @Override
        public String handle(String value) {
            return DesensitizedUtil.desensitized(value,DesensitizedUtil.DesensitizedType.ADDRESS);
        }
    }

    /**
     * 邮件
     */
    public static class Email implements SensitiveStrategy{
        @Override
        public String handle(String value) {
            return DesensitizedUtil.desensitized(value,DesensitizedUtil.DesensitizedType.EMAIL);
        }
    }

    /**
     * 银行卡
     */
    public static class BankCard implements SensitiveStrategy{
        @Override
        public String handle(String value) {
            return DesensitizedUtil.desensitized(value,DesensitizedUtil.DesensitizedType.BANK_CARD);
        }
    }

    /**
     * IPV4
     */
    public static class IPV4 implements SensitiveStrategy{
        @Override
        public String handle(String value) {
            return DesensitizedUtil.desensitized(value,DesensitizedUtil.DesensitizedType.IPV4);
        }
    }

    /**
     * IPV6
     */
    public static class IPV6 implements SensitiveStrategy{
        @Override
        public String handle(String value) {
            return DesensitizedUtil.desensitized(value,DesensitizedUtil.DesensitizedType.IPV6);
        }
    }
}
