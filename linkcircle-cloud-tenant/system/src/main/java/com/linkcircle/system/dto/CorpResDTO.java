package com.linkcircle.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/6 9:12
 */
@Data
@Schema(description = "企业查询")
public class CorpResDTO {
    @Schema(description = "企业ID")
    @NotBlank(message = "企业ID不能为空")
    private String id;
    @Schema(description = "企业名称")
    @NotBlank(message = "企业名称不能为空")
    private String name;

    private String businessStr;
    private List<String> businessList = new ArrayList<>();
}
