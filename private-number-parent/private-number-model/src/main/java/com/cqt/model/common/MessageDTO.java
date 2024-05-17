package com.cqt.model.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2022/5/12 10:27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO implements Serializable {

    private static final long serialVersionUID = 8111332504179599256L;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空!")
    @ApiModelProperty("消息内容")
    private String content;

    /**
     * 消息类型: 短信 sms, 邮箱 email, 钉钉 ding
     */
    @NotBlank(message = "消息类型不能为空!")
    @ApiModelProperty("消息类型: 短信 sms, 邮箱 email, 钉钉 dingding")
    private String type;

    /**
     * 接收人, 多个以英文逗号隔开
     */
    @ApiModelProperty("接收人, 多个以英文逗号隔开(短信, 邮箱必填; 钉钉可不填)")
    private String to;

    /**
     * 操作类型
     */
    @NotBlank(message = "操作类型不能为空!")
    @ApiModelProperty("操作类型: 当前操作描述")
    private String operateType;

    @ApiModelProperty("钉钉必填, 群组代码: 默认private")
    private String group;

    @ApiModelProperty("钉钉必填, 消息类型: 默认text, markdown..")
    private String msgType;

    @ApiModelProperty("钉钉选填, 消息类型为markdown时必填")
    private String title;
}
