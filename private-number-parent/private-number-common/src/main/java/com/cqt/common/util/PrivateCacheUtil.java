package com.cqt.common.util;

import cn.hutool.core.text.StrFormatter;
import com.cqt.common.constants.PrivateCacheConstant;

/**
 * @author linshiqiang
 * @since 2022/2/15 14:40
 * 缓存key 格式化
 */
public class PrivateCacheUtil {

    /**
     * AXE 分机号绑定统计-按地市
     * {vcc_id}:axeStats:{area_code}
     */
    public static String getAxeBindStatsKey(String vccId, String areaCode) {

        return StrFormatter.format(PrivateCacheConstant.AXE_EXT_BIND_STATS, vccId, areaCode);
    }

    /**
     * bindIdKey: {vcc_id}:{type}:bind:{bind_id}
     * string
     */
    public static String getBindIdKey(String vccId, String type, String bindId) {

        return String.format(PrivateCacheConstant.BIND_ID_KEY, vccId, type.toLowerCase(), bindId);
    }

    /**
     * requestIdKey: {vcc_id}:{type}:request:{request_id}
     * string
     */
    public static String getRequestIdKey(String vccId, String type, String requestId) {

        return String.format(PrivateCacheConstant.REQUEST_ID_KEY, vccId, type.toLowerCase(), requestId);
    }

    /**
     * fs bindInfoKey: {vcc_id}:{type}:bind:{tel_a/tel_b}:{tel_x}
     * string
     */
    public static String getBindInfoKey(String vccId, String type, String tel, String telX) {

        return StrFormatter.format(PrivateCacheConstant.BIND_INFO_KEY, vccId, type.toLowerCase(), tel, telX);
    }

    /**
     * X号码和分机号bindInfoKey: {vcc_id}:{type}:bind:{tel_x}:{tel_x_ext}
     * string
     */
    public static String getExtBindInfoKey(String vccId, String type, String telX, String extNum) {

        return StrFormatter.format(PrivateCacheConstant.BIND_INFO_KEY, vccId, type.toLowerCase(), telX, extNum);
    }

    /**
     * setnx X号码和分机号bindInfoKey: {vcc_id}:uniqueExt:{area_code}:{tel_x}:{tel_x_ext}
     * string
     */
    public static String getUniqueExtBindKey(String vccId, String areaCode, String telX, String extNum) {
        return StrFormatter.format(PrivateCacheConstant.UNIQUE_EXT_BIND_KEY, vccId, areaCode, telX, extNum);
    }

    /**
     * AX fs bindInfoKey: {vcc_id}:{type}:bind:{area_code}:{tel_x}
     */
    public static String getTelxBindInfoKey(String vccId, String type, String telX) {

        return String.format(PrivateCacheConstant.X_BIND_INFO_KEY, vccId, type.toLowerCase(), telX).intern();
    }

    /**
     * AX/BX可用号码池初始化标识: {vcc_id}:{type}:pool:init:{area_code}:{tel_a/tel_b}
     * string
     */
    public static String getInitFlagKey(String vccId, String type, String areaCode, String tel) {

        return String.format(PrivateCacheConstant.INIT_FLAG_KEY, vccId, type.toLowerCase(), areaCode, tel);
    }

    /**
     * AX/BX可用号码池: {vcc_id}:{type}:pool:{area_code}:{tel_a/tel_b}
     * set
     */
    public static String getUsablePoolKey(String vccId, String type, String areaCode, String tel) {

        return String.format(PrivateCacheConstant.USABLE_X_POOL_KEY, vccId, type.toLowerCase(), areaCode, tel);
    }

    public static String getUsablePoolSlotKey(String vccId, String type, String areaCode, String tel) {

        return String.format(PrivateCacheConstant.USABLE_X_POOL_SLOT_KEY, vccId, type.toLowerCase(), areaCode, tel);
    }

    /**
     * 用户号码 已使用X号码池子  {vcc_id}:{type}:pool:{area_code}:{tel_a/tel_b}
     */
    public static String getUsedPoolSlotKey(String vccId, String type, String areaCode, String tel) {

        return String.format(PrivateCacheConstant.USABLE_X_POOL_SLOT_KEY, vccId, type.toLowerCase(), areaCode, tel);
    }

