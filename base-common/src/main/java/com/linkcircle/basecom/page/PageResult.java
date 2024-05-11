package com.linkcircle.basecom.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class PageResult<T> {

    /**
     * 当前页
     */
    @Schema(description = "当前页")
    private Long pageNum;

    /**
     * 每页的数量
     */
    @Schema(description = "每页的数量")
    private Long pageSize;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数")
    private Long total;

    /**
     * 总页数
     */
    @Schema(description = "总页数")
    private Long pages;

    /**
     * 结果集
     */
    @Schema(description = "结果集")
    private List<T> list;

    @Schema(description = "是否为空")
    private Boolean emptyFlag;

}
