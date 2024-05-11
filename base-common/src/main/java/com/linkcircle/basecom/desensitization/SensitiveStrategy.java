package com.linkcircle.basecom.desensitization;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 脱敏策略类
 * @createTime 2024/4/12 17:08
 */

public interface SensitiveStrategy {
    /**
     * 脱敏处理
     * @param value
     * @return
     */
    String handle(String value);
}
