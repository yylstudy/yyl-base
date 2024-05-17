package com.cqt.monitor.web.callevent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 地市管理
 * @Author: jeecg-boot
 * @Date:   2022-05-23
 * @Version: V1.0
 */
@Data
@TableName("t_area")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="area_table对象", description="地市管理")
public class AreaTable implements Serializable {
    private static final long serialVersionUID = 1L;

	/**城市区号*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "城市区号")
    private String telCode;
	/**省份名称*/

    @ApiModelProperty(value = "省份名称")
    private String province;
	/**城市名称*/

    @ApiModelProperty(value = "城市名称")
    private String city;
	/**南方北方*/

    @ApiModelProperty(value = "南方北方")
    private String type;
	/**详情*/

    @ApiModelProperty(value = "详情")
    private String details;
    /**创建人*/
    @ApiModelProperty(value = "创建人")
    private String createBy;
    /**创建日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建日期")
    private Date createTime;
    /**更新人*/
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    /**更新日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private Date updateTime;
}
