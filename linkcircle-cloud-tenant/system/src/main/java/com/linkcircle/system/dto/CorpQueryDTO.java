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
@Schema(description = "企业查询")
public class CorpQueryDTO extends PageParam {

    @Schema(description = "企业名称")
    @Size(max = 50, message = "企业名称最多50字符")
    private String name;
    @Schema(description = "企业ID")
    @Size(max = 50, message = "企业ID最多50字符")
    private String id;
}
