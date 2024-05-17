package com.cqt.broadnet.common.model.axb.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author huweizhong
 * date  2023/8/3 10:14
 */
@Data
public class SmsBack {

    /**
     * 话音开放平台给App分配的AppKey。
     */
    private String appKey;
    /**
     * 短信标识，由话音开放平台生成，唯一标识一个呼叫
     */
    @TableId(type = IdType.INPUT)
    private String msgIdentifier ;
    /**
     * 主叫号码
     */
    private String calling;
    /**
     * 被叫号码，X模式时不携带
     */
    private String called;
    /**
     * 短信上报的被叫号码
     */
    private String virtualCalled;
    /**
     * 短信下发显示的主叫号码，X模式时不携带
     */
    private String displayCalling;
    /**
     * 发送结果
     * Success 成功
     * Failed 失败
     */

    private String bindId;

    @JsonProperty("TotalCount")
    private String totalCount;

    @JsonProperty("SuccessCount")
    private String successCount;

    private String result;

    private String timeStamp;

    private String content;
}
