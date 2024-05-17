package com.cqt.monitor.common.constant;

public interface Constant {

    /**
     * 表 warning_config的数据，以set存入redis的key
     */
    String WARNINGCONFIG = "warning_config_all_set";

    /**
     * 表warning_rule的数据在redis中的key warning_rule:{warncofig.getId}
     */
    String WARNINGRULE = "warning_rule:%s";
    /**
     * 告警配置的告警规则计数器，有一条规则到达阈值计数器+1
     */
    String COUNTFORRULE = "count_for_rule:%s";

    /**
     * 话务事件key cdr_one_min_before:{currentTime-1}
     */

    /**
     * 扬州平台话单
     */
    String CDRONEMINBEFOREYZ = "cdr_one_min_before_yz:%s";
    /**
     * 南京平台话单
     */
    String CDRONEMINBEFORENJ = "cdr_one_min_before_nj:%s";


    /**
     * 周期计算为不计算时，redis中统计周期次数的key no_count_key:{warnRule.getId()}
     */
    String NOCOUNTKEY = "no_count_key:%s";

    /**
     * 周期计算为总计时，计算周期次数key
     */
    String TOTALCOUNTKEY = "total_count_key:%s";
    /**
     * 周期计算为总计时，存储阈值之和的key
     */
    String ALLTHRESHOLDCOUNT = "all_threshold_count_key:%s";
}
