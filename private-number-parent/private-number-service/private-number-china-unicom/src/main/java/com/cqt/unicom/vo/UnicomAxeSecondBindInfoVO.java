package com.cqt.unicom.vo;

import com.cqt.model.bind.vo.BindInfoVO;

/**
 * @author huweizhong
 * date  2023/7/6 13:49
 */

public class UnicomAxeSecondBindInfoVO {
    /**
     * 状态码，200 表示成功 其他表示处理失败
     */
    private Integer code;
    /**
     * 失败时，错误原因描述必填
     */
    private String msg;
    /**
     * 0 挂断
     * 1 接续
     * 2 再次收号
     */
    private String opType;
    /**
     *客户平台指定 ID 标识
     */
    private String uId;
    /**
     * 呼出主叫号码, optype 为 1 时必填
     */
    private String caller;
    /**
     * 呼出被叫号码, optype 为 1 时必填
     */
    private String callee;


    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getCaller() {
        return caller;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCallee() {
        return callee;
    }

    public void setCallee(String callee) {
        this.callee = callee;
    }

    public String getDtmfLength() {
        return dtmfLength;
    }

    public void setDtmfLength(String dtmfLength) {
        this.dtmfLength = dtmfLength;
    }

    public String getDtmfWaitingTime() {
        return dtmfWaitingTime;
    }

    public void setDtmfWaitingTime(String dtmfWaitingTime) {
        this.dtmfWaitingTime = dtmfWaitingTime;
    }

    public String getFrontDtmf() {
        return frontDtmf;
    }

    public void setFrontDtmf(String frontDtmf) {
        this.frontDtmf = frontDtmf;
    }

    public String getAfterDtmfOk() {
        return afterDtmfOk;
    }

    public void setAfterDtmfOk(String afterDtmfOk) {
        this.afterDtmfOk = afterDtmfOk;
    }

    public String getAfterDtmfFail() {
        return afterDtmfFail;
    }

    public void setAfterDtmfFail(String afterDtmfFail) {
        this.afterDtmfFail = afterDtmfFail;
    }

    /**
     * 再次收号的长度
     * 0 表示未知，必须以#号结束
     * 1 至 30 表示，明确知道需要的长度
     * 31 至 98 表示无效，视为 0
     * 99 表示不生效，不执行再次收号
     */
    private String dtmfLength;
    /**
     * DTMF 最大等待时长，单位秒，有效值范
     * 围 5 至 60，范围之外视为未设置
     * 未设置时缺省值为 15
     */
    private String dtmfWaitingTime;
    /**
     * 再次收号前的引导音频文件
     */
    private String frontDtmf;
    /**
     * 再次收号后的终结音频文件, 有效按键时播
     */
    private String afterDtmfOk;

    public String getConnectAudioToUp() {
        return connectAudioToUp;
    }

    public void setConnectAudioToUp(String connectAudioToUp) {
        this.connectAudioToUp = connectAudioToUp;
    }

    public String getConnectAudioToDown() {
        return connectAudioToDown;
    }

    public void setConnectAudioToDown(String connectAudioToDown) {
        this.connectAudioToDown = connectAudioToDown;
    }

    /**
     *DTMF 后的终结音频文件，无效按键时播
     */
    private String afterDtmfFail;

    /**
     * 接通后,向主叫播放的音频文件编码
     */
    private String connectAudioToUp;
    /**
     *  接通后,向被叫叫播放的音频文件编码
     */
    private String connectAudioToDown;

    public static UnicomAxeSecondBindInfoVO buildUnicomBindInfoVO(BindInfoVO bindInfoApiVO){
        UnicomAxeSecondBindInfoVO unicomAxeBindInfoVO = new UnicomAxeSecondBindInfoVO();
        unicomAxeBindInfoVO.setCode(200);
        unicomAxeBindInfoVO.setMsg(bindInfoApiVO.getMessage());
        if (bindInfoApiVO.getCode() == 200){
            //接续
            unicomAxeBindInfoVO.setOpType("1");
        }else if (bindInfoApiVO.getCode() == 9997){
            //再次收号
            unicomAxeBindInfoVO.setOpType("2");
        }else {
            unicomAxeBindInfoVO.setOpType("0");
        }
        unicomAxeBindInfoVO.setCallee(bindInfoApiVO.getCalledNum());
        unicomAxeBindInfoVO.setCaller(bindInfoApiVO.getCallNum());
        unicomAxeBindInfoVO.setConnectAudioToUp(bindInfoApiVO.getCallerIvr());
        unicomAxeBindInfoVO.setConnectAudioToDown(bindInfoApiVO.getCalledIvr());
        return unicomAxeBindInfoVO;
    }
}
