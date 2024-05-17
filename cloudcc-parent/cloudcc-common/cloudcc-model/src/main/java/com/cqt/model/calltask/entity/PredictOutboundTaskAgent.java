package com.cqt.model.calltask.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 预测外呼任务-坐席(PredictOutboundTaskAgent)表实体类
 *
 * @author linshiqiang
 * @since 2023-10-27 13:54:04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("cloudcc_predict_outbound_task_agent")
public class PredictOutboundTaskAgent {

    /**
     * 主键
     */
    private String id;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 租户id同企业编码
     */
    private String tenantId;

    /**
     * 坐席id
     */
    private String agentId;

    /**
     * 启动任务
     */
    private Integer startTaskFlag;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 更新人
     */
    private String updateBy;


}

