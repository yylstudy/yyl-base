package com.cqt.model.bind.dto;

import com.cqt.model.common.BaseAuth;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2021/9/9 15:10
 * 修改绑定关系对象
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBindDTO extends BaseAuth implements Serializable  {

    private static final long serialVersionUID = -3099109269119152038L;

    @JsonProperty("bind_id")
    @ApiModelProperty(value = "绑定时返回的绑定ID")
    @NotBlank(message = "bindId 不能为空")
    private String bindId;

    @JsonProperty("expiration")
    @ApiModelProperty(value = "新的有效期 新的过期时间按当前时间+新的有效期计算。单位：秒")
    @NotBlank(message = "expiration 不能为空")
    private String expiration;
}
