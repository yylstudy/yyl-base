package com.linkcircle.basecom.filter;

import com.linkcircle.basecom.handler.TokenHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/25 11:07
 */
public class JwtFilter extends OncePerRequestFilter implements Ordered {
    @Autowired
    private TokenHandler tokenHandler;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (tokenHandler.isSkip(requestURI)){
            filterChain.doFilter(request, response);
            return;
        }
        tokenHandler.checkAndHandleTokenUser(request,response,filterChain);

    }
    @Override
    public int getOrder() {
        return 1;
    }

    public TokenHandler getTokenHandler() {
        return tokenHandler;
    }

    public void setTokenHandler(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
    }
}
