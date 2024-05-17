package com.cqt.model.freeswitch.dto.event;

import com.cqt.model.freeswitch.base.FreeswitchEventBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:18
 * 分机注册状态变化事件通知
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExtensionStatusEventDTO extends FreeswitchEventBase implements Serializable {

    private static final long serialVersionUID = 6485493042884676528L;

    private EventData data;

    @Data
    public static class EventData implements Serializable {

        private static final long serialVersionUID = 8808219597885610325L;

        /**
         * 分机号
         */
        @JsonProperty("ext_id")
        private String extId;

        /**
         * 当前分机注册地址
         */
        @JsonProperty("reg_addr")
        private String regAddr;

        /**
         * 当前剩余有效期(s)
         */
        @JsonProperty("reg_expsecs")
        private Long regExpire;

        /**
         * 最近注册时间
         */
        @JsonProperty("reg_time")
        private String regTime;

        /**
         * 分机注册状态【UNREG:注销，REG:注册】
         */
        @JsonProperty("status")
        private String status;
    }
}
