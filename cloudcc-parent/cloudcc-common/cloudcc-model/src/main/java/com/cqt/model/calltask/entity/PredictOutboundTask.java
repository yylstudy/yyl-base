package com.cqt.model.calltask.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 预测外呼任务(PredictOutboundTask)表实体类
 *
 * @author linshiqiang
 * @since 2023-10-27 10:35:00
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("cloudcc_predict_outbound_task")
public class PredictOutboundTask implements BaseTaskInfo, Serializable {

    private static final long serialVersionUID = 1244438796861487232L;

    /**
     * 任务id
     */
    @TableId(type = IdType.INPUT)
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 租户id同企业编码
     */
    @JsonProperty("tenantId")
    @TableField("tenant_id")
    private String companyCode;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 等待音类型(默认音/默认视频和自选)
     */
    private Integer waitingToneType;

    /**
     * 等待音文件id
     */
    private String waitingTone;

    /**
     * 弹屏
     */
    private String popScreen;

    /**
     * 任务开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 任务结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 呼出时段 [{"beginTime":"00:00:00","endTime":"23:00:00" }]
     */
    private String callableTime;

    /**
     * 外显号码 号码数组json ["131xxx","132xxx"]
     */
    private String displayNumber;

    /**
     * 判重类型
     */
    private Integer duplicateType;

    /**
     * 判重时间（天）
     */
    private Integer duplicateTime;

    /**
     * 外呼频率（秒）
     */
    private Integer outboundFrequency;

    /**
     * 无人接听振铃时长（秒）
     */
    private Integer maxRingTime;

    /**
     * 外呼次数
     */
    private Integer maxAttemptCount;

    /**
     * 外呼比例
     */
    private Integer outboundRatio;

    /**
     * 重呼间隔（秒）
     */
    private Integer attemptInterval;

    /**
     * 任务状态 (1-草稿 2-暂停  3-启用  4-已结束)
     */
    private Integer taskState;

    /**
     * xxl-job对应的任务id
     */
    private Integer jobId;

}

