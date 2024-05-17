package com.cqt.model.calltask.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 预测外呼任务-号码(PredictOutboundTaskNumber)表实体类
 *
 * @author linshiqiang
 * @since 2023-10-27 10:35:00
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("cloudcc_predict_outbound_task_number")
public class PredictOutboundTaskNumber {

    /**
     * 号码id
     */
    @TableId(type = IdType.INPUT)
    private String numberId;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 租户id同企业编码
     */
    private String tenantId;

    /**
     * 外呼号码
     */
    private String number;

    /**
     * 呼叫状态 1- 已呼叫 0-未呼叫
     */
    private Integer callStatus;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 接通状态 1-已接通 0 -未接通
     */
    private Integer answerStatus;

    /**
     * 呼叫次数
     */
    private Integer callCount;

    /**
     * 发起呼叫时间
     */
    private Date callTime;

}

