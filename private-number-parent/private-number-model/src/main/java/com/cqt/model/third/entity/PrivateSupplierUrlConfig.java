package com.cqt.model.third.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author dingsh
 * @since 2022-07-25
 */
@Getter
@Setter
@TableName("private_supplier_url_config")
@ApiModel(value = "PrivateSupplierUrlConfig对象", description = "")
public class PrivateSupplierUrlConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId("id")
    private String id;

    @ApiModelProperty("供应商id")
    @TableField("supplier_id")
    private String supplierId;

    @ApiModelProperty("url 地址")
    @TableField("url")
    private String url;

    @ApiModelProperty("url 类型 1：綁定 2： 更新绑定有效期 3：解除绑定 4..")
    @TableField("url_type")
    private Integer urlType;

    @ApiModelProperty("url 名称")
    @TableField("url_name")
    private String urlName;

    @ApiModelProperty("备注")
    @TableField("note")
    private String note;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @ApiModelProperty("创建用户")
    @TableField("create_user")
    private String createUser;

    @ApiModelProperty("更新用户")
    @TableField("update_user")
    private String updateUser;


}
