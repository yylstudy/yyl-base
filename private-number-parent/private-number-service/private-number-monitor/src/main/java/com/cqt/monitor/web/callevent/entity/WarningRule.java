package com.cqt.monitor.web.callevent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 告警规则表
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WarningRule {

    /**
     * id
     */
    private String id;

    /**
     * 关联warning_config表id
     */
    private String configId;

    /**
     * 基础数据 0-话单量，1-呼叫量，2-绑定数量，3-绑定失败率，4-振铃率，5-振铃失败率，6-接通率，7-未接通率
     */
    private Integer basicData;

    /**
     * 统计周期
     */
    private Integer countCycle;

    /**
     * 周期条件 1-连续1周期，2-连续2周期，3-连续3周期
     */
    private Integer cycleCondition;

    /**
     * 周期计算 0-总计，1-不计算
     */
    private Integer cycleCount;

    /**
     * 条件
     */
    private String compareCondition;

    /**
     * 阈值
     */
    private String threshold;


}
