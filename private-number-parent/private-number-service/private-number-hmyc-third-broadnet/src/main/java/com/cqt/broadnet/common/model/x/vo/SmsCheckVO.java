package com.cqt.broadnet.common.model.x.vo;

import com.cqt.broadnet.common.model.x.dto.SmsCheckDTO;
import com.cqt.broadnet.common.utils.FormatUtil;
import com.cqt.common.enums.ControlOperateEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author linshiqiang
 * date:  2023-04-26 14:53
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SmsCheckVO {

    /**
     * 是	短信标识，由话音开放平台生成，唯一标识一个呼叫。
     */
    private String msgIdentifier;

    /**
     * 是	控制操作类型
     * REJECT:（拦截）
     * CONTINUE:（接续）
     */
    private String controlOperate;

    /**
     * 否	被叫号码，X模式时必须携带，其他模式不涉及
     * 此号码为短信接续方的真实号码。
     * 号码格式遵循国际电信联盟定义的E.164标准。
     */
    private String called;

    /**
     * 否	短信下发显示的主叫号码，X模式时必须携带，其他模式不涉及
     * 号码格式遵循国际电信联盟定义的E.164标准。
     */
    private String displayCalling;

    public static SmsCheckVO reject(SmsCheckDTO smsCheckDTO) {
        SmsCheckVO smsCheckVO = new SmsCheckVO();
        smsCheckVO.setMsgIdentifier(smsCheckDTO.getMsgIdentifier());
        smsCheckVO.setControlOperate(ControlOperateEnum.REJECT.name());
        return smsCheckVO;
    }

    public static SmsCheckVO reject(String msgIdentifier) {
        SmsCheckVO smsCheckVO = new SmsCheckVO();
        smsCheckVO.setMsgIdentifier(msgIdentifier);
        smsCheckVO.setControlOperate(ControlOperateEnum.REJECT.name());
        return smsCheckVO;
    }

    public static SmsCheckVO ok(SmsCheckDTO smsCheckDTO, String called) {
        SmsCheckVO smsCheckVO = new SmsCheckVO();
        smsCheckVO.setMsgIdentifier(smsCheckDTO.getMsgIdentifier());
        smsCheckVO.setControlOperate(ControlOperateEnum.CONTINUE.name());
        smsCheckVO.setCalled(called);
        smsCheckVO.setDisplayCalling(smsCheckDTO.getDisplayCalling());
        smsCheckVO.transfer();
        return smsCheckVO;
    }

    public static SmsCheckVO ok(SmsCheckDTO smsCheckDTO) {
        SmsCheckVO smsCheckVO = new SmsCheckVO();
        smsCheckVO.setControlOperate(ControlOperateEnum.CONTINUE.name());
        smsCheckVO.setMsgIdentifier(smsCheckDTO.getMsgIdentifier());
        return smsCheckVO;
    }

    /**
     * 号码格式遵循国际电信联盟定义的E.164标准
     */
    private void transfer() {
        setCalled(FormatUtil.getNumber164(this.getCalled()));
        setDisplayCalling(FormatUtil.getNumber164(this.getDisplayCalling()));
    }
}
