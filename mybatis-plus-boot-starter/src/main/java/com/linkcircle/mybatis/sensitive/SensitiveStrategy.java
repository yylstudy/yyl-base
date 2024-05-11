package com.linkcircle.mybatis.sensitive;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/12 10:14
 */

public interface SensitiveStrategy {
    /**
     * 脱敏处理
     * @param value
     * @return
     */
    String handle(String value);
}
