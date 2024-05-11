package com.linkcircle.system.service;

import com.linkcircle.basecom.common.Result;
import com.linkcircle.system.dto.SysCaptchaDTO;
import com.linkcircle.system.dto.SysCaptchaLoginReqDTO;
import com.linkcircle.system.dto.SysLoginReqDTO;
import com.linkcircle.system.dto.SysLoginResDTO;
import com.linkcircle.system.common.SystemLoginUserInfo;

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
     * 切换企业
     * @param corpId
     * @return
     */
    Result<SysLoginResDTO> switchCorp(String corpId);
    /**
     * 验证码登录
     * @param sysCaptchaLoginReqDto
     * @param ip
     * @return
     */
    Result<SysLoginResDTO> captchaLogin(SysCaptchaLoginReqDTO sysCaptchaLoginReqDto, String ip);

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
     * @param systemLoginUserInfo
     * @return
     */
    SysLoginResDTO getLoginResult(SystemLoginUserInfo systemLoginUserInfo);
}
