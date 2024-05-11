package com.linkcircle.system.dto;

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
public class SysDepartTreeDTO extends SysDepartDTO {

    @Schema(description = "同级上一个元素id")
    private Long preId;

    @Schema(description = "同级下一个元素id")
    private Long nextId;

    @Schema(description = "子部门")
    private List<SysDepartTreeDTO> children;

    @Schema(description = "自己和所有递归子部门的id集合")
    private List<Long> selfAndAllChildrenIdList;

}
