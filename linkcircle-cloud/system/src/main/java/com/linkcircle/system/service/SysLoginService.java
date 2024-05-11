package com.linkcircle.system.service;

import com.linkcircle.basecom.common.LoginUserInfo;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.system.dto.SysCaptchaDTO;
import com.linkcircle.system.dto.SysLoginReqDTO;
import com.linkcircle.system.dto.SysLoginResDTO;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/1 10:27
 */

public interface SysLoginService {

    /**
     * 登录
     * @param sysLoginReqDto
     * @param ip
     * @return
     */
    Result<SysLoginResDTO> login(SysLoginReqDTO sysLoginReqDto, String ip);

    /**
     * token校验
     * @param token
     * @return
     */
    Result tokenVerify(String token);
    /**
     * 生成图形验证码
     * @return
     */
    SysCaptchaDTO generateCaptcha();

    /**
     * 登出
     * @param ip
     * @return
     */
    Result<String> logout(String ip);

    /**
     * 获取登录信息
     * @param loginUserInfo
     * @return
     */
    SysLoginResDTO getLoginResult(LoginUserInfo loginUserInfo);
}
