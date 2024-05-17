package com.cqt.broadnet.common.model.axb.dto;

import cn.hutool.core.date.DateUtil;
import com.cqt.broadnet.common.utils.FormatUtil;
import com.cqt.common.enums.CallEventEnum;
import com.cqt.model.push.entity.PrivateStatusInfo;
import lombok.Data;

/**
 * @author linshiqiang
 * date:  2023-05-26 11:50
 * 1.3.2.5 呼叫发起推送接口 入参
 */
@Data
public class AxbCallStartDTO {

    /**
     * 绑定Id
     * 是
     * 绑定接口返回的bindId
     */
    private String bindId;

    /**
     * 呼叫Id
     * 是
     * 呼叫Id，唯一标示一个呼叫
     */
    private String callId;

    /**
     * 城市ID
     * 是
     * 固定为-1
     */
    private Long cityId;

    /**
     * 呼叫发起方号码
     * 是
     * 呼叫发起方号码，用户号码格式遵循国际电信联盟定义的E.164标准国际号码格式，比如8613812345678
     */
    private String callNo;

    /**
     * 呼叫发起时间
     * 是
     * 时间戳，秒为单位
     */
    private Long callTime;

    /**
     * 最终呼叫接收方号码
     * 是
     * 最终呼叫接收方号码，用户号码格式遵循国际电信联盟定义的E.164标准国际号码格式，比如8613812345678
     */
    private String peerNo;

    /**
     * 上一个X号码
     * 否
     * 使用场景：维护接口中检查X号码状态不可用时，重新分配新的X号码，同时记录不可用的X号码，在推送消息时作为preX上报
     */
    private String preX;

    /**
     * 中间号码X
     * 是
     * 中间号码X，用户号码格式遵循国际电信联盟定义的E.164标准国际号码格式，比如8613812345678
     */
    private String x;



}
