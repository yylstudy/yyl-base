package com.cqt.hmyc.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.cache.CorpBusinessConfigCache;
import com.cqt.common.constants.TaobaoApiConstant;
import com.cqt.common.enums.BehaviorType;
import com.cqt.common.enums.ControlOperateEnum;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.enums.NumberTypeEnum;
import com.cqt.common.util.TaobaoApiClient;
import com.cqt.hmyc.config.AudioCodeUtil;
import com.cqt.hmyc.web.service.BindInfoQueryService;
import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.call.dto.StartCallRequest;
import com.cqt.model.call.vo.AlibabaAliqinAxbVendorCallControlResponse;
import com.cqt.model.call.vo.CallControlResponse;
import com.cqt.model.call.vo.TaobaoBindInfoVO;
import com.cqt.model.properties.TaobaoApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-01-28 11:21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BindInfoQueryServiceImpl implements BindInfoQueryService {

    private final TaobaoApiClient taobaoApiClient;

    private final TaobaoApiProperties taobaoApiProperties;

    private final ObjectMapper objectMapper;

    @Override
    public TaobaoBindInfoVO getBindInfo(BindInfoApiQuery bindInfoApiQuery) {
        if (log.isInfoEnabled()) {
            log.info("查询绑定参数: {}", JSON.toJSONString(bindInfoApiQuery));
        }
        String vccId = bindInfoApiQuery.getVccId();
        Optional<CallControlResponse> responseOptional = requestBind(bindInfoApiQuery);
        if (!responseOptional.isPresent()) {
            // 未查到绑定关系, 返回异常
            return TaobaoBindInfoVO.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage(), getNotBindIvr(vccId));
        }
        AlibabaAliqinAxbVendorCallControlResponse callControlResponse = responseOptional.get().getAlibabaAliqinAxbVendorCallControlResponse();
        AlibabaAliqinAxbVendorCallControlResponse.Response result = callControlResponse.getResult();
        String code = result.getCode();
        if (!TaobaoApiConstant.OK.equals(code)) {
            // 返回不是OK
            log.error("返回结果code不是OK");
            return TaobaoBindInfoVO.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage(), getNotBindIvr(vccId));
        }

        // 绑定关系结果 to BindInfoVO
        AlibabaAliqinAxbVendorCallControlResponse.Response.ControlRespDto controlRespDto = result.getControlRespDto();
        return convertResultVO(controlRespDto, bindInfoApiQuery);
    }

    private Optional<CallControlResponse> requestBind(BindInfoApiQuery bindInfoApiQuery) {
        StartCallRequest request = new StartCallRequest();
        request.setExtension(bindInfoApiQuery.getDigitInfo());
        request.setSecretNo(bindInfoApiQuery.getCalled());
        request.setCallNo(bindInfoApiQuery.getCaller());
        request.setCallTime(DateUtil.now());
        request.setCallId(bindInfoApiQuery.getCallId());
        request.setRecordType(StrUtil.isEmpty(bindInfoApiQuery.getBehaviorType()) ? BehaviorType.CALL.name() : bindInfoApiQuery.getBehaviorType());
        request.setVendorKey(taobaoApiProperties.getVendorKey());
        try {
            String url = taobaoApiProperties.getRequestUrl();
            // 测试接口
            if (taobaoApiProperties.getTestBind().getTest()) {
                url = taobaoApiProperties.getTestBind().getTestAxbUrl();
            }
            log.info("查询绑定请求接口地址: {}", url);
            Optional<String> stringOptional = taobaoApiClient.callApi(url, request.getApiMethodName(), request.getTextParams(request));
            if (stringOptional.isPresent()) {
                String result = stringOptional.get();
                if (result.contains(TaobaoApiConstant.ERROR_RESPONSE)) {
                    log.error("接口返回error_response");
                    return Optional.empty();
                }
                if (StrUtil.isEmpty(result)) {
                    log.error("接口返回空");
                    return Optional.empty();
                }
                // 结果json处理
                return Optional.of(objectMapper.readValue(result, CallControlResponse.class));
            }
        } catch (Exception e) {
            log.error("接口taobao请求失败: ", e);
        }
        return Optional.empty();
    }

    private TaobaoBindInfoVO convertResultVO(AlibabaAliqinAxbVendorCallControlResponse.Response.ControlRespDto controlRespDto,
                                             BindInfoApiQuery bindInfoApiQuery) {
        String vccId = bindInfoApiQuery.getVccId();
        TaobaoBindInfoVO bindInfoVO = new TaobaoBindInfoVO();
        bindInfoVO.setCode(0);
        bindInfoVO.setMessage("查询成功!");
        bindInfoVO.setMaxDuration(taobaoApiProperties.getMaxDuration());
        // 接续控制信息:CONTINUE(接续),REJECT(拦截),IVR(收取用户键盘输入内容
        String controlOperate = controlRespDto.getControlOperate();
        if (ControlOperateEnum.REJECT.name().equals(controlOperate)) {
            // 拦截
            return TaobaoBindInfoVO.fail(ErrorCodeEnum.CALL_REJECT.getCode(), ErrorCodeEnum.CALL_REJECT.getMessage(), AudioCodeUtil.getAudioFileName(controlRespDto.getCallNoPlayCode()));
        }

        if (ControlOperateEnum.IVR.name().equals(controlOperate)) {
            // 输入分机号  放音: 请输入分机号
            return TaobaoBindInfoVO.okExt(0, "分机号模式(收取用户键盘输入内容)", controlRespDto.getCallNoPlayCode(), NumberTypeEnum.XE.name());
        }

        // 对应到小号平台的能力类型:AXB、AXN、AXN_EXTENSION_REUSE(AXN分机复用)

        bindInfoVO.setNumType(controlRespDto.getProductType());
        // 主叫放音编码 需转化为放音文件名
        bindInfoVO.setCallerIvr(AudioCodeUtil.getAudioFileName(controlRespDto.getCallNoPlayCode()));
        // 被叫放音
        bindInfoVO.setCalledIvr(AudioCodeUtil.getAudioFileName(controlRespDto.getCalledNoPlayCode()));
        // 是否媒体资源降级,放弃录音放音功能；接入方无此相关功能，可忽略;  0：否、1：是
        bindInfoVO.setMediaDegrade(BooleanUtil.toInt(controlRespDto.getMediaDegrade()));

        AlibabaAliqinAxbVendorCallControlResponse.Response.ControlRespDto.Subs subs = controlRespDto.getSubs();
        // sms_channel 短信通道方式SMS_INTERCEPT(拦截推送阿里)，SMS_NORMAL_SEND(正常现网下发)，SMS_DROP(拦截丢弃)
        bindInfoVO.setSmsChannel(subs.getSmsChannel());

        // 顺振参数数组
        bindInfoVO.setSequenceCalls(dealSequenceCallList(subs));

        // 被叫号码
        bindInfoVO.setCalledNum(subs.getCalledNo());
        // 被叫来显号码
        bindInfoVO.setDisplayNum(subs.getCalledDisplayNo());
        // 中间X 发起呼叫的号码
        bindInfoVO.setCallNum(bindInfoApiQuery.getCalled());

        // 呼叫类型MASTER(A->X->B), CALLED(B->X->A), SMS_SENDER, SMS_RECEIVER
        bindInfoVO.setCallType(subs.getCallType());

        // 录音类型，mp3/wav
        bindInfoVO.setRecordFileFormat(subs.getRecType());

        // 订购关系ID；目前字符串长度为16位，建议预留32位
        bindInfoVO.setBindId(subs.getSubsId());

        // 顺振超时时间
        bindInfoVO.setSequenceTimeout(subs.getSequenceTimeout());

        // 是否录音
        bindInfoVO.setEnableRecord(BooleanUtil.toInt(subs.getNeedRecord()));

        // 录音内容模式，1：仅录制通话录音、2：放音录音+通话录音
        bindInfoVO.setRecordContentMode(Convert.toInt(subs.getRecordMode()));

        // 是否需要优先下载录音，0：否、1：是
        bindInfoVO.setFastRecord(Convert.toInt(subs.getFastRecord()));

        // 是否开启铃音检测 0：不开启 1：开启
        bindInfoVO.setRrdsControl(subs.getRrdsControl());

        bindInfoVO.setWsAddr(subs.getWsAddr());
        bindInfoVO.setWsAddrCalled(subs.getWsAddrCalled());
        bindInfoVO.setOutId(subs.getOutId());
        bindInfoVO.setNeedRealtimeMedia(BooleanUtil.toInt(subs.getNeedRealtimeMedia()));
        bindInfoVO.setRtpType(Convert.toInt(subs.getRtpType()));

        // 挂机IVR参数
        bindInfoVO.setEndCallIvr(dealEndCallIvr(subs));
        if (log.isInfoEnabled()) {
            log.info("绑定关系处理结果: {}", JSON.toJSONString(bindInfoVO));
        }
        return bindInfoVO;
    }

    /**
     * 挂机IVR参数
     */
    private TaobaoBindInfoVO.EndCallIvr dealEndCallIvr(AlibabaAliqinAxbVendorCallControlResponse.Response.ControlRespDto.Subs subs) {
        TaobaoBindInfoVO.EndCallIvr endCallIvr = new TaobaoBindInfoVO.EndCallIvr();

        AlibabaAliqinAxbVendorCallControlResponse.Response.ControlRespDto.EndCallIvr subsEndCallIvr = subs.getEndCallIvr();
        if (ObjectUtil.isEmpty(subsEndCallIvr)){
            return null;
        }
        endCallIvr.setEndCallIvr(subsEndCallIvr.getEndCallIvr());
        endCallIvr.setValidKey(subsEndCallIvr.getValidKey());
        endCallIvr.setWaitingDtmfTime(subsEndCallIvr.getWaitingDtmfTime());
        endCallIvr.setMaxLoop(subsEndCallIvr.getMaxLoop());
        endCallIvr.setWaitingEndCall(subsEndCallIvr.getWaitingEndCall());
        // 这里带.wav后缀
        endCallIvr.setStep1File(subsEndCallIvr.getStep1File());
        endCallIvr.setStep2File(subsEndCallIvr.getStep2File());
        return endCallIvr;
    }

    /**
     * 顺振参数数组处理
     */
    private List<TaobaoBindInfoVO.SequenceCall> dealSequenceCallList(AlibabaAliqinAxbVendorCallControlResponse.Response.ControlRespDto.Subs subs) {
        AlibabaAliqinAxbVendorCallControlResponse.Response.ControlRespDto.SequenceCalls sequenceCalls = subs.getSequenceCalls();
        if (ObjectUtil.isEmpty(sequenceCalls)) {
            return Lists.newArrayList();
        }
        List<AlibabaAliqinAxbVendorCallControlResponse.Response.ControlRespDto.SequenceCalls.SequenceCall> sequenceCallList = sequenceCalls.getSequenceCalls();
        if (CollUtil.isEmpty(sequenceCallList)) {
            return Lists.newArrayList();
        }
        List<TaobaoBindInfoVO.SequenceCall> list = new ArrayList<>();
        sequenceCallList.forEach(item -> {
            TaobaoBindInfoVO.SequenceCall sequenceCall = new TaobaoBindInfoVO.SequenceCall();
            sequenceCall.setCalledNo(item.getCalledNo());
            sequenceCall.setCalledDisplayNo(item.getCalledDisplayNo());
            // 放音编码
            sequenceCall.setCallNoPlayCode(AudioCodeUtil.getAudioFileName(item.getCallNoPlayCode()));
            sequenceCall.setCalledNoPlayCode(AudioCodeUtil.getAudioFileName(item.getCalledNoPlayCode()));
            list.add(sequenceCall);
        });
        return list;
    }

    private String getNotBindIvr(String vccId) {
        Optional<String> notBindIvrOptional = CorpBusinessConfigCache.getNotBindIvr(vccId);
        return notBindIvrOptional.orElse(taobaoApiProperties.getNotBindIvr());
    }

    private String getDigitsIvr(String vccId) {
        Optional<String> digitsIvrOptional = CorpBusinessConfigCache.getDigitsIvr(vccId);
        return digitsIvrOptional.orElse(taobaoApiProperties.getDigitsIvr());
    }

}
