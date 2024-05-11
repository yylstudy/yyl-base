package com.linkcircle.system.dto;

import com.linkcircle.basecom.validators.CheckEnum;
import com.linkcircle.basecom.validators.Email;
import com.linkcircle.basecom.validators.Phone;
import com.linkcircle.system.common.GenderEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysUserAddDTO {

    @Schema(description = "姓名")
    @NotNull(message = "姓名不能为空")
    @Size(max = 30, message = "姓名最多30字符")
    private String username;

    @CheckEnum(value = GenderEnum.class, message = "性别必须在指定范围 {value}")
    private Integer gender;

    @Schema(description = "部门id")
    private Long departId;

    @Schema(description = "密码",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "密码不能为空")
    private String password;

    @Schema(description = "是否启用")
    @NotNull(message = "是否被禁用不能为空")
    private Boolean disabledFlag;

    @Schema(description = "手机号")
    @NotNull(message = "手机号不能为空")
    @Phone
    private String phone;
    @Schema(description = "邮箱")
    @NotNull(message = "邮箱不能为空")
    @Email
    private String email;


    @Schema(description = "角色列表")
    private List<Long> roleIdList;
}
