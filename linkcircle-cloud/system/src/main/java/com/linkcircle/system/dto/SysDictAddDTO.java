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
public class SysDictAddDTO {

    @Schema(description = "编码")
    @NotBlank(message = "编码不能为空")
    @Size(max = 50,message = "编码太长了，不能超过50字符")
    private String dictCode;

    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    @Size(max = 50,message = "名称太长了，不能超过50字符")
    private String dictName;

    @Schema(description = "备注")
    @Size(max = 500,message = "备注太长了")
    private String remark;
}