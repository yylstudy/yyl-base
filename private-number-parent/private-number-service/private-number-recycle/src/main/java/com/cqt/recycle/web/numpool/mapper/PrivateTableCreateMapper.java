package com.cqt.recycle.web.numpool.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author linshiqiang
 * @date 2022/8/31 17:24
 * 表结构mapper
 */
@Mapper
public interface PrivateTableCreateMapper {

    /**
     * AXB 绑定关系表
     *
     * @param vccId 企业id
     */
    void createBindAxb(String vccId);

    /**
     * AXB 初始化号码表
     *
     * @param vccId 企业id
     */
    void createBindAxbInit(String vccId);

    /**
     * AXE 绑定关系表
     *
     * @param vccId 企业id
     */
    void createBindAxe(String vccId);

    /**
     * AXE 绑定关系表 按X号码分表
     *
     * @param vccId 企业id
     * @param index 分表索引
     */
    void createBindAxeSharding(@Param("vccId") String vccId, @Param("index") Integer index);

    /**
     * AXYB 绑定关系表
     *
     * @param vccId 企业id
     */
    void createBindAxyb(String vccId);

    /**
     * AXG 绑定关系表
     *
     * @param vccId 企业id
     */
    void createBindAxg(String vccId);

    /**
     * AX 绑定关系表
     *
     * @param vccId 企业id
     */
    void createBindAx(String vccId);

    /**
     * AXEBN 绑定关系表
     *
     * @param vccId 企业id
     */
    void createBindAxebn(String vccId);

    /**
     * AXBN 绑定关系表
     *
     * @param vccId 企业id
     */
    void createBindAxbn(String vccId);

    /**
     * AXBN 真是号码表
     *
     * @param vccId 企业id
     */
    void createBindAxbnRealTel(String vccId);

    /**
     * AXBN 绑定历史表
     *
     * @param date 日期 20220831
     */
    void createTableOfBindInfoAxbnHis(@Param("date") String date);

    /**
     * AXB 绑定历史表
     *
     * @param date 日期 20220831
     */
    void createTableOfBindInfoAxbHis(@Param("date") String date);

    /**
     * AXG 绑定历史表
     *
     * @param date 日期 20220831
     */
    void createTableOfBindInfoAxgHis(@Param("date") String date);

    /**
     * AXYB 绑定历史表
     *
     * @param date 日期 20220831
     */
    void createTableOfBindInfoAxybHis(@Param("date") String date);

    /**
     * AX 绑定历史表
     *
     * @param date 日期 20220831
     */
    void createTableOfBindInfoAxHis(@Param("date") String date);

    /**
     * AXE 绑定历史表
     *
     * @param date 日期 20220831
     */
    void createTableOfBindInfoAxeHis(@Param("date") String date);

    /**
     * AXEBN 绑定历史表
     *
     * @param date 日期 20220831
     */
    void createTableOfBindInfoAxebnbHis(@Param("date") String date);


    /**
     * 创建通话事件统计表
     *
     * @param date 日期 20220831
     */
    void createTableOfCallEventStats(String date);

    /**
     * 并发统计表
     *
     * @param month 月份
     */
    void createTableOfCorpConcurrencyInfo(String month);

    /**
     * 创建短信记录表
     *
     * @param vccId 企业id
     * @param month 月份
     */
    void createSmsTable(String vccId, String month);

    /**
     * 企业每小时绑定次数统计
     *
     * @param month 月份
     */
    void createTableOfCorpBindCountStats(String month);
}
