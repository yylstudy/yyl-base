package com.linkcircle.redis.config;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 用户ID处理器
 * @createTime 2024/3/27 18:44
 */

public interface UserIdHandler {
    /**
     * 获取用户ID
     * @return
     */
    String getUserId();
}
