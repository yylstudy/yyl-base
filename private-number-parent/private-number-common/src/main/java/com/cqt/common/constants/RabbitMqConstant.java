package com.cqt.common.constants;

/**
 * @author linshiqiang
 * date:  2023-02-20 13:46
 * rabbitmq 队列交换机
 */
public interface RabbitMqConstant {

    /**
     * (通话话单)交换机名称
     **/

    String ICCP_CDR_SAVE_EXCHANGE = "iccp_cdr_save_exchange";

    /**
     * (通话话单)路由键
     **/
    String ICCP_CDR_SAVE_ROUTE_KEY = "iccp_cdr_save_routing";

    /**
     * (短信话单)交换机名称
     **/
    String ICCP_SMS_CDR_SAVE_EXCHANGE = "iccp_sms_sdr_exchange";

    /**
     * (短信话单)路由键
     **/
    String ICCP_SMS_CDR_SAVE_ROUTE_KEY = "iccp_sms_sdr_routing";
}
