package com.linkcircle.mq.common;

import lombok.Data;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description rabbitmq投递地址
 * @createTime 2024/5/6 14:32
 */
@Data
public class RabbitDestination {
    /**
     * 交换器
     */
    private String exchange;
    /**
     * 路由键
     */
    private String routingKey;
}
