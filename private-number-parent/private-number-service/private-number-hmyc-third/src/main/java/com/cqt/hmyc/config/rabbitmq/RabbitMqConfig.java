package com.cqt.hmyc.config.rabbitmq;

import org.springframework.context.annotation.Configuration;

/**
 * @author linshiqiang
 * date 2021/9/12 14:35
 */
@Configuration
public class RabbitMqConfig {

    public static final String BIND_RECYCLE_EXCHANGE = "private-%s-bind-recycle-exchange";

    public static final String BIND_RECYCLE_DELAY_QUEUE = "private-bind-recycle-%s-delay-queue";

    /**
     * 绑定关系db新增队列
     */
    public static final String BIND_DB_INSERT_QUEUE = "private-bind-db-insert-queue";

    /**
     * 绑定关系db修改队列
     */
    public static final String BIND_DB_UPDATE_QUEUE = "private-bind-db-update-queue";

    /**
     * 绑定关系db删除队列
     */
    public static final String BIND_DB_DELETE_QUEUE = "private-bind-db-delete-queue";

    /**
     * 绑定关系db操作
     */
    public static final String BIND_DB_EXCHANGE = "private-bind-db-exchange";
}
