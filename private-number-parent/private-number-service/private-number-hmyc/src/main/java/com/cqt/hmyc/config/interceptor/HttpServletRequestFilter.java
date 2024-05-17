package com.cqt.hmyc.config.interceptor;

import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author linshiqiang
 * @date 2022-02-22 21:07
 */
@Order(1)
@WebFilter(filterName = "httpServletRequestFilter", urlPatterns = "/api/v1/bind/*")
public class HttpServletRequestFilter implements Filter {

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        ServletRequest requestWrapper = null;
        if(request instanceof HttpServletRequest) {
            requestWrapper = new HttpRequestWrapper((HttpServletRequest) request);
        }

        // 获取请求中的流如何，将取出来的字符串，再次转换成流，然后把它放入到新request对象中
        // 在chain.doFiler方法中传递新的request对象
        if(requestWrapper == null) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(requestWrapper, response);
        }
    }
    @Override
    public void init(FilterConfig arg0) throws ServletException {}
}