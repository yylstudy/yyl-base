package com.cqt.cloudcc.manager.service;

import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.cqt.model.freeswitch.dto.api.*;
import com.cqt.model.freeswitch.vo.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author linshiqiang
 * date:  2023-07-13 9:42
 */
public interface FreeswitchRequestService {

    /**
     * 外呼单路通话接口，支持外呼时录制
     */
    FreeswitchApiVO originate(OriginateDTO originateDTO) throws Exception;

    /**
     * 挂断通话接口，支持单路挂断和双路挂断
     */
    FreeswitchApiVO hangup(HangupDTO hangupDTO);

    /**
     * **挂断长通分机(实际通话)** *hangup_always_ext*
     * 挂断长通分机真实通话，如果有桥接默认会将另外一路也挂断
     */
    FreeswitchApiVO hangupAlwaysExt(HangupDTO hangupDTO);

    /**
     * 语音切换接口，用于通话中的音视频切换
     */
    FreeswitchApiVO mediaToggle(MediaToggleDTO mediaToggleDTO);

    /**
     * 用于将已存在的两路通话桥接在一起
     */
    FreeswitchApiVO bridge(BridgeDTO bridgeDTO);

    /**
     * 外呼一路新的通话，并且将此通话和一路已存在的通话进行桥接
     */
    FreeswitchApiVO callBridge(CallBridgeDTO callBridgeDTO);

    /**
     * 接听某一路通话，可选择视频接听或者音频接听
     */
    FreeswitchApiVO answer(AnswerDTO answerDTO);

    /**
     * 往某路通话进行放音接口，支持tts放音。如果有填写tts引擎信息，优先走播放tts，否则走播放文件流程。放音开始和结束通过event(play_status)状态进行推送
     */
    FreeswitchApiVO playback(PlaybackDTO playbackDTO);

    /**
     * 停止放音
     */
    FreeswitchApiVO stopPlay(StopPlayDTO stopPlayDTO);

    /**
     * 二次拨号流程，往某路发送按键接口
     */
    FreeswitchApiVO sendDtmf(SendDtmfDTO sendDtmfDTO);

    /**
     * 通话保持、恢复接口
     */
    FreeswitchApiVO hold(HoldDTO holdDTO);

    /**
     * 录制通话接口
     */
    RecordVO record(RecordDTO recordDTO);

    /**
     * 结束录音
     */
    FreeswitchApiVO stopRecord(StopRecordDTO stopRecordDTO);

    /**
     * 坐席A和客户B正在通话，坐席A需要咨询坐席C或者将通话转接给坐席C或者将坐席C拉入进行三方通话
     * 坐席A和坐席B正在通话，坐席C现在要和坐席A进行单向通话并且坐席A和坐席B通话不中断，并且坐席B无感知(耳语)，或者坐席C监听坐席A和客户B的通话，坐席A和客户B无感知
     * 调用此接口前，C坐席要先外呼接通之后再调用此接口。
     */
    FreeswitchApiVO xfer(XferDTO xferDTO) throws Exception;

    /**
     * 通过接口调用FS往通话写入通道变量
     */
    FreeswitchApiVO setSessionVar(SetSessionVarDTO setSessionVarDTO);

    /**
     * 通过接口调用FS获取session通道变量
     */
    GetSessionVarVO getSessionVar(GetSessionVarDTO getSessionVarDTO);

    /**
     * 通过接口调用FS执行lua脚本
     */
    ExecuteLuaVO executeLua(ExecuteLuaDTO executeLuaDTO);

    /**
     * [特殊接口] 通话执行lua脚本，一般用于呼入(转IVR、转满意度等场景)
     */
    ExecuteLuaVO trans2lua(ExecuteLuaDTO executeLuaDTO);

    /**
     * 查询分机当前的注册状态
     */
    GetExtensionRegVO getExtensionReg(GetExtensionRegStatusDTO getExtensionRegStatusDTO);

    /**
     * 排队超时时调用，用于结束话务排队，接着走IVR流程
     */
    FreeswitchApiVO callQueueExit(CallQueueExitDTO callQueueExitDTO);

    /**
     * 退出ivr操作, 接着走IVR流程
     */
    FreeswitchApiVO callIvrExit(CallIvrExitDTO callIvrExitDTO);

    /**
     * 获取分机注册地址
     */
    DisExtensionRegAddrVO disExtensionRegAddr(DisExtensionRegAddrDTO disExtensionRegAddrDTO);

    /**
     * 进入IVR，执行lua
     * [特殊接口]呼入事件收到之后，接听电话，然后执行lua，走IVR流程
     */
    FreeswitchApiVO callIvrLua(CallIvrLuaDTO callIvrLuaDTO);

    /**
     * **队列呼叫坐席** *call_queue_to_agent*
     * 排队找到空闲坐席，呼叫坐席前调用，避免检测到超时，分机还未接起，就停止排队了
     */
    FreeswitchApiVO callQueueToAgent(CallQueueToAgentDTO callQueueToAgentDTO);

    /**
     * ## **放音收号** *play_and_get_digits*
     * 播放语音并且收号，支持tts播放。如果有填写tts引擎信息，优先走播放tts，否则走播放文件流程。按键结果通过event(dtmf)推送
     */
    @PostMapping("play_and_get_digits")
    FreeswitchApiVO playAndGetDigits(@RequestBody PlayAndGetDigitsDTO playAndGetDigitsDTO);

    /**
     * **查询企业当前并发数** *get_online_company_concurrency*
     */
    CompanyConcurrencyVO getOnlineCompanyConcurrency(@RequestBody FreeswitchApiBase freeswitchApiBase);

    /**
     * ## **放音控制** *playback_control*
     * 文件播放暂停、开始、倍速、指定位置播放，在playback有效。
     */
    FreeswitchApiVO playbackControl(@RequestBody PlaybackControlDTO playbackControlDTO);
}
