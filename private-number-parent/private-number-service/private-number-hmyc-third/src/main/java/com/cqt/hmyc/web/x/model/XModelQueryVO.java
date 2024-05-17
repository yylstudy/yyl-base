package com.cqt.hmyc.web.x.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Xienx
 * @date 2023-06-07 16:45:16:45
 */
@Data
public class XModelQueryVO implements Serializable {

    private static final long serialVersionUID = -238509430351313190L;

    /**
     * 状态码 0000：成功； 其他值都代表失败
     */
    private String code;

    /**
     * 返回结果描述
     */
    private String message;

    /**
     * 操作指令
     * 语音：0挂断 1接通 2收号 短信：0拦截 1转发 2托收
     */
    private String opType;

    /**
     * 主叫放音编号
     */
    private String callNoPlayCode;

    /**
     * 被叫放音编号
     */
    private String calledPlayCode;

    /**
     * 需要接续的被叫号码
     */
    private String calledNo;

    /**
     * 被叫显号
     */
    private String calledDisplayNo;

    /**
     * 录音控制 0：通话不需要录音 1：通话需要录音
     */
    private Integer needRecord;

    /**
     * 绑定id
     */
    private String bindId;

    /**
     * 收号成功提示音，仅收号流程生效，如果不传，播放静音
     */
    private String collectionPlayCode;

    /**
     * 主叫视频彩铃编码
     */
    private String videoCode;

    /**
     * 透传数据
     */
    private String data;
}
