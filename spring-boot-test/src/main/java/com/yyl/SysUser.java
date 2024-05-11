package com.yyl;

import com.linkcircle.basecom.annotation.Dict;
import com.linkcircle.basecom.validators.CheckEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/25 18:24
 */
@Data
public class SysUser implements Serializable {
    @Schema(description = "用户名",requiredMode = Schema.RequiredMode.REQUIRED)
    private String usernme;
    @Schema(description = "手机号",requiredMode = Schema.RequiredMode.REQUIRED)
    private String phone;
    @Schema(description = "邮箱",requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    @Schema(description = "性别",requiredMode = Schema.RequiredMode.REQUIRED)
    @CheckEnum(value = SexEnum.class,message = "性别必须在指定范围 {value}")
    @NotEmpty(message = "性别不能为空")
    @Dict(dictCode = "sex",dictText = "typeName")
    private String sex;

    private boolean testa = false;
}