    /**
     * axbn {{vcc_id}:{type}:pool:{area_code}}
     */
    public static String getPoolSlotKey(String vccId, String type, String areaCode) {

        return String.format(PrivateCacheConstant.USABLE_AXBN_POOL_SLOT_KEY, vccId, type.toLowerCase(), areaCode);
    }

    /**
     * X号码的未使用分机号池: {vcc_id}:{type}:pool:ext:{tel_x}
     * set
     */
    public static String getUsableExtPoolKey(String vccId, String type, String telX) {

        return String.format(PrivateCacheConstant.USABLE_EXT_POOL_KEY, vccId, type.toLowerCase(), telX).intern();
    }

    /**
     * 未使用X号码 list
     * X号码的未使用分机号池: {vcc_id}:{type}:pool:ext:{area_code}:{tel_x}
     */
    public static String getUsableExtPoolListKey(String vccId, String type, String areaCode, String telX) {

        return StrFormatter.format(PrivateCacheConstant.USABLE_EXT_POOL_LIST_KEY, vccId, type.toLowerCase(), areaCode, telX).intern();
    }

    /**
     * redisson分布式锁hash:
     * axb ax池初始化: {vcc_id}:axb_lock_init_ax:{tel_a}
     * hash
     */
    public static String getLockInitAxKey(String vccId, String type, String tel) {

        return String.format(PrivateCacheConstant.AXB_LOCK_INIT_AX_KEY, vccId, type.toLowerCase(), tel);
    }

    /**
     * axb bx池初始化: {vcc_id}:{type}_lock_init_ax:{tel_b}
     * hash
     */
    public static String getLockInitBxKey(String vccId, String type, String tel) {

        return String.format(PrivateCacheConstant.AXB_LOCK_INIT_BX_KEY, vccId, type.toLowerCase(), tel);
    }

    /**
     * 企业信息 private:vcc:info:{vcc_id}
     */
    public static String geVccInfoKey(String vccId) {

        return String.format(PrivateCacheConstant.CORP_BUSINESS_INFO, vccId).intern();
    }

    /**
     * 内部配置实体json
     * corp_interior:{VCCD}:{900007}
     */
    public static String getCorpInteriorInfoKey(String vccId, String serviceKey) {
        return String.format(PrivateCacheConstant.CORP_INTERIOR_INFO, vccId, serviceKey).intern();
    }

    public static String getKey(String vccId, String areaCode) {

        return vccId + ":" + areaCode;
    }

    /**
     * AX 可用X号码池: {vcc_id}:{type}:pool:{area_code}
     * set
     */
    public static String getAxUsablePoolKey(String vccId, String type, String areaCode) {

        return String.format(PrivateCacheConstant.AX_USABLE_POOL_KEY, vccId, type.toLowerCase(), areaCode).intern();
    }

    /**
     * 106初始化
     */
    public static String getIndustryInitLockKey(String tel) {
        return String.format(PrivateCacheConstant.LOCK_INIT_INDUSTRY_USABLE_POOL_KEY, tel);
    }

    /**
     * 106号码初始化标识
     * mt:pool:106:axb:{area_code}:{A/B}
     */
    public static String getIndustrySmsTelInitKey(String vccId, String areaCode, String tel) {
        return String.format(PrivateCacheConstant.INDUSTRY_SMS_TEL_INIT_KEY, vccId, areaCode, tel);
    }

    /**
     * 106号码可用池
     * mt:pool:106:axb:pool:{area_code}:{A/B}
     */
    public static String getIndustrySmsTelUsablePoolKey(String vccId, String areaCode, String tel) {
        return String.format(PrivateCacheConstant.INDUSTRY_SMS_TEL_USABLE_POOL_KEY, vccId, areaCode, tel);
    }

    /**
     * 获取第三方url key
     */
    public static String getThirdSupplierUrlKey(String supplierId, String urlType) {
        return String.format(PrivateCacheConstant.THIRD_SUPPLIER_URL_KEY, supplierId, urlType);
    }

