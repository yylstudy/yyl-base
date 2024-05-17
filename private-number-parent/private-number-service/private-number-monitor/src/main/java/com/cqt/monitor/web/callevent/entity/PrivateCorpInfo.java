package com.cqt.monitor.web.callevent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Date;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivateCorpInfo {


    @TableId(type = IdType.INPUT)
    @ApiModelProperty(value = "企业编号")
    @NotBlank(message = "vcc_id不能为空")
    private String vccId;


    @ApiModelProperty(value = "企业名称")
    private String vccName;


    @ApiModelProperty(value = "开通产品")
    private String openProduct;


    @ApiModelProperty(value = "行业类型")
    private String tradeType;


    @ApiModelProperty(value = "企业规模")
    private Integer corpScale;


    @ApiModelProperty(value = "统一社会信用代码")
    private String corpCreditCode;


    @ApiModelProperty(value = "用户数量")
    private Integer userCount;


    @ApiModelProperty(value = "组织机构代码证")
    private String businessLicense;

    @ApiModelProperty(value = "营业执照名称")
    private String licenseName;

    @ApiModelProperty(value = "营业执照图片访问地址")
    private String licenseUrl;

    @ApiModelProperty(value = "主营业务描述")
    private String mainBusiness;

    @ApiModelProperty(value = "企业地址")
    private String corpAddress;

    @ApiModelProperty(value = "联系人")
    private String contactsName;

    @ApiModelProperty(value = "联系电话")
    private String contactsTel;

    @ApiModelProperty(value = "联系邮箱")
    private String contactsEmail;

    @ApiModelProperty(value = "有效期开始时间")
    private Date expireStartTime;

    @ApiModelProperty(value = "有效期结束时间")
    private Date expireEndTime;

    @ApiModelProperty(value = "企业状态")
    private String state;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "更新人")
    private String updateBy;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "备注")
    private String remark;



}
