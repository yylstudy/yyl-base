package com.cqt.vccidhmyc.web.service;

import com.cqt.model.unicom.entity.PrivateCorpInteriorInfo;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-04-03 15:09
 */
public interface DataQueryService {

    /**
     * 根据漫游号查询imsi
     * @param roamingCalledNum 漫游号
     * @return imsi
     */
    Optional<String> getImsi(String roamingCalledNum);

    /**
     * 查询imsi对应的x号码
     * @param imsi imsi
     * @return x号码
     */
    String getSecretNoByImsi(String imsi);

    /**
     * 根据x号码查询归属企业id
     * @param secretNo x号码
     * @return 企业id
     */
    String getVccIdBySecretNo(String secretNo);

    /**
     * 查询企业Limit
     * @param vccId 企业id
     * @return Limit
     */
    Integer getCallLimit(String vccId);

    /**
     * 查询企业内部配置
     * @param vccId 企业id
     * @return 内部配置
     */
    Optional<PrivateCorpInteriorInfo> getCorpInteriorInfo(String vccId);
}
