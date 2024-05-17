package com.cqt.broadnet.common.model.x.vo;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.enums.ControlOperateEnum;
import com.cqt.common.enums.NumberTypeEnum;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.common.ResultVO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author linshiqiang
 * date:  2023-02-15 14:42
 * 呼转控制接口响应参数
 * {
 * "result":{
 * "message":"xxx",
 * "control_resp_dto":{
 * "control_operate":"CONNECT",
 * "control_msg":"controlMsg",
 * "product_type":"AXB",
 * "call_no_play_code":"185",
 * "called_no_play_code":"187",
 * "subs":{
 * "called_display_no":"15200000001",
 * "called_no":"15300000001",
 * "subs_id":"12345",
 * "call_type":"CALLED",
 * "need_record":false
 * }
 * },
 * "code":"OK"
 * }
 * }
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CallControlResponseVO {

    /**
     * 转呼控制接口响应消息体
     */
    @JsonProperty("result")
    private Response result;

    @Data
    public static class Response {

        /**
         * 响应的业务CODE。
         * OK：请求成功
         * 非OK：异常
         * 取值样例：OK
         */
        @JsonProperty("code")
        private String code;

        /**
         * 转呼控制结构体
         */
        @JsonProperty("control_resp_dto")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private ControlRespDto controlRespDto;

        /**
         * 此字段用于业务日志输出，问题排查，无实际业务含义。
         * 广电中台能力平台，不记录不校验。
         */
        @JsonProperty("message")
        private String message;

        @Data
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class ControlRespDto {

            /**
             * 主叫提示音,放音编码和录音文件一一对应
             * 取值样例：185
             * 若OTT不携带此参数或值为空，则广电中台能力平台进行默认放音。
             */
            @JsonProperty("call_no_play_code")
            private String callNoPlayCode;

            /**
             * 被叫提示音
             * 取值样例：187
             */
            @JsonProperty("called_no_play_code")
            private String calledNoPlayCode;

            /**
             * controlMsg此字段用于业务日志输出，问题排查，无实际业务含义。
             * 广电中台能力平台，不记录不校验。
             */
            @JsonProperty("control_msg")
            private String controlMsg;

            /**
             * 控制操作类型：
             * REJECT：（拦截）
             * CONTINUE：（接续）
             * IVR：IVR（收取用户输入内容）
             */
            @JsonProperty("control_operate")
            private String controlOperate;

            /**
             * 是否媒体资源降级，放弃录音放音功能。
             * 广电中台能力平台忽略此参数。
             */
            @JsonProperty("media_degrade")
            private Boolean mediaDegrade;

            /**
             * 产品类型：
             * 取值有AXB、AXN、AXN_EXTENSION_REUSE(AXN分机复用)等。
             * 取值样例：AXB
             * 广电中台能力平台，不校验。
             */
            @JsonProperty("product_type")
            private String productType;

            /**
             * 绑定关系信息
             */
            @JsonProperty("subs")
            private Subs subs;

            @Data
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Subs {

                /**
                 * 针对AX关系中的A号码定义的呼叫类型。
                 * 呼叫流程：
                 * MASTER表示主叫，说明是A打X给B；
                 * CALLED表示被叫，说明是B打X给A。
                 * 短信流程：
                 * SMS_SENDER表示短信发送
                 * SMS_RECEIVER表示短信接收
                 * 取值样例：CALLED
                 */
                @JsonProperty("call_type")
                private String callType;

                /**
                 * 被叫显号，用于路由呼叫\短信时的号码显示。
                 * 国内号码格式，取值样例：13519000000
                 */
                @JsonProperty("called_display_no")
                private String calledDisplayNo;

                /**
                 * 转呼号码，用户路由呼叫\短信时的目的号码
                 * 国内号码格式，取值样例：15200000002
                 */
                @JsonProperty("called_no")
                private String calledNo;

                /**
                 * 是否要录音。
                 * true：需要录音
                 * false：不需要录音
                 * 当为语音呼叫（CALL）时，此参数生效。若响应消息中不携带此参数，则默认为false（不录音）。
                 */
                @JsonProperty("need_record")
                private Boolean needRecord;

                /**
                 * 短信通道方式。
                 * SMS_INTERCEPT：拦截推送（委托OTT进行短信推送）
                 * SMS_NORMAL_SEND：正常现网下发
                 * SMS_DROP：拦截丢弃
                 * 当为短信场景（SMS）时，此参数生效，且必须携带此参数。若未携带，则默认认为SMS_DROP。
                 */
                @JsonProperty("sms_channel")
                private String smsChannel;

                /**
                 * 唯一绑定关的ID（数字格式字符串）
                 * 取值样例：12345
                 */
                @JsonProperty("subs_id")
                private String subsId;

                /**
                 * 路由模式：
                 * 0：顺振
                 * 1：同振
                 * 默认是顺振
                 */
                @JsonProperty("route_mode")
                private String routeMode;

                /**
                 * “wav”
                 * “mp3”
                 */
                @JsonProperty("record_type")
                private String recordType;

                /**
                 * 录音方式
                 * Mix 混音（默认值）
                 * DualChannel双声道
                 */
                @JsonProperty("record_mode")
                private String recordMode;

                /**
                 * 左声道信息，设置需要录制在右声道的号码，需与传递的真实主叫或者真实被叫号码一致
                 * 当type=DualChannel时有效且必须输入
                 */
                @JsonProperty("left_channel_num")
                private String leftChannelNum;

                /**
                 * 右声道信息，设置需要录制在右声道的号码，需与传递的真实主叫或者真实被叫号码一致
                 * 当type=DualChannel时有效且必须输入
                 */
                @JsonProperty("right_channel_num")
                private String rightChannelNum;
            }
        }
    }

    public static CallControlResponseVO fail(String message) {
        CallControlResponseVO callControlResponseVO = new CallControlResponseVO();
        Response response = new Response();
        response.setMessage(message);
        response.setCode("fail");
        callControlResponseVO.setResult(response);
        return callControlResponseVO;
    }

    public static CallControlResponseVO ok() {
        CallControlResponseVO callControlResponseVO = new CallControlResponseVO();
        Response response = new Response();
        response.setMessage("success");
        response.setCode("OK");
        callControlResponseVO.setResult(response);
        return callControlResponseVO;
    }

    public static CallControlResponseVO ok(String message) {
        CallControlResponseVO callControlResponseVO = new CallControlResponseVO();
        Response response = new Response();
        response.setMessage(message);
        response.setCode("OK");
        callControlResponseVO.setResult(response);
        return callControlResponseVO;
    }

    public static CallControlResponseVO buildCallControlResponseVO(String caller, ResultVO<BindInfoApiVO> bindInfoResultVO) {
        BindInfoApiVO bindInfoApiVO = bindInfoResultVO.getData();
        CallControlResponseVO callControlResponseVO = new CallControlResponseVO();
        CallControlResponseVO.Response response = new CallControlResponseVO.Response();
        response.setCode("OK");
        response.setMessage("success");

        Response.ControlRespDto controlRespDto = new Response.ControlRespDto();
        controlRespDto.setControlMsg(bindInfoResultVO.getMessage());
        String controlOperate = bindInfoApiVO.getControlOperate();
        controlRespDto.setControlOperate(controlOperate);
        if (StrUtil.isNotEmpty(bindInfoApiVO.getCallerIvr())) {
            controlRespDto.setCallNoPlayCode(FileNameUtil.mainName(bindInfoApiVO.getCallerIvr()));
        }
        if (StrUtil.isNotEmpty(bindInfoApiVO.getCalledIvr())) {
            controlRespDto.setCalledNoPlayCode(FileNameUtil.mainName(bindInfoApiVO.getCalledIvr()));
        }
        controlRespDto.setMediaDegrade(false);
        controlRespDto.setProductType(NumberTypeEnum.AXB.name());
        if (ControlOperateEnum.CONTINUE.name().equals(controlOperate)) {
            Response.ControlRespDto.Subs subs = new Response.ControlRespDto.Subs();
            subs.setCalledNo(bindInfoApiVO.getCalledNum());
            // TODO 这个字段不知
            subs.setCallType("CALLED");
            // 分机号
            subs.setSubsId("1234");
            if (StrUtil.isNotEmpty(bindInfoApiVO.getExtNum())) {
                subs.setSubsId(bindInfoApiVO.getExtNum());
            }
            subs.setCalledDisplayNo(bindInfoApiVO.getDisplayNum());
            subs.setNeedRecord(true);
//        subs.setSmsChannel(bindInfoApiVO.getType() == 0 ? SmsChannelEnum.SMS_NORMAL_SEND.name() : SmsChannelEnum.SMS_DROP.name());

            // 新增字段
            subs.setRecordType(bindInfoApiVO.getRecordFileFormat());
            if (ObjectUtil.isNotEmpty(bindInfoApiVO.getRecordMode())) {
                subs.setRecordMode("MIX");
                if (bindInfoApiVO.getRecordMode() == 1) {
                    subs.setRecordMode("DualChannel");
                    Integer dualRecordMode = bindInfoApiVO.getDualRecordMode();
                    // 默认 0：主叫录音到左声道，被叫录音到右声道。
                    subs.setLeftChannelNum(caller);
                    subs.setRightChannelNum(bindInfoApiVO.getCalledNum());
                    if (ObjectUtil.isNotEmpty(dualRecordMode) && dualRecordMode == 1) {
                        // 1：被叫录音到左声道，主叫录音到右声道。
                        subs.setLeftChannelNum(bindInfoApiVO.getCalledNum());
                        subs.setRightChannelNum(caller);
                    }
                }
            }
            controlRespDto.setSubs(subs);
        }
        response.setControlRespDto(controlRespDto);
        callControlResponseVO.setResult(response);
        return callControlResponseVO;
    }

}
