package com.cqt.model.sms.save;

/**
 * @author fat boy y
 */
public class SmsSdr {

    private String streamNumber;
    private String messageId;
    private String vccId;
    private String callerNumber;
    private String inNumber;
    private String outNumber;
    private String calledNumber;
    private String imsi;
    private String gsmCenter;
    private String inContent;
    private String failCode;
    private String failReason;
    private String requestTime;
    private String sendTime;
    private String remark;
    private String tableName;
    /**
     *短信条数
     */
    private Integer smsNumber;
    /**
     * 第三方供应商id
     */
    private  String supplierId;
    //绑定id
    private String bindId;
    //0:下行，1上行
    private String direction ;
    /**
     * 小号区号
     */
    private String areaCode;



    public SmsSdr() {
    }

    @Override
    public String toString() {
        return "SmsSdr{" + "streamNumber='" + streamNumber + '\'' + ", messageId='" + messageId + '\'' + ", vccId='" + vccId + '\'' + ", callerNumber='" + callerNumber + '\'' + ", inNumber='" + inNumber + '\'' + ", outNumber='" + outNumber + '\'' + ", calledNumber='" + calledNumber + '\'' + ", imsi='" + imsi + '\'' + ", gsmCenter='" + gsmCenter + '\'' + ", inContent='" + inContent + '\'' + ", failCode='" + failCode + '\'' + ", failReason='" + failReason + '\'' + ", requestTime='" + requestTime + '\'' + ", sendTime='" + sendTime + '\'' + ", remark='" + remark + '\'' + ", tableName='" + tableName + '\'' + '}';
    }

    public String getStreamNumber() {

        return streamNumber;
    }

    public void setStreamNumber(String streamNumber) {

        this.streamNumber = streamNumber;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getVccId() {
        return vccId;
    }

    public void setVccId(String vccId) {
        this.vccId = vccId;
    }

    public String getCallerNumber() {
        return callerNumber;
    }

    public void setCallerNumber(String callerNumber) {
        this.callerNumber = callerNumber;
    }

    public String getInNumber() {
        return inNumber;
    }

    public void setInNumber(String inNumber) {
        this.inNumber = inNumber;
    }

    public String getOutNumber() {
        return outNumber;
    }

    public void setOutNumber(String outNumber) {
        this.outNumber = outNumber;
    }

    public String getCalledNumber() {
        return calledNumber;
    }

    public void setCalledNumber(String calledNumber) {
        this.calledNumber = calledNumber;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getGsmCenter() {
        return gsmCenter;
    }

    public void setGsmCenter(String gsmCenter) {
        this.gsmCenter = gsmCenter;
    }

    public String getInContent() {
        return inContent;
    }

    public void setInContent(String inContent) {
        this.inContent = inContent;
    }

    public String getFailCode() {
        return failCode;
    }

    public void setFailCode(String failCode) {
        this.failCode = failCode;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getBindId() {
        return bindId;
    }

    public void setBindId(String bindId) {
        this.bindId = bindId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Integer getSmsNumber() {
        return smsNumber;
    }

    public void setSmsNumber(Integer smsNumber) {
        this.smsNumber = smsNumber;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }
}
