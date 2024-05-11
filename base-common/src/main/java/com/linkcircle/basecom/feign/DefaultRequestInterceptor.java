package com.linkcircle.basecom.feign;

import com.linkcircle.basecom.constants.CommonConstant;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 默认的feign拦截器
 * @createTime 2024/4/23 18:26
 */

public class DefaultRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader(CommonConstant.TOKEN_HEADER_KEY);
        if(StringUtils.hasText(token)){
            requestTemplate.header(CommonConstant.TOKEN_HEADER_KEY, token);
        }
    }
}
