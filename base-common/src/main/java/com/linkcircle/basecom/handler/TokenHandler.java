package com.linkcircle.basecom.handler;

import com.linkcircle.basecom.common.LoginUserInfo;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description token处理器
 * @createTime 2024/3/26 13:57
 */

public interface TokenHandler {
    /**
     * 是否跳过
     * @param path 路径
     * @return
     */
    boolean isSkip(String path);
    /**
     * token处理
     * @param request
     * @return
     */
    void checkAndHandleTokenUser(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException;
}
