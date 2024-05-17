package com.cqt.model.unicom.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @author zhengsuhao
 * @date 2022/12/07
 */
@Api(tags = "企业实体类")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivateCorpInteriorInfo implements Serializable {

    private static final long serialVersionUID = -8056276556567752106L;

    @TableId(type = IdType.INPUT)
    private String id;

    @ApiModelProperty(value = "企业标识")
    @NotBlank
    private String vccId;

    @ApiModelProperty(value = "业务标识")
    private String serviceKey;

    @ApiModelProperty(value = "位置更新是否自动抢回")
    private Integer locationUpdateAuto;

    @ApiModelProperty(value = "位置更新自动抢回次数")
    private Integer locationUpdateAutoNum;

    @ApiModelProperty(value = "是否使用空号检测")
    @TableField("fail_stat_easr")
    private Integer failStatEasr;

    @ApiModelProperty(value = "是否全量录音，0:否,1是，默认全量录音")
    @TableField("all_isrecord")
    @JSONField(name = "isRecord")
    private Integer isRecord;

    @ApiModelProperty(value = "lua脚本名称")
    private String luaName;

    @ApiModelProperty(value = "空号检测是否自动挂断")
    private Integer autoHangup;

    @ApiModelProperty(value = "全量录音是否推送联通")
    private Integer allIsrecordPush;

    @ApiModelProperty(value = "呼入话单是否推送联通")
    private Integer callinCdrPushUnicom;

    @ApiModelProperty(value = "呼出话单是否推送联通")
    private Integer calloutCdrPushUnicom;

    @ApiModelProperty(value = "呼出录音话单是否推送联通")
    private Integer calloutRecoredCdrPushUnicom;

    @ApiModelProperty(value = "主叫号码是否允许固话")
    private Integer callinFixNum;

    private String distribution;

    private String profile;

    @ApiModelProperty(value = "主叫号码是否允许为400号")
    @TableField("callin_400_num")
    private Integer callin400Num;

    @ApiModelProperty(value = "主叫号码是否允许为95号")
    @TableField("callin_95_num")
    private Integer callin95Num;

    @ApiModelProperty(value = "内部查询绑定关系url")
    private String queryBindInfoUrl;

    @ApiModelProperty(value = "短信话单推送地址（内部）")
    private String smsCdrUrl;

    @ApiModelProperty(value = "语音话单推送地址（内部）")
    private String voiceCdrUrl;

    @ApiModelProperty(value = "状态")
    private Integer state;

    @ApiModelProperty(value = "创建人")
    private String createBy;

    @ApiModelProperty(value = "创建日期")
    private Date createTime;

    @ApiModelProperty(value = "更新人")
    private String updateBy;

    @ApiModelProperty(value = "profile文件id")
    private String profileId;

    @ApiModelProperty(value = "dis组id")
    private String distributionId;

    @ApiModelProperty(value = "profile文件名称")
    private String profileName;

    @ApiModelProperty(value = "dis组文件名称")
    private String distributionName;

    @ApiModelProperty(value = "dis组文件对应list")
    private String listName;

    @ApiModelProperty(value = "备注")
    private String remark;

}

