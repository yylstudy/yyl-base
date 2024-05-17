package com.cqt.client.aspect;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.annotations.Auth;
import com.cqt.base.contants.HeaderNameConstant;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.exception.AuthException;
import com.cqt.client.service.TokenVerifyService;
import com.cqt.model.common.CloudCallCenterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcContextAttachment;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author linshiqiang
 * date:  2023-11-15 15:05
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestAuthInterceptor implements HandlerInterceptor {

    private final TokenVerifyService tokenVerifyService;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) {
        String language = getLanguage(request);
        RpcContextAttachment serverContext = RpcContext.getServerContext();
        serverContext.setAttachment(HeaderNameConstant.LANGUAGE, language);
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.hasMethodAnnotation(Auth.class)) {
                // auth
                String sdkToken = request.getHeader(HeaderNameConstant.SDK_TOKEN);
                boolean pass = tokenVerifyService.checkToken(sdkToken);
                if (pass) {
                    return true;
                }
                throw new AuthException(SdkErrCode.SDK_TOKEN_INVALID);
            }
        }
        return true;
    }

    private String getLanguage(HttpServletRequest request) {
        String lang = request.getHeader(HeaderNameConstant.LANGUAGE);
        if (StrUtil.isNotEmpty(lang)) {
            return lang;
        }
        return cloudCallCenterProperties.getDefaultConfig().getLanguage();
    }

}
