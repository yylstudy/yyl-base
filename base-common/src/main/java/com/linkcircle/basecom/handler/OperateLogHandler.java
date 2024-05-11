package com.linkcircle.basecom.handler;

import com.linkcircle.basecom.common.LoginUserInfo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/25 10:37
 */

public interface OperateLogHandler {
    /**
     * 添加日志
     * @param isSuccess 是否成功
     * @param failReason 失败原因
     * @param content 操作日志
     * @param requestUrl 请求url
     * @param operateMethod 操作方法
     * @param costTime 耗时
     * @param loginUserInfo token
     */
    void addLog(HttpServletRequest request, boolean isSuccess, String failReason, String content,
                String requestUrl, String operateMethod, long costTime, LoginUserInfo loginUserInfo, String ip);
}
