package com.cqt.rabbitmq.retry.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * date:  2023-02-17 14:28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PushRetryDataDTO {

    /**
     * 消息唯一id redis键
     */
    private String uniqueIdKey;

    /**
     * 消息唯一id
     */
    private String uniqueId;

    /**
     * 企业id
     */
    private String vccId;

    /**
     * 企业名称
     */
    private String vccName;

    /**
     * 推送数据json
     */
    private String pushData;

    /**
     * 推送接口地址
     */
    private String pushUrl;

    /**
     * 接口错误信息
     */
    private String errorMessage;

    /**
     * 重推次数
     */
    private Integer maxRetry;

    /**
     * 重推间隔 ms
     */
    private Long interval;

}
