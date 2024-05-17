package com.cqt.model.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @date 2022/2/14 9:39
 */
@Data
@Component
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMqProperties {

    private String nameServer;

    private Consumer consumer;

    @Data
    public static class Consumer {

        private String topic;

        private String group;

        private String tag;

        private Integer pullBatchSize;

        private Integer consumeMessageBatchMaxSize;

        private Integer consumeThreadMin;

        private Integer consumeThreadMax;
    }
}
