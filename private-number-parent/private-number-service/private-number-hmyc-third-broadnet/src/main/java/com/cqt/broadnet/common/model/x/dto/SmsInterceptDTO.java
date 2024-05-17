package com.cqt.broadnet.common.model.x.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * @author linshiqiang
 * date:  2023-02-16 13:56
 * 短信托收推送接口入参
 * &sms_intercept_request={"secret_no":"15100000000","call_no":"18200000001","sms_content":"0061",
 * "mt_time":"2018-04-20 10:35:41","call_id":"8618200000001_1524191740904_6546353676133343552",
 * "subs_id":"12345","vendor_key":"CMCC"}
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SmsInterceptDTO extends BaseAuthDTO {

    /**
     * 短信托收结构体
     */
    @JsonProperty("sms_intercept_request")
    private String smsInterceptRequestStr;

    private SmsInterceptRequest smsInterceptRequest;

    @Data
    public static class SmsInterceptRequest {

        private static final long serialVersionUID = 1351396541278396982L;

        /**
         * 中间号。
         * 国内号码格式，取值样例：，
         * 075556621234
         */
        @JsonProperty("secret_no")
        private String secretNo;

        /**
         * 短信发送主叫号码
         * 国内号码格式，取值样例：106xxxxxxx，
         * 075556621234
         */
        @JsonProperty("call_no")
        private String callNo;

        /**
         * 短信内容，请使用UCS2进行编码
         * 取值样例：30104E2D
         */
        @JsonProperty("sms_content")
        private String smsContent;

        /**
         * 短信时间戳
         * 取值样例：2018-01-01 12:00:00
         */
        @JsonProperty("mt_time")
        private Date mtTime;

        /**
         * 每次呼叫行为和短信行为的唯一ID
         * 取值样例：ABC-EFG-DFN
         */
        @JsonProperty("call_id")
        private String callId;

        /**
         * 唯一绑定关的ID
         * 取值样例：1234
         */
        @JsonProperty("subs_id")
        private String subsId;

        /**
         * 分配给供应商的KEY
         * 取值样例：CMCC
         */
        @JsonProperty("vendor_key")
        private String vendorKey;

    }

    public SmsInterceptRequest convertJson(ObjectMapper objectMapper) throws JsonProcessingException {
        this.smsInterceptRequest = objectMapper.readValue(this.smsInterceptRequestStr, SmsInterceptRequest.class);
        return this.smsInterceptRequest;
    }
}
