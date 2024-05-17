package com.cqt.cloudcc.manager.interceptor;

import com.cqt.base.annotations.Auth;
import com.cqt.base.contants.HeaderNameConstant;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.base.exception.AuthException;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.common.CloudCallCenterProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AuthInterceptor implements HandlerInterceptor {

    private final CommonDataOperateService commonDataOperateService;

    private final CloudCallCenterProperties cloudCallCenterProperties;

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.hasMethodAnnotation(Auth.class)) {
                if (!cloudCallCenterProperties.getAuth()) {
                    return true;
                }
                // auth
                String sdkToken = request.getHeader(HeaderNameConstant.SDK_TOKEN);
                boolean pass = commonDataOperateService.checkToken(sdkToken);
                if (pass) {
                    return true;
                }
                throw new AuthException(SdkErrCode.SDK_TOKEN_INVALID);
            }
        }
        return true;
    }

}
