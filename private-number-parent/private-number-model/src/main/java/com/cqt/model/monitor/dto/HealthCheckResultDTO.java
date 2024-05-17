package com.cqt.model.monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2022/1/24 11:40
 * 中间件, 检测结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HealthCheckResultDTO {

    /**
     * A机房 rabbitmq 是否异常
     */
    private Boolean roomRabbitmqA;

    /**
     * B机房 rabbitmq 是否异常
     */
    private Boolean roomRabbitmqB;

    /**
     * A机房 redis 是否异常
     */
    private Boolean roomRedisA;

    /**
     * B机房 redis 是否异常
     */
    private Boolean roomRedisB;
}
