package com.linkcircle.basecom.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
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
public class PageParam {

    @Schema(description = "页码(不能为空)", example = "1")
    @NotNull(message = "分页参数不能为空")
    private Integer pageNum;

    @Schema(description = "每页数量(不能为空)", example = "10")
    @NotNull(message = "每页数量不能为空")
    @Max(value = 200, message = "每页最大为200")
    private Integer pageSize;

    @Schema(description = "排序字段集合")
    @Size(max = 10, message = "排序字段最多10")
    @Valid
    private List<SortItem> sortItemList;

    /**
     * 排序DTO类
     */
    @Data
    public static class SortItem {

        @Schema(description = "true正序|false倒序")
        @NotNull(message = "排序规则不能为空")
        private Boolean isAsc;

        @Schema(description = "排序字段")
        @NotBlank(message = "排序字段不能为空")
        @Size(max = 30, message = "排序字段最多30")
        private String column;
    }
}
