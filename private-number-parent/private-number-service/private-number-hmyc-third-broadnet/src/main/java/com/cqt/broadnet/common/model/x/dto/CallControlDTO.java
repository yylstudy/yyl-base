package com.cqt.broadnet.common.model.x.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author linshiqiang
 * date:  2023-02-15 14:35
 * 呼叫控制请求参数
 * sign=8CFA61F3AB5BD989B8E9999574465B67&method=alibaba.aliqin.axb.vendor.call.control&app_key=1234&target_app_key=2345
 * &sign_method=hmac&session=3456&v=2.0&partner_id=apidoc
 * &start_call_request={"secret_no":"15100000000","call_no":"18200000001","call_time":"2018-04-20 10:40:05",
 * "call_id":"65463548100047099521524192004636","record_type":"CALL"}
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CallControlDTO extends BaseAuthDTO {

    /**
     * 参数在表单上
     * &start_call_request={"secret_no":"15100000000","call_no":"18200000001","call_time":"2018-04-20 10:40:05",
     * "call_id":"65463548100047099521524192004636","record_type":"CALL"}
     */
    @JsonProperty("start_call_request")
    private String startCallRequestStr;

    private StartCallRequest startCallRequest;

    @Data
    public static class StartCallRequest {

        /**
         * 唯一的呼叫ID。
         * 取值样例：ABCD-EFCA
         */
        @JsonProperty("call_id")
        private String callId;

        /**
         * 主叫号码
         * 国内号码格式，取值样例：13519000000，
         * 075556621234
         */
        @JsonProperty("call_no")
        private String callNo;

        /**
         * 呼叫开始时间。
         * 取值样例：2018-01-01 12:00:00
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @JsonProperty("call_time")
        private Date callTime;

        /**
         * AXN分机号产品中通过IVR放音收取上来的用户输入的分机字符。
         * 若是号码，则为国内号码格式，如、075556621234。
         * 取值样例：123
         */
        @JsonProperty("extension")
        private String extension;

        /**
         * 行为类型
         * 	CALL：呼叫行为
         * 	SMS：短信行为
         * 取值样例：CALL
         */
        @JsonProperty("record_type")
        private String recordType;

        /**
         * 中间号码
         * 国内号码格式，取值样例：17010000000，075556621234
         */
        @JsonProperty("secret_no")
        private String secretNo;

        /**
         * 供应商KEY
         * 取值样例：CMCC
         */
        @JsonProperty("vendor_key")
        private String vendorKey;

    }

    public StartCallRequest convertJson(ObjectMapper objectMapper) throws JsonProcessingException {
        this.startCallRequest = objectMapper.readValue(this.startCallRequestStr, StartCallRequest.class);
        return this.startCallRequest;
    }

}
