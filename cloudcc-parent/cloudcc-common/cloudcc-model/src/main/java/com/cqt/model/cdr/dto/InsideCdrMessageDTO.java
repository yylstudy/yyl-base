package com.cqt.model.cdr.dto;

import com.cqt.model.cdr.entity.CallCenterMainCdr;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import com.cqt.model.cdr.entity.CdrChanneldata;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-07-26 10:39
 * 话单入库发送mq消息体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class InsideCdrMessageDTO implements Serializable {

    private static final long serialVersionUID = 8833530864417071776L;

    /**
     * 是否重发
     */
    private boolean whetherResend;

    /**
     * 是否为留言
     */
    private Boolean voiceMailFlag;

    /**
     * 呼入转IVR, 没有分配坐席
     */
    private Boolean callInIvrNoAgent;

    /**
     * fs服务器标识
     */
    private String serviceId;

    /**
     * 主话单实体
     */
    private CallCenterMainCdr mainCdr;

    /**
     * 子话单实体
     */
    private List<CallCenterSubCdr> subCdr;


    /**
     * 通道变量
     */
    private CdrChanneldata channelData;

    public boolean isWhetherResend() {
        return whetherResend;
    }
}
