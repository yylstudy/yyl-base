package com.cqt.model.hmbc.vo;

import com.cqt.model.hmbc.entity.PrivateDialTestTaskRecordDetails;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 定时位置更新结果推送企业 参数
 *
 * @author scott
 * @date 2022年08月02日 10:08
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HmbcResult implements Serializable {

    private static final long serialVersionUID = -6887909074278348179L;

    /**
     * 本次隐私号拨测的号码
     */
    @ApiModelProperty(value = "本次隐私号拨测的号码")
    private String number;

    /**
     * 拨测状态（0:失败, 1:成功）
     */
    @ApiModelProperty(value = "拨测状态（0:失败, 1:成功）")
    private Integer state;

    /**
     * 拨测失败原因
     */
    @ApiModelProperty(value = "拨测失败原因")
    private String reason;
    

    public HmbcResult(PrivateDialTestTaskRecordDetails details) {
        this.number = details.getNumber();
        this.state = details.getState();
        this.reason = details.getFailCause();
    }
}
