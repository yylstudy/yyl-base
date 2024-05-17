package com.cqt.model.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author linshiqiang
 * @date 2022/1/21 15:41
 * 小号监控 异常日志
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("private_monitor_error_log")
public class PrivateMonitorErrorLog {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 状态
     */
    private Integer status;

    /**
     * rabbitmq, redis
     */
    private String type;

    /**
     * 机房位置 南京A, 扬州B
     */
    private String place;

    /**
     * 业务类型 美团mt
     */
    private String business;

    /**
     * 日志内容
     */
    private String log;

    /**
     * 创建时间
     */
    private Date createTime;

}
