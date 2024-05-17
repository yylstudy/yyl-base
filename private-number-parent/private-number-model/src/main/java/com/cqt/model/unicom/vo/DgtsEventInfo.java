package com.cqt.model.unicom.vo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhengsuhao
 * @date 2022/12/5
 */
@Api(tags = "联通集团总部(江苏)按键收号对象")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DgtsEventInfo {
    /**
     * 控制指示
     */
    @ApiModelProperty(value = "控制指示")
    private Integer control;
    /**
     * 最小收集数字个数
     */
    @ApiModelProperty(value = "最小收集数字个数")
    private Integer minCollect;
    /**
     * 最大收集数字个数
     */
    @ApiModelProperty(value = "最大收集数字个数")
    private Integer maxCollect;
    /**
     * 等待收集数字完成的总时长
     */
    @ApiModelProperty(value = "等待收集数字完成的总时长")
    private Integer maxInteractTime;
    /**
     * 等待首位数字超时时间
     */
    @ApiModelProperty(value = "等待首位数字超时时间")
    private Integer initInterDgtTime;
    /**
     * 两个数字输入之间的间隔时间
     */
    @ApiModelProperty(value = "两个数字输入之间的间隔时间")
    private Integer normInterDgtTime;
    /**
     * 应答结束数字
     */
    @ApiModelProperty(value = "应答结束数字")
    private Integer enterDgtMask;
    /**
     * 收号方式
     */
    @ApiModelProperty(value = "收号方式")
    private Integer digitCollectionType;
}
