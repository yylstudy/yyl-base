package com.linkcircle.system.dto;

import com.linkcircle.system.entity.Corp;
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
public class SysLoginResDTO {

    @Schema(description = "是否需要切换企业")
    private boolean switchCorp;

    @Schema(description = "企业列表")
    private List<Corp> corpList;

    @Schema(description = "用户id")
    private Long id;

    @Schema(description = "token")
    private String token;

    @Schema(description = "菜单列表")
    private List<SysMenuDTO> menuList;

    @Schema(description = "用户名称")
    private String username;

    private Integer gender;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "部门id")
    private Long departId;

    @Schema(description = "部门名称")
    private String departName;

}
