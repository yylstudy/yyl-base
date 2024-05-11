package com.linkcircle.system.dto;

import com.linkcircle.basecom.page.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysDictItemQueryDTO extends PageParam {

    @Schema(description = "dictId")
    @NotNull(message = "dictId不能为空")
    private Long dictId;

    @Schema(description = "搜索词")
    private String searchWord;
}