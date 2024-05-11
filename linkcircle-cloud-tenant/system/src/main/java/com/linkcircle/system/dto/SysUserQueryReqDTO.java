package com.linkcircle.system.dto;

import com.linkcircle.basecom.page.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysUserQueryReqDTO extends PageParam {

    @Schema(description = "手机号/邮箱")
    private String phoneOrEmail;

    @Schema(description = "部门id")
    private Long departId;

    @Schema(description = "是否禁用")
    private Boolean disabledFlag;

//    @Schema(description = "用户id集合")
//    @Size(max = 99, message = "最多查询99个用户")
//    private List<Long> employeeIdList;

    @Schema(description = "删除标识", hidden = true)
    private Boolean deletedFlag;

    @Schema(description = "企业ID")
    private String corpId;
}
