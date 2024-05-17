package com.cqt.model.numpool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


import java.io.Serializable;
import java.util.Date;

/**
 * 隐私号号码信息表(PrivateNumberInfo)实体类
 *
 * @author makejava
 * @since 2022-05-13 15:43:52
 */
@Data
public class PrivateNumberInfo implements Serializable {

    private static final long serialVersionUID = -77517885156418353L;

    /**
     * X号码
     */

    @ApiModelProperty(value = "小号")
    @TableId(type = IdType.INPUT)
    private String number;

    /**
     * 企业id
     */
    @ApiModelProperty(value = "企业vccId")
    private String vccId;

    /**
     * 业务类型, AXB, AXE
     */
    @ApiModelProperty(value = "业务类型")
    private String businessType;

    /**
     * 号码池类型，见字典表定义
     */
    @ApiModelProperty(value = "号码池类型")
    private String poolType;

    /**
     * 号码类型，1 移动小号、2 固话号码、3 95号码、4 400固话号码
     */
    @ApiModelProperty(value = "号码类型")
    private Integer numType;

    /**
     * 归属运营商，（2：电信，3：联通，4：移动，5：广电）
     */
    @ApiModelProperty(value = "归属运营商")
    private Integer isp;

    /**
     * 归属供应商
     */
    @ApiModelProperty(value = "归属供应商")
    private String supplierId;

    /**
     * 手机号码对应的IMSI
     */
    @ApiModelProperty(value = "移动小号对应的IMSI")
    private String imsi;

    /**
     * GT码
     */
    @ApiModelProperty(value = "移动小号对应的GT码")
    private String gtCode;

    /**
     * 地区编码 010
     */
    @ApiModelProperty(value = "号码所属地市编码")
    private String areaCode;

    /**
     * 地区完整名称 例（福建福州）
     */
    @ApiModelProperty(value = "号码所属地市名称")
    private String areaName;

    /**
     * 主备池类型, 默认 MASTER
     */
    @ApiModelProperty(value = "主备池类型 MASTER SALVE")
    private String place;

    /**
     * 位置更新状态（0：待更新；1：已更新；2：更新中）
     */
    @ApiModelProperty(value = "位置更新状态")
    private Integer locationUpdateStatus;

    /**
     * 位置更新失败原因
     */
    @ApiModelProperty(value = "位置更新失败原因")
    private String failCause;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 号码状态，字典项配置，0正常、1下线
     */
    @ApiModelProperty(value = "号码状态 0正常、1下线")
    private Integer state;

    /**
     * 号码分配状态，字典项配置，0待分配，1已分配
     */
    @ApiModelProperty(value = "号码分配状态")
    private Integer allocationFlag;

    /**
     * 是否支持短信（0：否，1：是）
     */
    @ApiModelProperty(value = "是否支持短信")
    private Integer supportSms;

    /**
     * 号码新增、上线时是否自动进行位置更新（0：否，1：是）
     */
    @ApiModelProperty(value = "是否自动位置更新")
    private Integer autoLocationUpdate;

    /**
     * 号码绑定的Enabler服务名
     */
    @ApiModelProperty(value = "华为位置更新服务专用字段")
    private String enablerName;

    /**
     * 短信中心匹配地市
     */
    @ApiModelProperty(value = "短信中心匹配地市")
    private String smsCenterCity;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;

    // 逻辑字段
    /**
     * 企业名称
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "企业名称")
    private String vccName;

    /**
     * GT码对应的环境名称
     */
    @TableField(exist = false)
    @ApiModelProperty(value = "GT码对应的环境名称")
    private String gtName;



    public PrivateNumberInfo() {

    }

    public PrivateNumberInfo(String number) {
        this.number = number;
    }

    public PrivateNumberInfo(String number, String vccId, String businessType, String poolType, String place, Integer allocationFlag) {
        this.number = number;
        this.vccId = vccId;
        this.businessType = businessType;
        this.poolType = poolType;
        this.place = place;
        this.allocationFlag = allocationFlag;
    }

    public PrivateNumberInfo(String number, String vccId, String businessType, String poolType, String place, String areaCode, Integer allocationFlag) {
        this.number = number;
        this.vccId = vccId;
        this.businessType = businessType;
        this.poolType = poolType;
        this.place = place;
        this.areaCode = areaCode;
        this.allocationFlag = allocationFlag;
    }
}

