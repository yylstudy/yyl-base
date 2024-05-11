package com.linkcircle.system.dto;

import com.linkcircle.system.common.CommonConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysUserUpdatePasswordDTO {

    @Schema(hidden = true)
    private Long userId;

    @Schema(description = "原密码")
    @NotBlank(message = "原密码不能为空")
    @Pattern(regexp = CommonConstant.PWD_REGEXP, message = "密码不小于8位，必须包含数字、字母、特殊符号")
    private String oldPassword;

    @Schema(description = "新密码")
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = CommonConstant.PWD_REGEXP, message = "密码不小于8位，必须包含数字、字母、特殊符号")
    private String newPassword;
}