    /**
     * 获取异常统计数key
     */
    public static String getThirdSupplierExceptionCountKey(String supplierId) {
        return String.format(PrivateCacheConstant.THIRD_SUPPLIER_EXCEPTION_KEY, supplierId);
    }

    /**
     * 供应商信息 string
     * key 供应商id
     * value: json
     */
    public static String getSupplierInfoKey(String supplierId) {
        return String.format(PrivateCacheConstant.PRIVATE_SUPPLIER_INFO_KEY, supplierId);
    }

    /**
     * 第三方绑定关系key
     */
    public static String getThirdBindMapperKey(String vccid, String bindId) {
        return String.format(PrivateCacheConstant.THIRD_CQTBIND_MAPPER, vccid, bindId);
    }

    /**
     * 第三方绑定关系key
     */
    public static String getBindMapperKey(String supplierId, String bindId) {
        return String.format(PrivateCacheConstant.THIRD_BIND_MAPPER, supplierId, bindId);
    }

    public static String getBindMapperKey(String bindId) {
        return String.format(PrivateCacheConstant.THIRD_BIND_ID_INFO, bindId);
    }

    /**
     * x号码所属vccId string
     * key: msrn_{number}
     * value: vccId
     */
    public static String getVccIdByNumberKey(String number) {

        return String.format(PrivateCacheConstant.X_NUMBER_BELONG_VCC_ID_KEY, number).intern();
    }

    /**
     * 中间号监控, 修改sbc配置文件加锁
     * private:updateSbcDisMonitor:{sbc_ip}
     */
    public static String getUpdateSbcDisMonitorLockKey(String scbIp) {

        return String.format(PrivateCacheConstant.UPDATE_SBC_DIS_MONITOR_LOCK_KEY, scbIp);
    }

    /**
     * 异常的节点 dis组权重为0 断开连接
     * string
     * private:dis:disconnectNode:{SN_IP}
     */
    public static String getDisConnectNodeKey(String currentIp) {

        return String.format(PrivateCacheConstant.DISCONNECT_NODE_KEY, currentIp);
    }

    /**
     * h码
     * h_1310762
     */
    public static String getAreaCodeOfTelCodeKey(String telCode) {

        return String.format(PrivateCacheConstant.TEL_CODE_OF_AREA_CODE_KEY, telCode);
    }

    public static String getBroadNetCallBillRetryCountKey(String type, String vccId, String recordId) {
        return String.format(PrivateCacheConstant.BROAD_NET_CALL_BILL_RETRY_COUNT_KEY, type, vccId, recordId);
    }

    public static String getBroadNetCallIdWithBindIdKey(String callId) {
        return String.format(PrivateCacheConstant.BROAD_NET_CALL_ID_WITH_BIND_ID_KEY, callId);
    }

    /**
     * 企业并发控制
     * bus:call:limit:[vccid]:info
     */
    public static String getCorpCallLimitInfoKey(String vccId) {

        return String.format(PrivateCacheConstant.BUS_CALL_LIMIT_INFO, vccId);
    }

    public static String getThirdRecordUrlKey(String callId) {

        return String.format(PrivateCacheConstant.THIRD_RECORD_URL, callId);
    }

    /**
     * 平台号码
     * private:numberInfo:{num}
     */
    public static String getNumberInfo(String num) {

        return String.format(PrivateCacheConstant.NUM_INFO, num);
    }


    public static String getRecordUrlKey(String callId) {

        return String.format(PrivateCacheConstant.RECORD_URL, callId);
    }

    /**
     * key: private:blacklist:caller:{vcc_id}:{business_type}:{caller_number}:{callee_number}
     * value：1
     */
    public static String getCallerBlacklistKey(String vccId, String businessType, String callerNumber, String calleeNumber) {
        return StrFormatter.format(PrivateCacheConstant.CALLER_BLACKLIST_KEY, vccId, businessType.toLowerCase(), callerNumber, calleeNumber);
    }

}
