package com.cqt.model.monitor.dto;

import lombok.Data;

/**
 * @author linshiqiang
 * @date 2022/1/24 16:05
 */
@Data
public class CommonBindHealthCheckDTO {

    /**
     * 主业务
     */
    private String business;

    /**
     * 绑定业务
     */
    private String businessBind;

    /**
     * A机房 rabbitmq 集群名称
     */
    private String bindRabbitmqA;

    /**
     * B机房 rabbitmq 集群名称
     */
    private String bindRabbitmqB;

    /**
     * A机房 redis 集群名称
     */
    private String bindRedisA;

    /**
     * B机房 redis 集群名称
     */
    private String bindRedisB;

    /**
     * A机房 nacos 集群名称
     */
    private String nacosA;

    /**
     * B机房 nacos 集群名称
     */
    private String nacosB;
}
