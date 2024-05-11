package com.linkcircle.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysDictItemAddDTO {

    @Schema(description = "dictId")
    @NotNull(message = "dictId不能为空")
    private Long dictId;

    @Schema(description = "编码")
    @NotBlank(message = "编码不能为空")
    @Size(max = 50,message = "编码太长了，不能超过50字符")
    private String itemValue;

    @Schema(description = "名称")
    @NotBlank(message = "名称不能为空")
    @Size(max = 50,message = "名称太长了，不能超过50字符")
    private String itemText;

    @Schema(description = "排序")
    @NotNull(message = "排序不能为空")
    private Integer sort;

}