package com.cqt.base.enums;

/**
 * @author linshiqiang
 * date:  2023-07-03 11:24
 * 前端SDK请求消息类型枚举
 */
public enum MsgTypeEnum {

    /**
     * 查询坐席状态
     */
    get_status,

    /**
     * 切换坐席服务模式
     */
    toggle_service_mode,

    /**
     * 迁入
     */
    checkin,

    /**
     * 迁出
     */
    checkout,

    /**
     * 外呼
     */
    call,

    /**
     * 切换状态（包括强制）
     */
    change_status,

    /**
     * 保持/取消保持
     */
    hold,

    /**
     * 静音/取消静音
     */
    mute,

    /**
     * 挂断
     */
    hangup,

    /**
     * 转接
     */
    trans,

    /**
     * 咨询
     */
    consult,

    /**
     * 咨询中转接
     */
    consult_to_trans,

    /**
     * 三方通话
     */
    three_way,

    /**
     * 二次拨号（DTMF）
     */
    dtmf,

    /**
     * 监听
     */
    eavesdrop,

    /**
     * 耳语
     */
    whisper,

    /**
     * 代接
     */
    substitute,

    /**
     * 强拆
     */
    interrupt_call,

    /**
     * 强插
     */
    force_call,

    /**
     * 呼入
     */
    callin,

    /**
     * 接听
     */
    answer,

    /**
     * 坐席状态变化
     */
    agent_change,

    /**
     * 音视频切换
     */
    change_media,

    /**
     * 坐席踢下线
     */
    kick_off,

    /**
     * 结果回调通知
     */
    callback,

    /**
     * 话务条播放录音
     *
     * @since 7.0.0
     */
    play_record,

    /**
     * 话务条播放录音控制
     *
     * @since 7.0.0
     */
    play_record_control,

    /**
     * 外呼任务
     *
     * @since 7.0.0
     */
    call_task,

    /**
     * 预览外呼
     *
     * @since 7.0.0
     */
    preview_out_call,

    /**
     * 获取token
     *
     * @since 7.0.0
     */
    get_token
}
