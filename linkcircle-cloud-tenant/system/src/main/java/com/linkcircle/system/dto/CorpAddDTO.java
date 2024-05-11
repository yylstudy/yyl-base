package com.linkcircle.system.dto;

import com.linkcircle.basecom.validators.Email;
import com.linkcircle.basecom.validators.Phone;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
@Schema(description = "企业")
public class CorpAddDTO {

    @Schema(description = "企业ID")
    @NotBlank(message = "企业ID不能为空")
    private String id;

    @Schema(description = "企业名称")
    @NotBlank(message = "企业名称不能为空")
    @Size(max = 100, message = "企业名称最多100个字符")
    private String name;

    @Schema(description = "业务菜单")
    private List<BusinessMenuDTO> businessMenus;

    @Schema(description = "邮箱")
    @Email
    private String email;

    @Schema(description = "手机号")
    @Phone
    private String phone;
}
