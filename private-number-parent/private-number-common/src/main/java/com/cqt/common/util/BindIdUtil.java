package com.cqt.common.util;

import cn.hutool.core.util.*;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.ServiceCodeEnum;

import java.time.Instant;

/**
 * @author linshiqiang
 * @date 2021/9/12 11:21
 */
public class BindIdUtil {

    @Deprecated
    public static String getBindId(BusinessTypeEnum businessTypeEnum, String cityCode) {
        String idStr = cityCode + IdUtil.getSnowflake().nextIdStr() + Instant.now().toEpochMilli();
        return getId(businessTypeEnum, idStr);
    }

    /**
     * cqt- + 雪花id + 三位随机数 + fnvHash(supplier)
     *
     * @param businessTypeEnum 业务模式
     * @param cityCode         地市编码
     * @param supplierId       供应商id
     * @return 绑定id
     */
    @Deprecated
    public static String getBindId(BusinessTypeEnum businessTypeEnum, String cityCode, String supplierId) {
        int fnvHash = HashUtil.fnvHash(supplierId);
        String idStr = cityCode + IdUtil.getSnowflake().nextIdStr() + RandomUtil.randomNumbers(3) + fnvHash;
        return getId(businessTypeEnum, idStr);
    }

    /**
     * cqt- + 雪花id + fnvHash(telX) + 5位随机数 + fnvHash(supplier)
     * cqt-2205911628567514209312768108989078373251344793307
     * 53位
     *
     * @param businessTypeEnum 业务模式
     * @param cityCode         地市编码
     * @param supplierId       供应商id
     * @param telX             X号码
     * @return 绑定id
     */
    public static String getBindId(BusinessTypeEnum businessTypeEnum, String cityCode, String supplierId, String telX) {
        String supplierHash = getHash(supplierId);
        String xHash = getHash(telX);
        String idStr = cityCode + IdUtil.getSnowflake().nextIdStr() + xHash + RandomUtil.randomNumbers(5) + supplierHash;
        return getId(businessTypeEnum, idStr);
    }

    private static String getId(BusinessTypeEnum businessTypeEnum, String idStr) {
        switch (businessTypeEnum) {
            case AXB:
                return SystemConstant.BIND_ID_SUFFIX + ServiceCodeEnum.AXB.getCode() + idStr;
            case AXE:
                return SystemConstant.BIND_ID_SUFFIX + ServiceCodeEnum.AXE.getCode() + idStr;
            case AXEYB:
                return SystemConstant.BIND_ID_SUFFIX + ServiceCodeEnum.AXEYB_AXE.getCode() + idStr;
            case AYB:
                return SystemConstant.BIND_ID_SUFFIX + ServiceCodeEnum.AXEYB_AYB.getCode() + idStr;
            case AXEBN:
                return SystemConstant.BIND_ID_SUFFIX + ServiceCodeEnum.AXEBN.getCode() + idStr;
            case AX:
                return SystemConstant.BIND_ID_SUFFIX + ServiceCodeEnum.AX.getCode() + idStr;
            case AXYB:
                return SystemConstant.BIND_ID_SUFFIX + ServiceCodeEnum.AXYB.getCode() + idStr;
            case AXG:
                return SystemConstant.BIND_ID_SUFFIX + ServiceCodeEnum.AXG.getCode() + idStr;
            default:
                break;
        }
        return SystemConstant.BIND_ID_SUFFIX + idStr;
    }

    /**
     * 根据bindId 查询area_code
     * cqt-2205911628567514209312768108989078373251344793307
     * 53位
     *
     * @param bindId 绑定id cqt-2205911628567514209312768108989078373251344793307
     * @return area_code
     */
    public static String getAreaCodeByBindId(String bindId) {

        return StrUtil.sub(bindId, 6, -44);
    }

    /**
     * 根据bindId 查询X号码fnvHash
     */
    public static String getNumberHashByBindId(String bindId) {
        return StrUtil.sub(bindId, -25, -15);
    }

    /**
     * 截取供应商id的hash 后10位
     *
     * @param bindId 绑定id
     * @return 供应商id的hash
     */
    public static String getSupplierIdHash(String bindId) {

        return StrUtil.subSuf(bindId, -10);
    }

    /**
     * 获取hash 不足10位, 前面自动补0
     */
    public static String getHash(String value) {
        int hash = HashUtil.fnvHash(value);
        return String.format("%010d", hash);
    }

    public static Boolean isAreaCode(String areaCode) {

        return ReUtil.isMatch("0\\d{2,3}", areaCode);
    }

    public static String getAxbnRealTelId(String vccId, String telA, String telX) {

        return vccId + ":" + telA + ":" + telX;
    }

    /**
     * 绑定关系是否为第三方供应商
     *
     * @param supplierId 供应商id
     * @return 是否
     */
    public static Boolean isThirdSupplier(String supplierId) {

        return StrUtil.isNotEmpty(supplierId) && !GatewayConstant.LOCAL.equals(supplierId);
    }

    /**
     * AXB 绑定是否指定X号码 1是 0 否
     *
     * @param directTelX 是否指定X号码
     * @return 是否
     */
    public static Boolean isDirectTelX(Integer directTelX) {

        return ObjectUtil.isNotEmpty(directTelX) && directTelX == 1;
    }
}
