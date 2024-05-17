package com.cqt.model.numpool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author linshiqiang
 * @date 2022/6/20 16:46
 * 企业号码池表(PrivateCorpNumberPool)实体类
 */
@Data
public class PrivateCorpNumberPool implements Serializable {
    private static final long serialVersionUID = 328956379921732196L;
    /**
     * 分布式主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "分布式主键ID")
    private String id;

    /**
     * 企业id
     */
    @ApiModelProperty(value = "企业id")
    private String vccId;

    /**
     * 地市编码
     */
    @ApiModelProperty(value = "地市编码")
    private String areaCode;

    /**
     * 号码池类型，见字典表定义
     */
    @ApiModelProperty(value = "号码池类型，见字典表定义")
    private String poolType;

    /**
     * 业务类型, AXB, AXE
     */
    @ApiModelProperty(value = "业务类型, AXB, AXE")
    private String businessType;


    /**
     * 号码类型，1 移动小号、2 固话号码、3 95号码、4 400固话号码
     */
    @ApiModelProperty(value = "号码类型，1 移动小号、2 固话号码、3 95号码、4 400固话号码")
    private Integer numType;

    /**
     * 归属运营商，（2：电信，3：联通，4：移动，5：广电）
     */
    @ApiModelProperty(value = "归属运营商，（2：电信，3：联通，4：移动，5：广电）")
    private Integer isp;

    /**
     * 主池数量
     */
    @ApiModelProperty(value = "主池数量")
    private Integer masterNum;

    /**
     * 主号码池已分配数量
     */
    @ApiModelProperty(value = "主号码池已分配数量")
    private Integer masterAllotNum;

    /**
     * 备池数量
     */
    @ApiModelProperty(value = "备池数量")
    private Integer slaveNum;

    /**
     * 备号码池已分配数量
     */
    @ApiModelProperty(value = "备号码池已分配数量")
    private Integer slaveAllotNum;

    /**
     * 用户
     */
    @ApiModelProperty(value = "用户")
    private String createBy;

    /**
     * 添加时间
     */
    @ApiModelProperty(value = "添加时间")
    private Date createTime;

    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人")
    private String updateBy;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
}
