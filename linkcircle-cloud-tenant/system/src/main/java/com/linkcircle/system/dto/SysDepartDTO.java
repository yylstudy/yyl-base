package com.linkcircle.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysDepartDTO implements Serializable {

    @Schema(description = "部门id")
    private Long id;

    @Schema(description = "部门名称")
    private String name;

    @Schema(description = "企业ID")
    private String corpId;

    @Schema(description = "部门负责人姓名")
    private String managerName;

    @Schema(description = "父级部门id")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

}
