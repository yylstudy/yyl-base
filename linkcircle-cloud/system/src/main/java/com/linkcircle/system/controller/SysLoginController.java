package com.linkcircle.system.controller;

import cn.hutool.extra.servlet.ServletUtil;
import com.linkcircle.basecom.common.LoginUserInfo;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.filter.LoginUserInfoHolder;
import com.linkcircle.system.dto.SysCaptchaDTO;
import com.linkcircle.system.dto.SysLoginReqDTO;
import com.linkcircle.system.dto.SysLoginResDTO;
import com.linkcircle.system.service.SysLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * @Description: 登录
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@RestController
@Tag(name = "登录/登出")
public class SysLoginController {
    @Resource
    private SysLoginService sysLoginService;

    @GetMapping("/tokenVerify")
    @Operation(summary = "token校验")
    public Result tokenVerify(@RequestParam("token") String token) {
        return sysLoginService.tokenVerify(token);
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<SysLoginResDTO> login(@Valid @RequestBody SysLoginReqDTO sysLoginReqDto, HttpServletRequest request) {
        String ip = ServletUtil.getClientIP(request);
        return sysLoginService.login(sysLoginReqDto, ip);
    }

    @GetMapping("/login/getLoginInfo")
    @Operation(summary = "获取登录结果信息")
    public Result<SysLoginResDTO> getLoginInfo() {
        LoginUserInfo loginUserInfo = LoginUserInfoHolder.get();
        SysLoginResDTO sysLoginResDto = sysLoginService.getLoginResult(loginUserInfo);
        return Result.ok(sysLoginResDto);
    }

    @Operation(summary = "退出登陆")
    @PostMapping("/login/logout")
    public Result<String> logout(HttpServletRequest request) {
        String ip = ServletUtil.getClientIP(request);
        return sysLoginService.logout(ip);
    }

    @Operation(summary = "获取验证码")
    @GetMapping("/login/getCaptcha")
    public Result<SysCaptchaDTO> getCaptcha() {
        return Result.ok(sysLoginService.generateCaptcha());
    }

}
