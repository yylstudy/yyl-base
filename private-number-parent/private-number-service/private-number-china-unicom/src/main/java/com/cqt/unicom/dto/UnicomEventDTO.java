package com.cqt.unicom.dto;

import lombok.Data;

/**
 * @author huweizhong
 * date  2023/10/26 17:51
 * 联通状态
 */

public class UnicomEventDTO {
    /**
     * 呼出主叫
     */
    private String caller;

    /**
     * 呼出被叫
     */
    private String callee;

    /**
     *  呼入主叫
     */
    private String incaller;

    /**
     * 业务类型：voice，sms
     */
    private String serviceType;

    /**
     * 事件标识
     * 1：起始
     */
    private Integer flag;

    private Extention extention;

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

    public String getIncaller() {
        return incaller;
    }

    public void setIncaller(String incaller) {
        this.incaller = incaller;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public Extention getExtention() {
        return extention;
    }

    public void setExtention(Extention extention) {
        this.extention = extention;
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }


    public static class Extention{
        /**
         * 系统处理, flag 为 1 时必填
         * 0：正常
         * 1：逻辑关机
         * 2：无效而丢弃（例如：呼叫未设置被叫号
         * 码，平台无法接续）
         * 第 9页共 15页
         * 3：拦截丢弃
         * 12：收号查询呼叫(需要 dtmf 事件)
         */
        private Integer process;

        /**
         * 语音 call-id，系统处理丢弃(process=2 或 3)时不生成 id
         */
        private String id;

        /**
         * 客户平台指定 ID 标识，用于交互模式接口
         */
        private String uId;

        /**
         * 拉流地址，RTSP 播放器均可直接播放
         */
        private String mediaAddr;

        /**
         * 释放方向
         * 1：被叫释放
         * 2：平台释放
         */
        private Integer releaseDir;

        /**
         * 话务分析
         */
        private Integer releaseCause;

        /**
         * 录音文件提取方式
         * 开放企业客户推送录音，并且接通时
         */
        private String recordUrl;

        /**
         * 短信条数
         */
        private String smscnt;

        public Integer getProcess() {
            return process;
        }

        public void setProcess(Integer process) {
            this.process = process;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getuId() {
            return uId;
        }

        public void setuId(String uId) {
            this.uId = uId;
        }

        public String getMediaAddr() {
            return mediaAddr;
        }

        public void setMediaAddr(String mediaAddr) {
            this.mediaAddr = mediaAddr;
        }

        public Integer getReleaseDir() {
            return releaseDir;
        }

        public void setReleaseDir(Integer releaseDir) {
            this.releaseDir = releaseDir;
        }

        public Integer getReleaseCause() {
            return releaseCause;
        }

        public void setReleaseCause(Integer releaseCause) {
            this.releaseCause = releaseCause;
        }

        public String getRecordUrl() {
            return recordUrl;
        }

        public void setRecordUrl(String recordUrl) {
            this.recordUrl = recordUrl;
        }

        public String getSmscnt() {
            return smscnt;
        }

        public void setSmscnt(String smscnt) {
            this.smscnt = smscnt;
        }
    }

    /**
     * 时间,精确到毫秒
     * 格式:YYYY-MM-DD hh24:mm:ss.SSS
     */
    private String eventTime;
}
