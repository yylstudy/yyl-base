package com.linkcircle.system.dto;

import com.linkcircle.basecom.page.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
@Schema(description = "参数查询")
public class SysConfigQueryDTO extends PageParam {

    @Schema(description = "key")
    @Size(max = 50, message = "key最多50字符")
    private String key;
}
