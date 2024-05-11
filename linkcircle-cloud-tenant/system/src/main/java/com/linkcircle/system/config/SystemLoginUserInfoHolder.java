package com.linkcircle.system.config;

import com.linkcircle.basecom.filter.LoginUserInfoHolder;
import com.linkcircle.system.common.SystemLoginUserInfo;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/28 14:29
 */

public class SystemLoginUserInfoHolder {
    /**
     * 获取登录信息
     * @return
     */
    public static SystemLoginUserInfo getLoginUserInfo(){
        return LoginUserInfoHolder.get();
    }

    /**
     * 获取企业ID
     * @return
     */
    public static String getCorpId(){
        SystemLoginUserInfo loginUserInfo = getLoginUserInfo();
        if(loginUserInfo!=null){
            return loginUserInfo.getCorpId();
        }
        return "";
    }
}
