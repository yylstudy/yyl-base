package com.cqt.model.monitor.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author hlx
 * @date 2022-01-21
 */
@Data
@Component
@ConfigurationProperties(prefix = "monitor")
public class MonitorProperties {

    /**
     * 是否开启监控
     */
    private Boolean openMonitor;

    /**
     * msrn号码为 0 连续多少次时切换
     */
    private Integer msrnNum;

    /**
     * 是否自动恢复
     */
    private Boolean autoRecover;

    /**
     * mq接口超时时间ms
     */
    private Integer timeout;

    /**
     * mq检测类型  1 Java代码, 0 15672接口
     */
    private Integer mqCheckType;
    /**
     * 请求切换失败重试次数
     */
    private Integer retryNum;

    /**
     * 查看master进程号
     */
    private String switchMaster;

    /**
     * 查看slave进程号
     */
    private String switchSlave;
}
