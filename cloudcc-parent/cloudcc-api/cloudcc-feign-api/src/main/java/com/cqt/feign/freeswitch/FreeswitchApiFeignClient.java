package com.cqt.feign.freeswitch;

import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.cqt.model.freeswitch.dto.api.*;
import com.cqt.model.freeswitch.vo.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

/**
 * @author linshiqiang
 * date 2023-06-16 21:54:00
 * 底层fs接口调用客户端
 */
@FeignClient(name = "base-api", url = "${cloudcc.base.base-url}")
public interface FreeswitchApiFeignClient {

    /**
     * 外呼单路通话接口，支持外呼时录制
     */
    @PostMapping("originate")
    FreeswitchApiVO originate(URI uri, @RequestBody OriginateDTO originateDTO);

    /**
     * 挂断通话接口，支持单路挂断和双路挂断
     */
    @PostMapping("hangup")
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 1.5))
    FreeswitchApiVO hangup(URI uri, @RequestBody HangupDTO hangupDTO);

    /**
     * **挂断长通分机(实际通话)** *hangup_always_ext*
     * 挂断长通分机真实通话，如果有桥接默认会将另外一路也挂断
     */
    @PostMapping("hangup_always_ext")
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 1.5))
    FreeswitchApiVO hangupAlwaysExt(URI uri, @RequestBody HangupDTO hangupDTO);

    /**
     * 语音切换接口，用于通话中的音视频切换
     */
    @PostMapping("media_reneg")
    FreeswitchApiVO mediaToggle(URI uri, @RequestBody MediaToggleDTO mediaToggleDTO);

    /**
     * 用于将已存在的两路通话桥接在一起
     */
    @PostMapping("bridge")
    FreeswitchApiVO bridge(URI uri, @RequestBody BridgeDTO bridgeDTO);

    /**
     * 外呼一路新的通话，并且将此通话和一路已存在的通话进行桥接
     */
    @PostMapping("call_bridge")
    FreeswitchApiVO callBridge(URI uri, @RequestBody CallBridgeDTO callBridgeDTO);

    /**
     * 接听某一路通话，可选择视频接听或者音频接听
     */
    @PostMapping("answer")
    FreeswitchApiVO answer(URI uri, @RequestBody AnswerDTO answerDTO);

    /**
     * 往某路通话进行放音接口，支持tts放音。如果有填写tts引擎信息，优先走播放tts，否则走播放文件流程。放音开始和结束通过event(play_status)状态进行推送
     */
    @PostMapping("playback")
    FreeswitchApiVO playback(URI uri, @RequestBody PlaybackDTO playbackDTO);

    /**
     * 停止放音
     */
    @PostMapping("stop_play")
    FreeswitchApiVO stopPlay(URI uri, @RequestBody StopPlayDTO stopPlayDTO);

    /**
     * 二次拨号流程，往某路发送按键接口
     */
    @PostMapping("send_dtmf")
    FreeswitchApiVO sendDtmf(URI uri, @RequestBody SendDtmfDTO sendDtmfDTO);

    /**
     * 通话保持、恢复接口
     */
    @PostMapping("hold")
    FreeswitchApiVO hold(URI uri, @RequestBody HoldDTO holdDTO);

    /**
     * 录制通话接口
     */
    @PostMapping("record")
    RecordVO record(URI uri, @RequestBody RecordDTO recordDTO);

    /**
     * 结束录音
     */
    @PostMapping("stop_record")
    FreeswitchApiVO stopRecord(URI uri, @RequestBody StopRecordDTO stopRecordDTO);

    /**
     * 坐席A和客户B正在通话，坐席A需要咨询坐席C或者将通话转接给坐席C或者将坐席C拉入进行三方通话
     * 坐席A和坐席B正在通话，坐席C现在要和坐席A进行单向通话并且坐席A和坐席B通话不中断，并且坐席B无感知(耳语)，或者坐席C监听坐席A和客户B的通话，坐席A和客户B无感知
     * 调用此接口前，C坐席要先外呼接通之后再调用此接口。
     */
    @PostMapping("xfer")
    FreeswitchApiVO xfer(URI uri, @RequestBody XferDTO xferDTO);

    /**
     * 通过接口调用FS往通话写入通道变量
     */
    @PostMapping("set_session_var")
    FreeswitchApiVO setSessionVar(URI uri, @RequestBody SetSessionVarDTO setSessionVarDTO);

    /**
     * 通过接口调用FS获取session通道变量
     */
    @PostMapping("get_session_var")
    GetSessionVarVO getSessionVar(URI uri, @RequestBody GetSessionVarDTO getSessionVarDTO);

    /**
     * 通过接口调用FS执行lua脚本
     */
    @PostMapping("execute_lua")
    ExecuteLuaVO executeLua(URI uri, @RequestBody ExecuteLuaDTO executeLuaDTO);

    /**
     * [特殊接口] 通话执行lua脚本，一般用于呼入(转IVR、转满意度等场景)
     */
    @PostMapping("trans2lua")
    ExecuteLuaVO trans2lua(URI uri, @RequestBody ExecuteLuaDTO executeLuaDTO);

    /**
     * 查询分机当前的注册状态
     */
    @PostMapping("get_extension_reg")
    GetExtensionRegVO getExtensionReg(URI uri, @RequestBody GetExtensionRegStatusDTO getExtensionRegStatusDTO);

    /**
     * 排队超时时调用，用于结束话务排队，接着走IVR流程
     */
    @PostMapping("call_queue_exit")
    FreeswitchApiVO callQueueExit(URI uri, @RequestBody CallQueueExitDTO callQueueExitDTO);

    /**
     * 退出ivr操作, 接着走IVR流程
     */
    @PostMapping("call2java_exit")
    FreeswitchApiVO callIvrExit(URI uri, @RequestBody CallIvrExitDTO callIvrExitDTO);

    /**
     * 获取分机注册地址，用于签入获取注册地址
     */
    @PostMapping("dis_extension_reg_addr")
    DisExtensionRegAddrVO disExtensionRegAddr(URI uri, @RequestBody DisExtensionRegAddrDTO disExtensionRegAddrDTO);

    /**
     * 配置生效，将lua脚本上传底层接口
     */
    @PostMapping("lua_write")
    LuaWriteVO luaWrite(@RequestBody LuaWriteDTO luaWriteDTO);

    /**
     * 配置生效，将lua脚本上传底层接口
     */
    @PostMapping("lua_write")
    LuaWriteVO luaWrite(URI uri, @RequestBody LuaWriteDTO luaWriteDTO);

    /**
     * 配置生效，将lua脚本上传底层接口
     */
    @PostMapping("lua_delete")
    LuaWriteVO luaDelete(URI uri, @RequestBody LuaWriteDTO luaWriteDTO);

    /**
     * 进入IVR，执行lua
     * [特殊接口]呼入事件收到之后，接听电话，然后执行lua，走IVR流程
     */
    @PostMapping("call_ivr_lua")
    FreeswitchApiVO callIvrLua(URI uri, @RequestBody CallIvrLuaDTO callIvrLuaDTO);

    /**
     * **队列呼叫坐席** *call_queue_to_agent*
     * 排队找到空闲坐席，呼叫坐席前调用，避免检测到超时，分机还未接起，就停止排队了
     */
    @PostMapping("call_queue_to_agent")
    FreeswitchApiVO callQueueToAgent(URI uri, @RequestBody CallQueueToAgentDTO callQueueToAgentDTO);

    /**
     * ## **放音收号** *play_and_get_digits*
     * 播放语音并且收号，支持tts播放。如果有填写tts引擎信息，优先走播放tts，否则走播放文件流程。按键结果通过event(dtmf)推送
     */
    @PostMapping("play_and_get_digits")
    FreeswitchApiVO playAndGetDigits(URI uri, @RequestBody PlayAndGetDigitsDTO playAndGetDigitsDTO);

    /**
     * 获取通话录音存放路径
     */
    @PostMapping("play_record")
    PlayRecordVO getPlayRecord(@RequestBody PlayRecordDTO playbackDTO);

    /**
     * 查询企业当前并发数
     */
    @PostMapping("get_online_company_concurrency")
    CompanyConcurrencyVO getOnlineCompanyConcurrency(URI uri, @RequestBody FreeswitchApiBase freeswitchApiBase);

    /**
     * 文件播放暂停、开始、倍速、指定位置播放，在playback有效。
     */
    @PostMapping("playback_control")
    CompanyConcurrencyVO playbackControl(URI uri, @RequestBody PlaybackControlDTO playbackControlDTO);
}
