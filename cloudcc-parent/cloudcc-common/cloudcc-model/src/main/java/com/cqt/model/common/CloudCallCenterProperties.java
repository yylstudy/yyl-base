package com.cqt.model.common;

import com.cqt.base.contants.SystemConstant;
import com.cqt.base.enums.RecordStartEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author linshiqiang
 * date:  2023-07-20 16:06
 * 自定义属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "cloudcc")
public class CloudCallCenterProperties {

    /**
     * 默认机房
     */
    private String defaultRoom = SystemConstant.LOCATION;

    /**
     * 排队轮训间隔ms
     */
    private Duration queuePollingScheduleTime = Duration.ofSeconds(1);

    /**
     * 排队redisson分布式锁等待时长s
     */
    private Duration queuePollingLockWaitTime = Duration.ofSeconds(5);

    /**
     * 排队redisson分布式锁续租时长s
     */
    private Duration queuePollingLockLeaseTime = Duration.ofSeconds(30);

    /**
     * 锁超时时间
     */
    private Duration lockTIme = Duration.ofSeconds(10);

    /**
     * 底层配置信息
     */
    private BASE base = new BASE();

    private DefaultConfig defaultConfig = new DefaultConfig();

    private AESSecretInfo secretInfo = new AESSecretInfo();

    private Boolean auth = true;

    /**
     * 是否保存分机状态迁移日志
     */
    private Boolean extStatusLog = true;

    /**
     * 是否执行排队任务
     */
    private Boolean startQueueTask = true;

    @Data
    public static class DefaultConfig {

        private String sdkServer = "http://{}:{}{}";

        /**
         * 录制文件后缀【mp3, wav, mp4】默认mp3
         */
        private String recordSuffix;

        /**
         * 话单数据库名称
         */
        private String cdrDbNames = "cdr";

        /**
         * 默认语言
         */
        private String language = "zh_CN";

        private Duration idleTime = Duration.ofMillis(3000);
    }

    @Data
    public static class BASE {

        private String baseUrl;

        private String serviceCode = "cloudcc";

        private String eventTopic = "cloudcc";

        private String eventGroup = "cloudcc_business";

        private String eventTag = "*";

        private String recordStart = RecordStartEnum.ANSWER.name();

        private Duration messageIdempotent = Duration.ofMinutes(3);

        private Duration transDelayHangup = Duration.ofMillis(1000);

        /**
         * 事件消息内时间戳早于当前时间60分钟不处理
         */
        private Duration messageTimeout = Duration.ofMinutes(60);

        private RocketMq rocketmq = new RocketMq();
    }

    @Data
    public static class AESSecretInfo {

        /**
         * aes 加密的key
         * */
        private String key = "VGSs88YCJ9HJfPkZ";

        /**
         * aes 加密的偏移量
         * */
        private String iv = "2HO21pQX3KLkLglb";
    }

    @Data
    public static class RocketMq {

        private String nameServer;

        private String accessKey;

        private String secretKey;

        private String topic = "cloudcc";

        private String group = "cloudcc_business";

        private String tag = "*";

        /**
         * Minimum consumer thread number
         */
        private int consumeThreadMin = 20;

        /**
         * Max consumer thread number
         */
        private int consumeThreadMax = 20;

        /**
         * Batch pull size
         */
        private int pullBatchSize = 10;

        /**
         * Batch consumption size
         */
        private int consumeMessageBatchMaxSize = 5;
    }
}
