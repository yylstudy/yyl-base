package com.linkcircle.system.dto;

import com.linkcircle.basecom.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysUserDTO {

    @Schema(description = "主键id")
    private Long id;
    private Integer gender;

    @Schema(description = "真实姓名")
    private String username;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "部门id")
    private Long departId;
    @Schema(description = "部门名称")
    private String departName;

    @Schema(description = "是否被禁用")
    private Boolean disabledFlag;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "角色列表")
    private List<Long> roleIdList = new ArrayList<>();

    @Schema(description = "角色名称列表")
    private List<String> roleNameList;
}
