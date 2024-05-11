package com.yyl;

import cn.hutool.jwt.JWT;
import com.linkcircle.basecom.handler.defaultHandler.DefaultTokenHandler;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/26 14:25
 */

public class MyTokenHandler2 extends DefaultTokenHandler {
    @Override
    protected MyLoginUserInfo getLoginUserInfo(JWT jwt) {
        if(jwt==null){
            return null;
        }
        String phone = jwt.getPayloads().getStr("phone");
        String username = jwt.getPayloads().getStr("username");
        Long userId = jwt.getPayloads().getLong("id");
        MyLoginUserInfo loginUserInfo = new MyLoginUserInfo();
        loginUserInfo.setPhone(phone);
        loginUserInfo.setUsername(username);
        loginUserInfo.setId(userId);
        loginUserInfo.setSex("男");
        return loginUserInfo;
    }
}
