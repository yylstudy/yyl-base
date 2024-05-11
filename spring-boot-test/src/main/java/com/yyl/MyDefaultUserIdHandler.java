package com.yyl;

import com.linkcircle.basecom.common.LoginUserInfo;
import com.linkcircle.basecom.filter.LoginUserInfoHolder;
import com.linkcircle.redis.config.UserIdHandler;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/28 9:48
 */

public class MyDefaultUserIdHandler implements UserIdHandler {
    @Override
    public String getUserId() {
        LoginUserInfo loginUserInfo = LoginUserInfoHolder.get();
        if(loginUserInfo!=null){
            return String.valueOf(loginUserInfo.getId());
        }
        return "";
    }
}
