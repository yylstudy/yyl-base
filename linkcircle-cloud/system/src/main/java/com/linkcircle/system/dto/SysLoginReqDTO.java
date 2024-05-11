package com.linkcircle.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysLoginReqDTO {
    @Schema(description = "登录账号")
    @NotBlank(message = "登录账号不能为空")
    @Size(max = 30, message = "登录账号最多30字符")
    private String username;
    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;
    @Schema(description = "验证码")
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
    @Schema(description = "验证码uuid标识")
    @NotBlank(message = "验证码uuid标识不能为空")
    private String captchaUuid;
}
