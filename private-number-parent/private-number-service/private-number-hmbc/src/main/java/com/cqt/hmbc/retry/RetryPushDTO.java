package com.cqt.hmbc.retry;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 重推参数定义
 *
 * @author Xienx
 * @date 2023年02月24日 9:50
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RetryPushDTO extends BaseRetryInfo {

    /**
     * 企业vccId
     */
    private String vccId;

    /**
     * 企业名称
     */
    private String vccName;

    /**
     * 拨测的号码
     */
    private String number;

    /**
     * 拨测任务类型
     */
    private Integer jobType;

    /**
     * 企业推送地址
     */
    private String url;

    /**
     * 推送参数
     */
    private String body;

    /**
     * 上一次的失败原因
     */
    private String failCause;

    @Override
    public String getBizId() {
        return String.format("企业 %s(%s) 推送 %s 拨测结果", vccId, vccName, number);
    }
}
