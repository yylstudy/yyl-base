package com.cqt.monitor.web.distributor.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author dingsh
 * @since 2022-09-02
 */
@Getter
@Setter
@TableName("private_sip_distributor_config_info")
@ApiModel(value = "PrivateSipDistributorConfigInfo对象")
public class PrivateSipDistributorConfigInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("主键")
    @TableId("id")
    private String id;

    @ApiModelProperty("distributorId")
    @TableField("distributor_id")
    private String distributorId;

    @ApiModelProperty("distirbutor list名称")
    @TableField("list_name")
    private String listName;

    @ApiModelProperty("list body")
    @TableField("list_body")
    private String listBody;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private Date createTime;

}
