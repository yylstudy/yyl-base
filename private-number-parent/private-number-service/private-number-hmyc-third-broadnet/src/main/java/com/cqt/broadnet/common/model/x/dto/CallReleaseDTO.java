package com.cqt.broadnet.common.model.x.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-02-16 13:43
 * 推送通话结束事件
 * &end_call_request={"release_cause":31,"call_id":"65463548100047099521524192004636","ring_time":"2018-04-20 10:40:05",
 * "start_time":"2018-04-20 10:40:09","secret_no":"15100000000","call_out_time":"2018-04-20 10:40:05",
 * "release_dir":2,"release_time":"2018-04-20 10:40:11","subs_id":"qwert"}
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CallReleaseDTO extends BaseAuthDTO {

    @JsonProperty("end_call_request")
    private String endCallRequestStr;

    private EndCallRequest endCallRequest;

    @Data
    public static class EndCallRequest {
        private static final long serialVersionUID = 1351398541278396982L;

        /**
         * 呼叫释放原因，Q.850。
         * 取值样例（包括但不限于如下取值）
         * 1：未分配的号码（空号）
         * 3：无至目的地的路由
         * 4：停机
         * 6：不可接受的信道
         * 16：正常清除
         * 17：用户忙
         * 18：无用户响应
         * 19：已有用户提醒，但无应答
         * 21：呼叫拒绝
         * 22：号码改变
         * 26：清除未选择的用户
         * 27：终点故障
         * 28：无效号码格式（不完全的号码）
         * 29：设施被拒绝
         * 30：对状态询问的响应
         * 31：正常--未规定
         * 34：无电路/信道可用
         * 38：网络故障
         * 41：临时故障
         * 42：交换设备拥塞
         * 43：接入信息被丢弃
         * 44：请求的电路/信道不可用
         * 47：资源不可用--未规定
         * 49：服务质量不可用
         * 50：未预订所请求的设施
         * 55：IncomingcallsbarredwithintheCUG
         * 57：承载能力未认可(未开通通话功能）
         * 58：承载能力目前不可用
         * 63：无适用的业务或任选项目-未规定
         * 65：承载业务不能实现
         * 68：ACMequaltoorgreaterthanACMmax
         * 69：所请求的设施不能实现
         * 70：仅能获得受限数字信息承载能力
         * 79：业务不能实现-未规定)
         * 81：无效处理识别码
         * 87：UsernotmemberofCUG
         * 88：非兼容目的地址
         * 91：无效过渡网选择
         * 95：无效消息-未规定
         * 96：必选消息单元差错
         * 97：消息类型不存在或不能实现
         * 98：消息与控制状态不兼容-消息类型不存在或不能实现
         * 99：信息单元不存在或不能实现
         * 100：无效信息单元内容
         * 101：消息与呼叫状态不兼容
         * 102：定时器超时恢复
         * 111：协议差错-未规定
         * 127：互通-未规定
         * 短信流程无释放原因值
         * [缺省值]
         * 16-正常清除。
         */
        @JsonProperty("release_cause")
        private Integer releaseCause;

        /**
         * 唯一呼叫ID，需要和转呼控制接口的call_id对应起来
         * 取值样例：ABC-EDF
         * 短信流程记录为 sms_id。
         */
        @JsonProperty("call_id")
        private String callId;

        /**
         * 短信流程记录id
         */
        @JsonProperty("sms_id")
        private String smsId;

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
         * 中间号，即虚拟号码。
         * 国内号码格式，取值样例：075556621234
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
         * 0：平台释放
         * 1：主叫释放
         * 2：被叫释放
         * 短信流程无此值，固定取值为0-平台释放
         */
        @JsonProperty("release_dir")
        private Integer releaseDir;

        /**
         * 通话结束时间。
         * 取值样例：2018-01-01 12:00:00
         */
        @JsonProperty("release_time")
        private String releaseTime;

        /**
         * 唯一绑定关ID(废弃)
         * 分机号
         */
        @JsonProperty("subs_id")
        private String extensionNo;

        /**
         * 供应商Key
         */
        @JsonProperty("vendor_key")
        private String vendorKey;

        /**
         * 主叫响铃时间。若呼叫未振铃，则取值向后对齐，同start_time_a（通话开始时间）
         * 仅当OTT发起双呼场景下，携带此参数，此时间为开始振铃
         * 取值样例：2018-01-01 12:00:00
         * 中台扩展参数。
         * 短信流程不携带
         */
        @JsonProperty("ring_time_a")
        private String ringTimeA;

        /**
         * 主叫通话开始时间。若呼叫未接通，则取值向后对齐，同release_time（通话结束时间）
         * 仅当OTT发起双呼场景下，携带此参数，此时间为应答时间
         * 取值样例：2018-01-01 12:00:05
         * 中台扩展参数。
         * 短信流程不携带。
         */
        @JsonProperty("start_time_a")
        private String startTimeA;

        /**
         * 空闲振铃率：A呼叫X给B时，B端手机处于空闲状态下，响铃成功。故称之为空闲振铃率，如果因为组网方式的原因, 此字段可以选择暂时不支持。
         * 中台设置此字段为ringTime。
         */
        @JsonProperty("free_ring_time")
        private String freeRingTime;

        /**
         * 短信条数，仅短信场景有效。
         */
        @JsonProperty("sms_number")
        private Integer smsNumber;

        /**
         * 录音下载URL，公网可以访问，仅语音场景有效，在呼叫结束时间中同步推送录音结果URL，支持HTTP方式访问。
         * 短信流程不携带。
         */
        @JsonProperty("record_url")
        private String recordUrl;

        /**
         * 必须	主叫真实号码
         */
        @JsonProperty("caller_num")
        private String callerNum;

        /**
         * 必须	被叫真实号码
         */
        @JsonProperty("callee_num")
        private String calleeNum;

        /**
         * dtmf按键 TODO 未提供
         */
        @JsonProperty("end_call_ivr_dtmf")
        private String endCallIvrDtmf;

        /**
         * 短信内容，请使用UCS2进行编码
         * 取值样例：30104E2D
         * TODO 未提供
         */
        @JsonProperty("sms_content")
        private String smsContent;

        /**
         * 可选	接收到RouteErr时的状态码
         */
        @JsonProperty("resp_code")
        private String respCode;

        /**
         * 可选	释放原因
         */
        @JsonProperty("releaseReason")
        private String releaseReason;

        /**
         * 可选	呼叫中或者呼叫挂断后收号结果，如果有多个用；连接多次收号结果
         */
        @JsonProperty("CollectResult")
        private String collectResult;

        /**
         * 路由结果上报
         * 当同顺振呼叫所有目的号码路由失败时上报。
         */
        private List<RouteResult> routeResult;

        /**
         * 路由结果上报
         * 当同顺振呼叫所有目的号码路由失败时上报。
         */
        @Data
        public static class RouteResult {

            /**
             * 必须	目的号码
             */
            @JsonProperty("Number")
            private String number;

            /**
             * 必须	接收到RouteErr时的状态码
             */
            @JsonProperty("RespCode")
            private String respCode;

            /**
             * 可选	导致呼叫结束；核心网上报的Q.850的原因值
             */
            @JsonProperty("Q.850")
            private String q850;
        }

    }

    public EndCallRequest convertJson(ObjectMapper objectMapper) throws JsonProcessingException {
        this.endCallRequest = objectMapper.readValue(this.endCallRequestStr, EndCallRequest.class);
        return this.endCallRequest;
    }
}
