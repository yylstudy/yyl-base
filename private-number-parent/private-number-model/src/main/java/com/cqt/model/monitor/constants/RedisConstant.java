package com.cqt.model.monitor.constants;

public interface RedisConstant {

    /**
     * 生效颗粒度：企业
     * 统计周期次数(扬州)不总计
     */
    String CORPTOTALCOUNTYZ = "corp_total_count:yz:%s:%s:%s";

    /**
     * 生效颗粒度：企业
     * 统计周期次数(南京)不总计
     */
    String CORPTOTALCOUNTNJ = "corp_total_count:nj:%s:%s:%s";


    /**
     * 生效颗粒度：企业
     * 总计时累加基础数据的值(扬州)
     */
    String CORPTOTALCOUNTVALUEYZ = "corp_total_count:value:yz:%s:%s:%s";

    /**
     * 生效颗粒度：企业
     * 总计时累加基础数据的值（南京）
     */
    String CORPTOTALCOUNTVALUENJ = "corp_total_count:value:nj:%s:%s:%s";

    /**
     * 生效颗粒度：企业号码
     * 统计周期次数(扬州)不总计
     */
    String CORPTOTALCOUNTNUMYZ = "corp_total_count:yz:%s:%s:%s:%s";

    /**
     * 生效颗粒度：企业号码
     * 统计周期次数(南京)不总计
     */
    String CORPTOTALCOUNTNUMNJ = "corp_total_count:nj:%s:%s:%s:%s";


    /**
     * 生效颗粒度：企业号码
     * 总计时累加基础数据的值(扬州)
     */
    String CORPTOTALCOUNTVALUENUMYZ = "corp_total_count:value:yz:%s:%s:%s:%s";

    /**
     * 生效颗粒度：企业号码
     * 总计时累加基础数据的值（南京）
     */
    String CORPTOTALCOUNTVALUENUMNJ = "corp_total_count:value:yz:nj:%s:%s:%s:%s";

    /**
     * 南京平台生效颗粒度为企业
     * Granularity_in_corpNJ:{vccid}:{areaCode}:{时间}:{供应商id}
     */
    String GRANULARITY_IN_CORPNJ = "Granularity_in_corpNJ:%s:%s:%s:%s";

    /**
     * 扬州平台生效颗粒度为企业
     * Granularity_in_corpYZ:{vccid}:{areaCode}:{时间}:{供应商id}
     */
    String GRANULARITY_IN_CORPYZ = "Granularity_in_corpYZ:%s:%s:%s:%s";

    /**
     * 扬州平台生效颗粒度为号码
     * Granularity_in_numberYZ:{vccid}:{areaCode}:{号码}：{时间}:{供应商id}
     */
    String GRANULARITY_IN_NUMYZ = "Granularity_in_numberYZ:%s:%s:%s:%s:%s";

    /**
     * 南京平台生效颗粒度为号码
     * Granularity_in_numberNJ:{vccid}:{areaCode}:{号码}：{时间}:{供应商id}
     */
    String GRANULARITY_IN_NUMNJ = "Granularity_in_numberNJ:%s:%s:%s:%s:%s";

    /**
     * "Number_Corp:{vccid}:{areaCode}"
     */
    String NUMBER_CORP = "warn_number_corp:%s:%s";

    String WARN_CONFIG = "warn_config_";

    String JOB_ID = "Job_Id_";

    String WARN_RULE = "warn_Rules_";

    /**
     * Set集合 存储供应商id
     */
    String SUPPLIER_INFO = "supplier_id";

    /**
     * private:numberInfo:{号码}
     * val：平台号码实体
     */
    String PRIVATE_NUMBER_INFO = "private:numberInfo:%s";


}
