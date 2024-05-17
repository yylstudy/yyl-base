package com.cqt.unicom.vo;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.bind.vo.BindInfoVO;
import com.cqt.unicom.cache.UnicomLocalCacheService;

/**
 * @author huweizhong
 * date  2023/7/5 19:57
 */

public class UnicomAxbBindInfoVO {

    private Integer code;

    @Override
    public String toString() {
        return "UnicomAxbBindInfoVO{" +
                "code=" + code +
                ", processType='" + processType + '\'' +
                ", data=" + data +
                '}';
    }

    private String processType;

    private DataInfo data;

    public Integer getCode() {
        return code;
    }

    public String getProcessType() {
        return processType;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public void setData(DataInfo data) {
        this.data = data;
    }

    public DataInfo getData() {
        return data;
    }

    public static class DataInfo{
        public void setuId(String uId) {
            this.uId = uId;
        }

        @Override
        public String toString() {
            return "DataInfo{" +
                    "uId='" + uId + '\'' +
                    ", caller='" + caller + '\'' +
                    ", callee='" + callee + '\'' +
                    ", limitTime='" + limitTime + '\'' +
                    ", audioCode='" + audioCode + '\'' +
                    ", ussd=" + ussd +
                    ", noAnswerForward='" + noAnswerForward + '\'' +
                    ", busyForword='" + busyForword + '\'' +
                    ", altCallee='" + altCallee + '\'' +
                    ", connectAudioToUp='" + connectAudioToUp + '\'' +
                    ", connectAudioToDown='" + connectAudioToDown + '\'' +
                    ", hangupDtmf='" + hangupDtmf + '\'' +
                    ", hangupDtmfWaitingTime='" + hangupDtmfWaitingTime + '\'' +
                    ", hangupFrontDtmf='" + hangupFrontDtmf + '\'' +
                    ", hangupAfterDtmfOk='" + hangupAfterDtmfOk + '\'' +
                    ", hangupAfterDtmfFail='" + hangupAfterDtmfFail + '\'' +
                    '}';
        }

        public void setCaller(String caller) {
            this.caller = caller;
        }

        public void setCallee(String callee) {
            this.callee = callee;
        }

        public void setLimitTime(String limitTime) {
            this.limitTime = limitTime;
        }

        public void setAudioCode(String audioCode) {
            this.audioCode = audioCode;
        }

        public void setUssd(Integer ussd) {
            this.ussd = ussd;
        }

        public void setNoAnswerForward(String noAnswerForward) {
            this.noAnswerForward = noAnswerForward;
        }

        public void setBusyForword(String busyForword) {
            this.busyForword = busyForword;
        }

        public void setAltCallee(String altCallee) {
            this.altCallee = altCallee;
        }

        public void setConnectAudioToUp(String connectAudioToUp) {
            this.connectAudioToUp = connectAudioToUp;
        }

        public void setConnectAudioToDown(String connectAudioToDown) {
            this.connectAudioToDown = connectAudioToDown;
        }

        public void setHangupDtmf(String hangupDtmf) {
            this.hangupDtmf = hangupDtmf;
        }

        public void setHangupDtmfWaitingTime(String hangupDtmfWaitingTime) {
            this.hangupDtmfWaitingTime = hangupDtmfWaitingTime;
        }

        public void setHangupFrontDtmf(String hangupFrontDtmf) {
            this.hangupFrontDtmf = hangupFrontDtmf;
        }

        public void setHangupAfterDtmfOk(String hangupAfterDtmfOk) {
            this.hangupAfterDtmfOk = hangupAfterDtmfOk;
        }

        public void setHangupAfterDtmfFail(String hangupAfterDtmfFail) {
            this.hangupAfterDtmfFail = hangupAfterDtmfFail;
        }

        public String getuId() {
            return uId;
        }

        public String getCaller() {
            return caller;
        }

        public String getCallee() {
            return callee;
        }

        public String getLimitTime() {
            return limitTime;
        }

        public String getAudioCode() {
            return audioCode;
        }

        public Integer getUssd() {
            return ussd;
        }

        public String getNoAnswerForward() {
            return noAnswerForward;
        }

        public String getBusyForword() {
            return busyForword;
        }

        public String getAltCallee() {
            return altCallee;
        }

        public String getConnectAudioToUp() {
            return connectAudioToUp;
        }

        public String getConnectAudioToDown() {
            return connectAudioToDown;
        }

        public String getHangupDtmf() {
            return hangupDtmf;
        }

        public String getHangupDtmfWaitingTime() {
            return hangupDtmfWaitingTime;
        }

        public String getHangupFrontDtmf() {
            return hangupFrontDtmf;
        }

        public String getHangupAfterDtmfOk() {
            return hangupAfterDtmfOk;
        }

        public String getHangupAfterDtmfFail() {
            return hangupAfterDtmfFail;
        }

        /**
         * 客户平台指定 ID 标识
         */
        private String uId;

        /**
         * 呼出的主叫号码
         */
        private String caller;

        /**
         * 呼出的被叫号码
         */
        private String callee;

        /**
         * 接通时长限制，单位为秒，有效值范围
         * 60 至 3600，范围之外视为未设置
         * 该参数通常不需要设置，最大时长由基
         * 础网限制
         */
        private String limitTime;

        /**
         * 企业彩铃的音频文件编码
         */
        private String audioCode;
        /**
         * 企业名片 1: 主叫企业名片
         *        2：被叫企业名片
         */
        private Integer ussd;
        /**
         *  无应答前转 0：不前转
         *           1：前转
         */
        private String noAnswerForward;
        /**
         * 遇忙前转 0：不前转
         *        1：前转
         */
        private String busyForword;
        /**
         * 前转号码
         */
        private String altCallee;
        /**
         * 接通后,向主叫播放的音频文件编码
         */
        private String connectAudioToUp;
        /**
         * 接通后,向被叫叫播放的音频文件编码
         */
        private String connectAudioToDown;
        /**
         * 挂断前的 DTMF 收号功能
         * 0：不生效
         * 1：主叫挂断时，被叫 DTMF 流程
         */
        private String hangupDtmf;
        /**
         * DTMF 最大等待时长，单位秒，有效值范
         * 围 5 至 30，范围之外视为未设置
         * 未设置时缺省值为 5
         */
        private String hangupDtmfWaitingTime;
        /**
         * DTMF 前的引导音频文件
         */
        private String hangupFrontDtmf;
        /**
         *  DTMF 后的终结音频文件,有效按键时播
         */
        private String hangupAfterDtmfOk;
        /**
         * DTMF 后的终结音频文件，无效按键时播
         */
        private String hangupAfterDtmfFail;

    }
    public static UnicomAxbBindInfoVO buildUnicomBindInfoVO(BindInfoApiVO bindInfoApiVO, String callee){
        UnicomAxbBindInfoVO unicomAxbBindInfoVO = new UnicomAxbBindInfoVO();
        unicomAxbBindInfoVO.setCode(200);
        unicomAxbBindInfoVO.setProcessType("0");

        DataInfo dataInfo = new DataInfo();
        //中间号
        dataInfo.setCaller(callee);
        dataInfo.setCallee(bindInfoApiVO.getCalledNum());
        dataInfo.setConnectAudioToUp(bindInfoApiVO.getCallerIvr());
        dataInfo.setConnectAudioToDown(bindInfoApiVO.getCalledIvr());
        unicomAxbBindInfoVO.setData(dataInfo);

        return unicomAxbBindInfoVO;
    }
    public static UnicomAxbBindInfoVO buildUnicomBindInfoVO(String message){
        UnicomAxbBindInfoVO unicomAxbBindInfoVO = new UnicomAxbBindInfoVO();
        unicomAxbBindInfoVO.setCode(200);
        unicomAxbBindInfoVO.setProcessType("0");
        JSONObject jsonObject = JSONObject.parseObject(message);
        String data1 = jsonObject.getString("data");
        UnicomAxbBindInfoVO.DataInfo dataInfo1 = JSONUtil.toBean(data1, UnicomAxbBindInfoVO.DataInfo.class);

//        DataInfo dataInfo = new DataInfo();
//        //中间号
//        dataInfo.setCaller(callee);
//        dataInfo.setCallee(bindInfoApiVO.getCalledNum());
//        dataInfo.setConnectAudioToUp(bindInfoApiVO.getCallerIvr());
//        dataInfo.setConnectAudioToDown(bindInfoApiVO.getCalledIvr());
        unicomAxbBindInfoVO.setData(dataInfo1);

        return unicomAxbBindInfoVO;
    }
}
