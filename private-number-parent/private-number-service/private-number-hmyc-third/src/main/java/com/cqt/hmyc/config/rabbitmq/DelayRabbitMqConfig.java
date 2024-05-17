package com.cqt.hmyc.config.rabbitmq;

import org.springframework.context.annotation.Configuration;

/**
 * @author linshiqiang@linkcircle.com
 * Date: 2021-11-27 21:51
 * Description:
 */
@Configuration
public class DelayRabbitMqConfig {

    public static final String RECYCLE_EXCHANGE_DELAY_H = "private_number_bind_recycle_exchange_delay_%sh";

    public static final String RECYCLE_QUEUE_DELAY_H = "private_number_bind_recycle_queue_delay_%sh";

    /**
     * 按秒s
     */
    public static final String RECYCLE_EXCHANGE_DELAY_S = "private_number_bind_recycle_exchange_delay_%ss";

    public static final String RECYCLE_QUEUE_DELAY_S = "private_number_bind_recycle_queue_delay_%ss";


}
