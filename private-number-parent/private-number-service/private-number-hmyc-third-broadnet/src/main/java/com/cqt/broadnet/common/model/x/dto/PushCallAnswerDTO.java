package com.cqt.broadnet.common.model.x.dto;

import com.cqt.common.enums.CallEventEnum;
import com.cqt.model.push.entity.PrivateStatusInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author linshiqiang
 * date:  2023-04-11 13:53
 * 摘机被叫应答事件参数
 * request_body={"call_id":"65463548100047099521524192004636","secret_no":"15100000000","ring_time":"2018-04-20 10:40:05",
 * "start_time":""2018-04-20 10:40:30","call_out_time":"2018-04-20 10:40:02","subs_id":"qwert",
 * "request_id":"65463548100047099521524192004636","caller_num":"18200000001","callee_num":"15300000001",
 * "caller_show_num":"15200000001","callee_show_num":"15100000000"}
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PushCallAnswerDTO extends BaseAuthDTO {

    /**
     * request对象，使用JSON格式拼接。
     */
    @JsonProperty("request_body")
    private String requestBodyStr;

    private RequestBody requestBody;

    @Data
    public static class RequestBody {

        /**
         * 唯一呼叫ID，需要和转呼控制接口的call_id对应起来
         * 取值样例：ABC-EDF
         * 短信流程记录为sms_id。
         */
        @JsonProperty("call_id")
        private String callId;

        /**
         * 响铃时间。若呼叫振铃，则取值向后对齐，同start_time（通话开始时间）
         * 取值样例：2018-01-01 12:00:00
         */
        @JsonProperty("ring_time")
        private String ringTime;

        /**
         * 通话开始时间，即被叫应答时间。若呼叫未接通，则取值向后对齐，同release_time（通话结束时间）
         * 取值样例：2018-01-01 12:00:05
         */
        @JsonProperty("start_time")
        private String startTime;

        /**
         * 中间号，即虚拟号码X
         * 国内号码格式，取值样例：13519000000，
         * 075556621234
         */
        @JsonProperty("secret_no")
        private String secretNo;

        /**
         * B路呼出时间。若未路由呼叫，则取值向后对齐，同ring_time（响铃时间）。
         * 取值样例：2018-01-01 12:00:00
         */
        @JsonProperty("call_out_time")
        private String callOutTime;

        /**
         * 分机号
         */
        @JsonProperty("subs_id")
        private String extensionNo;

        /**
         * 唯一，填写CallID
         */
        @JsonProperty("request_id")
        private String requestId;

        /**
         * 主叫真实号
         */
        @JsonProperty("caller_num")
        private String callerNum;

        /**
         * 被叫真实号码
         */
        @JsonProperty("callee_num")
        private String calleeNum;

        @JsonProperty("callee_show_num")
        private String calleeShowNum;


        @JsonProperty("caller_show_num")
        private String callerShowNum;

    }

    public RequestBody convertJson(ObjectMapper objectMapper) throws JsonProcessingException {
        this.requestBody = objectMapper.readValue(this.requestBodyStr, RequestBody.class);
        return this.requestBody;
    }

    public PrivateStatusInfo buildStatusInfo(PushCallAnswerDTO.RequestBody requestBody) {
        PrivateStatusInfo statusInfo = new PrivateStatusInfo();
        statusInfo.setEvent(CallEventEnum.answer.name());
        statusInfo.setRecordId(requestBody.getCallId());
        // TODO 未提供
        statusInfo.setBindId("");
        statusInfo.setCaller(requestBody.getCallerNum());
        statusInfo.setCalled(requestBody.getCalleeNum());
        statusInfo.setTelX(requestBody.getSecretNo());
        statusInfo.setCurrentTime(requestBody.getStartTime());
        // TODO 未提供
        statusInfo.setCallResult(1);
        statusInfo.setExt(requestBody.getExtensionNo());
        return statusInfo;
    }
}
