package com.linkcircle.system.config;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.linkcircle.basecom.common.LoginUserInfo;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.handler.defaultHandler.DefaultTokenHandler;
import com.linkcircle.system.common.SystemLoginUserInfo;
import com.linkcircle.system.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/28 14:03
 */

public class SystemTokenHandler extends DefaultTokenHandler {
    @Override
    protected Result<LoginUserInfo> checkAndGetJwtLoginUserInfo(HttpServletRequest request,String token) {
        return super.checkAndGetJwtLoginUserInfo(request,token);
    }

    @Override
    protected SystemLoginUserInfo getLoginUserInfo(JWT jwt) {
        String phone = jwt.getPayloads().getStr("phone");
        String email = jwt.getPayloads().getStr("email");
        String username = jwt.getPayloads().getStr("username");
        String departId = jwt.getPayloads().getStr("departId");
        Long userId = jwt.getPayloads().getLong("id");
        String corpId = jwt.getPayloads().getStr("corpId");
        SystemLoginUserInfo systemLoginUserInfo = new SystemLoginUserInfo();
        systemLoginUserInfo.setPhone(phone);
        systemLoginUserInfo.setEmail(email);
        systemLoginUserInfo.setUsername(username);
        systemLoginUserInfo.setId(userId);
        systemLoginUserInfo.setCorpId(corpId);
        systemLoginUserInfo.setDepartId(departId);
        return systemLoginUserInfo;
    }
}
