package com.cqt.hmyc.web.x.model;

import cn.hutool.core.date.DateUtil;
import com.cqt.common.constants.ThirdConstant;
import com.cqt.common.util.ThirdUtils;
import com.cqt.model.push.entity.PrivateStatusInfo;
import lombok.Data;

import java.text.SimpleDateFormat;

/**
 * @author linshiqiang
 * date:  2023-06-07 15:04
 */
@Data
public class XModelStatusDTO {

    /**
     * string <=32 是
     * 绑定关系 ID，唯一确定一组绑定关系
     */
    private String bindId;

    /**
     * string <=32 是 通话 ID，唯一确定一次通话
     */
    private String callId;

    /**
     * string <=32 是 被 叫 手 机 号 码 ， 例 如 ： 8613803111233
     */
    private String calleeNo;

    /**
     * string <=32 是
     * 主 叫 手 机 号 码 ， 例 如 ： 8613803111233
     */
    private String callerNo;

    /**
     * string <=32 是
     * 呼入：CALLIIN
     * 呼出：CALLOUT
     * 振铃事件：ALERTING
     * 摘机事件：PICKUP
     * 挂机事件：HANGUP
     */
    private String event;

    /**
     * string <=32 是 中间号，例如：8613803111233
     */
    private String secretNo;

    /**
     * long 是 事件时间戳
     */
    private Long timeStamp;

    /**
     * 分机号
     */
    private String extensionNo;

    public PrivateStatusInfo buildPrivateStatusInfo(String vccId) {
        PrivateStatusInfo info = new PrivateStatusInfo();
        info.setVccId(vccId);
        info.setRecordId(this.callId);
        info.setBindId(this.bindId);
        info.setCaller(ThirdUtils.getNumberUn86(this.callerNo));
        info.setCalled(ThirdUtils.getNumberUn86(this.calleeNo));
        info.setTelX(ThirdUtils.getNumberUn86(this.secretNo));
        info.setCurrentTime(new SimpleDateFormat(ThirdConstant.yyyyMMddHHmmss).format(this.timeStamp * 1000));
        info.setCallResult(1);
        info.setEvent(getEventMapping(this.getEvent()));
        info.setExt(this.getExtensionNo());
        return info;
    }

    private String getEventMapping(String event) {
        switch (event) {
            case "CALLIIN":
                return "callin";
            case "CALLOUT":
                return "callout";
            case "ALERTING":
                return "ringing";
            case "PICKUP":
                return "answer";
            case "HANGUP":
                return "hangup";
        }
        return "";
    }
}
