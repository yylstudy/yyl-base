package com.cqt.model.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author linshiqiang
 * @date 2022/1/21 15:41
 * 小号监控 连接信息
 */
@Data
@TableName("private_monitor_info")
public class PrivateMonitorInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String url;

    private String ip;

    private Integer port;

    private String password;

    private String username;

    /**
     * 集群名称
     */
    @TableField("cluster_name")
    private String clusterName;

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

    private String areaLocationTable;

    /**
     * nacos命名空间
     */
    private String namespace;

    /**
     * nacos组
     */
    @TableField("group_id")
    private String groupId;

    /**
     * nacos 配置文件名
     */
    @TableField("data_id")
    private String dataId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 是否发生灾备切换
     */
    private Integer toggle;

}
