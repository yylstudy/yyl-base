package com.linkcircle.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/13 10:18
 */
@Data
public class BusinessMenuDTO implements Serializable {
    @Schema(description = "业务")
    @NotBlank(message = "业务不能为空")
    private String business;

//    private List<Long> menuIdList;
}
