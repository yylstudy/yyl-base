package com.linkcircle.basecom.handler.defaultHandler;

import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.common.LoginUserInfo;
import com.linkcircle.basecom.config.ApplicationContextHolder;
import com.linkcircle.basecom.config.DefaultSkipUrl;
import com.linkcircle.basecom.config.SkipUrlConfig;
import com.linkcircle.basecom.constants.CommonConstant;
import com.linkcircle.basecom.filter.LoginUserInfoHolder;
import com.linkcircle.basecom.handler.TokenHandler;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 默认token处理器，若想自定义，继承此类，覆盖相应方法
 * @createTime 2024/3/26 13:57
 */
public class DefaultTokenHandler implements TokenHandler {
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private static volatile List<String> skipUrl;
    private static volatile String tokenHeaderKey ;
    @Override
    public boolean isSkip(String path) {
        if(skipUrl==null){
            synchronized (DefaultTokenHandler.class){
                if(skipUrl==null){
                    List<String> list = new ArrayList();
                    list.addAll(DefaultSkipUrl.getSkipUrl());
                    addCustomSkipUrl(list);
                    SkipUrlConfig skipUrlConfig = ApplicationContextHolder.getBean(SkipUrlConfig.class);
                    list.addAll(skipUrlConfig.getSkipUrl());
                    skipUrl = list;
                }
            }
        }
        return skipUrl.stream().anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }

    @Override
    public void checkAndHandleTokenUser(HttpServletRequest request,HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        String token = getToken(request);
        if(!StringUtils.hasText(token)){
            handleEmptyToken(request,response,filterChain);
            return;
        }
        Result<? extends LoginUserInfo> result = checkAndGetJwtLoginUserInfo(request,token);
        if(result.isSuccess()){
            LoginUserInfo loginUserInfo = result.getData();
            LoginUserInfoHolder.set(loginUserInfo);
            try{
                filterChain.doFilter(request, response);
            }finally {
                LoginUserInfoHolder.remove();
            }
        }else{
            writeData(response,result);
        }
    }

    /**
     * 添加自定义的skipUrl，子类可重写
     */
    protected void addCustomSkipUrl(List<String> list){

    }

    /**
     * 检查和获取jwt，这里默认不检查，为空也通过
     */
    protected String getToken(HttpServletRequest request){
        if(tokenHeaderKey==null){
            synchronized (DefaultTokenHandler.class){
                if(tokenHeaderKey==null){
                    tokenHeaderKey = ApplicationContextHolder.getEnvironment()
                            .getProperty(CommonConstant.TOKEN_HEADER_KEY_CONFIG,CommonConstant.TOKEN_HEADER_KEY);
                }
            }
        }
        String token = request.getHeader(tokenHeaderKey);
        return token;
    }

    /**
     * 处理空token，默认不处理
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    protected void handleEmptyToken(HttpServletRequest request,HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        //writeData(response,Result.error("token为空"));
        filterChain.doFilter(request, response);
    }

    /**
     * 检查和获取LoginUserInfo
     */
    protected Result<LoginUserInfo> checkAndGetJwtLoginUserInfo(HttpServletRequest request,String token){
        JWT jwt = JWTUtil.parseToken(token);
        //if(!jwt.setKey(CommonConstant.DEFAULT_SECRET.getBytes()).verify()) {
        //    return Result.errorAuth("无效的token");
        //}
        //if(!jwt.validate(0)) {
        //return Result.errorAuth("token已过期，请重新登录");
        //}
        LoginUserInfo loginUserInfo = getLoginUserInfo(jwt);
        return Result.ok(loginUserInfo);
    }
    /**
     * 根据jwt获取LoginUserInfo，子类可重写
     * @param jwt
     * @return
     */
    protected <T extends LoginUserInfo> T getLoginUserInfo(JWT jwt){
        if(jwt==null){
            return null;
        }
        String phone = jwt.getPayloads().getStr("phone");
        String username = jwt.getPayloads().getStr("username");
        Long userId = jwt.getPayloads().getLong("id");
        LoginUserInfo loginUserInfo = new LoginUserInfo();
        loginUserInfo.setPhone(phone);
        loginUserInfo.setUsername(username);
        loginUserInfo.setId(userId);
        return (T)loginUserInfo;
    }

    private void writeData(HttpServletResponse response,Result result){
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        try {
            response.getWriter().write(JSONUtil.toJsonStr(result));
            response.flushBuffer();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
