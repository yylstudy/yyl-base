package com.cqt.push.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: private_cdr_repush
 * @Author: jeecg-boot
 * @Date:   2022-07-11
 * @Version: V1.0
 */
@Data
@TableName("private_cdr_repush")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value="private_cdr_repush对象", description="private_cdr_repush")
public class PrivateCdrRepush implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
	/**重推失败时间*/

    @ApiModelProperty(value = "重推失败时间")
    private Date repushFailTime;
	/**企业VCCID*/
    @ApiModelProperty(value = "企业VCCID")
    private String vccId;
	/**企业名称*/
    @ApiModelProperty(value = "企业名称")
    private String vccName;
	/**话单推送url*/
    @ApiModelProperty(value = "话单推送url")
    private String cdrPushUrl;
	/**失败原因*/
    @ApiModelProperty(value = "失败原因")
    private String failReason;
	/**json报文*/
    @ApiModelProperty(value = "json报文")
    private String jsonStr;
	/**createBy*/
    @ApiModelProperty(value = "createBy")
    private String createBy;
	/**updateBy*/
    @ApiModelProperty(value = "updateBy")
    private String updateBy;
	/**createTime*/

    @ApiModelProperty(value = "createTime")
    private Date createTime;
	/**updateTime*/

    @ApiModelProperty(value = "updateTime")
    private Date updateTime;
}
