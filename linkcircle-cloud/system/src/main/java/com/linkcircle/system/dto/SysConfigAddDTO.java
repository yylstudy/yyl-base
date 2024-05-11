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
@Schema(description = "参数")
public class SysConfigAddDTO {

    @Schema(description = "key")
    @NotBlank(message = "参数key不能为空")
    @Size(max = 255, message = "参数key最多255个字符")
    private String key;

    @Schema(description = "value")
    @NotBlank(message = "参数的值不能为空")
    @Size(max = 60000, message = "参数的值最多60000个字符")
    private String value;

    @Schema(description = "参数名称")
    @NotBlank(message = "参数名称不能为空")
    @Size(max = 255, message = "参数名称最多255个字符")
    private String name;

    @Schema(description = "备注")
    @Size(max = 255, message = "备注最多255个字符")
    private String remark;
}
