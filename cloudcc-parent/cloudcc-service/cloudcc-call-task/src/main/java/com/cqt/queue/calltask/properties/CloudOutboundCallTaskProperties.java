package com.cqt.queue.calltask.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-10-26 10:38
 */
@Data
@Component
@ConfigurationProperties(prefix = "cloudcc.call-task")
public class CloudOutboundCallTaskProperties {

    private TaskExecutor executor = new TaskExecutor();

    public Integer getPoolSize() {
        return this.getExecutor().getPoolSize();
    }

    public Integer getQueueCapacity() {
        return this.getExecutor().getQueueCapacity();
    }

    @Data
    private static class TaskExecutor {

        private Integer poolSize = 20;

        private Integer queueCapacity = 300;
    }
}
