package com.cqt.model.supplier;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * (PrivateSupplierInfo)表实体类
 *
 * @author linshiqiang
 * @since 2022-12-21 09:47:31
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivateSupplierInfo {

    /**
     * 供应商id(编码)
     */
    @TableId(type = IdType.INPUT)
    @ApiModelProperty(value = "供应商id(编码)")
    private String supplierId;

    /**
     * 供应商鉴权信息
     */
    @ApiModelProperty(value = "供应商鉴权信息")
    private String supplierAuthInfo;

    /**
     * 绑定接口
     */
    @ApiModelProperty(value = "绑定接口")
    private String bindingUrl;

    /**
     * 解绑接口
     */
    @ApiModelProperty(value = "解绑接口")
    private String unbindUrl;

    /**
     * 修改有效期接口
     */
    @ApiModelProperty(value = "修改有效期接口")
    private String updateExpirationUrl;

}

