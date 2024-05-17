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
@Api(tags = "联通集团总部(江苏)TransMedia格式")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransMedia {

    /**
     * 媒体属性
     */
    @ApiModelProperty(value = "媒体属性")
    private Integer mediaAttribute;
    /**
     * 对端RTP连接的IP地址
     */
    @ApiModelProperty(value = "对端RTP连接的IP地址")
    private String connectionAddress;
    /**
     * 对端媒体流使用的端口号
     */
    @ApiModelProperty(value = "对端媒体流使用的端口号")
    private Integer mediaPort;
}
