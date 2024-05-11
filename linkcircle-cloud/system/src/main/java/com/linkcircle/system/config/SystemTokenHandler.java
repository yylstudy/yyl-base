package com.linkcircle.system.config;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.linkcircle.basecom.common.LoginUserInfo;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.handler.defaultHandler.DefaultTokenHandler;
import com.linkcircle.system.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/2 9:12
 */

public class SystemTokenHandler extends DefaultTokenHandler {
    @Override
    protected <T extends LoginUserInfo> T getLoginUserInfo(JWT jwt) {
        return super.getLoginUserInfo(jwt);
    }
}
