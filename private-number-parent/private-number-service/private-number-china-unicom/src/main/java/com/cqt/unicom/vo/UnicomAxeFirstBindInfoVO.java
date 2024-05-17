package com.cqt.unicom.vo;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.model.bind.vo.BindInfoVO;
import lombok.RequiredArgsConstructor;

/**
 * @author huweizhong
 * date  2023/7/6 9:56
 */

@RequiredArgsConstructor
public class UnicomAxeFirstBindInfoVO {

    private Integer code;

    @Override
    public String toString() {
        return "UnicomAxeFirstBindInfoVO{" +
                "code=" + code +
                ", processType='" + processType + '\'' +
                ", data=" + data +
                '}';
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public DataInfo getData() {
        return data;
    }

    public void setData(DataInfo data) {
        this.data = data;
    }

    private String processType;

    private DataInfo data;


    public static class DataInfo {
        public String getuId() {
            return uId;
        }

        public void setuId(String uId) {
            this.uId = uId;
        }

        public String getLimitTime() {
            return limitTime;
        }

        public void setLimitTime(String limitTime) {
            this.limitTime = limitTime;
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

        public String getHangupDtmf() {
            return hangupDtmf;
        }

        public void setHangupDtmf(String hangupDtmf) {
            this.hangupDtmf = hangupDtmf;
        }

        public String getHangupDtmfWaitingTime() {
            return hangupDtmfWaitingTime;
        }

        public void setHangupDtmfWaitingTime(String hangupDtmfWaitingTime) {
            this.hangupDtmfWaitingTime = hangupDtmfWaitingTime;
        }

        public String getHangupFrontDtmf() {
            return hangupFrontDtmf;
        }

        public void setHangupFrontDtmf(String hangupFrontDtmf) {
            this.hangupFrontDtmf = hangupFrontDtmf;
        }

        public String getHangupAfterDtmfOk() {
            return hangupAfterDtmfOk;
        }

        public void setHangupAfterDtmfOk(String hangupAfterDtmfOk) {
            this.hangupAfterDtmfOk = hangupAfterDtmfOk;
        }

        public String getHangupAfterDtmfFail() {
            return hangupAfterDtmfFail;
        }

        public void setHangupAfterDtmfFail(String hangupAfterDtmfFail) {
            this.hangupAfterDtmfFail = hangupAfterDtmfFail;
        }

        /**
         * 客户平台指定 ID 标识
         */
        private String uId;
        /**
         * 接通时长限制，单位为秒，有效值范围
         * 60 至 3600，范围之外视为未设置
         * 该参数通常不需要设置，最大时长由基
         * 础网限制
         */
        private String limitTime;
        /**
         * DTMF 收号长度
         * 0 必须以#号结束
         * 1 至 30 表示，明确知道需要的长度
         */
        private String dtmfLength;
        /**
         * DTMF 最大等待时长，单位秒，有效值范
         * 围 5 至 60，范围之外视为未设置
         * 未设置时缺省值为 15
         */
        private String dtmfWaitingTime;
        /**
         * DTMF 前的引导音频文件编码
         */
        private String frontDtmf;
        /**
         * DTMF 后的终结音频文件编码,有效按键
         * 时播
         */
        private String afterDtmfOk;
        /**
         * DTMF 后的终结音频文件编码，无效按键
         * 时播
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
        /**
         * 挂断前的 DTMF 收号功能
         * 0：不生效
         * 1：主叫挂断时，被叫 DTMF 流程
         * 2：被叫挂断时，主叫 DTMF 流程
         */
        private String hangupDtmf;
        /**
         * DTMF 最大等待时长，单位秒，有效值范
         * 围 5 至 30，范围之外视为未设置
         * 未设置时缺省值为 5
         */
        private String hangupDtmfWaitingTime;
        /**
         * DTMF 前的引导音频文件编码
         */
        private String hangupFrontDtmf;
        /**
         * DTMF 后的终结音频文件编码,有效按键
         * 时播
         */
        private String hangupAfterDtmfOk;
        /**
         * DTMF 后的终结音频文件编码，无效按键
         * 时播
         */
        private String hangupAfterDtmfFail;
    }

    public static UnicomAxeFirstBindInfoVO buildUnicomBindInfoVO(String message){
        UnicomAxeFirstBindInfoVO unicomAxbBindInfoVO = new UnicomAxeFirstBindInfoVO();
        unicomAxbBindInfoVO.setCode(200);
        unicomAxbBindInfoVO.setProcessType("12");
        JSONObject jsonObject = JSONObject.parseObject(message);
        String data1 = jsonObject.getString("data");
        DataInfo dataInfo1 = JSONUtil.toBean(data1, DataInfo.class);
        dataInfo1.setDtmfLength("4");
        unicomAxbBindInfoVO.setData(dataInfo1);
        return unicomAxbBindInfoVO;
    }
}
