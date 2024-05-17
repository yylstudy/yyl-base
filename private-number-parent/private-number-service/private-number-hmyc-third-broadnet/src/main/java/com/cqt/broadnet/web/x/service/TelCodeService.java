package com.cqt.broadnet.web.x.service;

/**
 * @author linshiqiang
 * date:  2023-02-17 9:52
 */
public interface TelCodeService {

    /**
     * 根据手机前7位查询归属地市
     *
     * @param telCode 手机前7位
     * @return 归属地市
     */
    String getAreaCode(String telCode);

    /**
     * 获取计费类型
     * 市话长途，B和x判断
     *
     * @param aNumber a号码
     * @param bNumber b号码
     * @return 计费类型
     */
    String getChargeType(String aNumber, String bNumber);
}
