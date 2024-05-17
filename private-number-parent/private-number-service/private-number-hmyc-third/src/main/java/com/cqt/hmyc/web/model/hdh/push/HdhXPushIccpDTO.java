package com.cqt.hmyc.web.model.hdh.push;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author huweizhong
 * date  2023/6/19 15:10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HdhXPushIccpDTO {
    private static final long serialVersionUID = -8056276556567752106L;

    /**
     * 应用 Id，标志出要操作的应用。
     * 若账户名下只有一个应用，本字段可选填。
     * 若不填此字段，则操作针对该应用。
     * 若账户名下有多个应用，本字段必填，否则
     * 会报错
     */
    private String appId;

    /**
     * 绑定关系 ID
     */
    private String bindId;

    /**
     * 通话 ID
     */
    private String callId;


    /**
     * 主叫号码
     */
    private String callNo;


    /**
     * 被叫号码：
     */
    private String peerNo ;

    /**
     * 中间号码：
     */
    private String x;

    /**
     * 通话发生时间
     */
    private String callTime;


    /**
     * 通话开始时间：
     */
    private String startTime;


    /**
     *   通话结束时间：
     */
    private String finishTime;


    /**
     *     通话时长
     */
    private Integer callDuration;


    /**
     *     结束发起方
     */
    private String finishType;

    /**
     *     结束状态（即挂断原因）：
     */
    private String finishState;

    /**
     *     回传参数
     */
    private String userData;

    /**
     *     主叫视频彩铃播放结果
     */
    private String callNoVrbtResult;

    /**
     *     视频彩铃播放时长，单位秒
     */
    private Integer callNoVrbtDuration;

    /**
     *     振铃时间：
     */
    private String ringTime;

    /**
     *     通话录音文件保存
     */
    private String recordUrl;


    /**
     *     发送短信时间
     */
    private String smsTime;

    /**
     *     smsResult
     */
    private String smsResult;

    /**
     *     短信条数（长短信被拆分的条数）
     */
    private Integer smsNumber;

    /**
     *     企业id
     */
    private String vccId;

    /**
     *     bindTime
     */
    private String bindTime;

    /**
     *     请求id
     */
    private String requestId;

    /**
     *     短信内容
     */
    private String smsContent;

    private String supplierId;

    private String ext;
}
