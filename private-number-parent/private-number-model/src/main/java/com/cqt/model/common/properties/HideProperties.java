package com.cqt.model.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2021/9/9 16:12
 */
@Data
@Component
@ConfigurationProperties(prefix = "hide")
public class HideProperties {

    /**
     * 放音编码与文件名对应关系
     */
    private String audioCodeDataId;

    /**
     * 请输入分机号
     */
    private String digitsIvr;

    /**
     * 找不到绑定关系ivr
     */
    private String notBindIvr;

    /**
     * 最大通话时长 s
     */
    private Integer maxDuration = 7200;

    /**
     * redis 绑定关系key默认超时时间 s 4小时 14400
     */
    private Integer defaultExpiration = 14400;

    /**
     * 配置的服务器ip
     */
    private List<String> serverIps;

    /**
     * 号码池同步url;
     */
    private String syncNumberUrl;

    /**
     * 企业业务配置同步url
     */
    private String syncCorpInfoUrl;

    /**
     * 拨测脚本命令
     */
    private String dialTestLuaCmd;

    /**
     * 拨测放音文件名
     */
    private String dialTestVoice;

    private String dialTestUrl;

    /**
     * 企业id
     */
    private String vccId;

    /**
     * 地市编码-机房对应关系json
     */
    private String areaLocationDataId = "private-audio-code.json";

    /**
     * 号码分配 地市按供应商权重配置信息
     * private_corp_area_distribution_strategy.json
     */
    private String supplierDistributionStrategyDataId = "private_corp_area_distribution_strategy.json";

    /**
     * 当前机房
     */
    private String currentLocation;

    /**
     * 回收延时增加10秒
     */
    private Integer delayTimeout = 10;

    /**
     * requestId重复 等待时间 10毫秒
     */
    private Integer requestTimeout = 20;

    /**
     * requestId重复, 最大等待时间 ms
     */
    private Integer maxRequestTimeout = 500;

    /**
     * redis scan命令 批量数量
     */
    private Integer scanLimit = 50000;

    /**
     * 线程池 最大阻塞队列大小
     */
    private Integer maxQueueSize = 500000;

    /**
     * 回收axb可用号码周期
     */
    private Integer offsetDay;

    /**
     * X号码回收延时队列, 按小时分队列
     */
    private List<Integer> deadQueueIndexList;

    /**
     * 按秒分队列
     */
    private List<Integer> deadQueueSecondIndexList;

    /**
     * 延时队列等级, s-每10s, h-每一小时, d-每天, 在范围内的推送死信队列,每个等级一个队列, 不在范围内推送到延时插件
     */
    private DelayLevel delayLevel;

    /**
     * jdk 延时队列 , 锁超时时间
     */
    private Integer setnxTimeout = 5;

    /**
     * 超时此时间 不推mq
     */
    private Integer longestExpiration = 259200;

    /**
     * 行业短信号码正则
     * ^(106.*)$
     */
    private String industrySmsNumberRegex;

    /**
     * 异地机房nacos ip:port
     */
    private String backNacos;

    /**
     * 未过期绑定关系表分表数量 index 从0开始
     * private_bind_info_axe_${vccId}_${index}
     */
    private Integer bindTableSharingMaxIndex = 4;

    /**
     * 未过期绑定关系表要分表业务模式
     * 正则 ex: AXE|AX..
     */
    private String bindTableSharingBusinessType = "AXE";

    /**
     * http请求超时时间
     */
    private Integer httpTimeout = 5000;

    /**
     * 随机取出x号码数量
     */
    private Integer randomPoolSize = 10;

    /**
     * 开关
     */
    private Switch switchs = new Switch();

    private DataId dataId = new DataId();

    /**
     * 超时时间配置
     */
    private Timeout timeout = new Timeout();

    /**
     * 个性化配置
     */
    private CustomConfig customConfig = new CustomConfig();

    /**
     * 来电黑名单检测
     * key: 业务类型, value: 是否开启黑名单检测
     */
    private Map<String, Boolean> callerBlacklistChecker = new HashMap<>();

    @Data
    public static class DelayLevel {

        /**
         * 超过这个时间的延时消息不推送mq
         */
        private Duration delayThreshold;

        private Duration minSecond = Duration.ofSeconds(10);

        private Duration maxSecond = Duration.ofSeconds(120);

        private Duration minHour = Duration.ofHours(1);

        private Duration maxHour = Duration.ofHours(25);

        private Duration minDay;

        private Duration maxDay;
    }

    @Data
    public static class Switch {

        /**
         * 新的任务
         */
        private Boolean scanDbNewFlag = true;

        private Boolean aybUnBindPush = false;

        /**
         * hmyc 隐藏服务, AOP 是否开启鉴权
         */
        private Boolean authFlag = false;

        /**
         * 为true 绑定关系 新增,修改,删除 当前线程池异步处理
         * 为false 先发送mq, 再消费入库
         */
        private Boolean bindInfoAsyncSaveDb = false;

        /**
         * 绑定关系是否保存入库
         */
        private Boolean saveDb = true;

        /**
         * 项目启动是否初始化分机号
         */
        private Boolean initExtFlag = false;

        /**
         * 是否检测AXE绑定 requestId幂等性
         */
        private Boolean checkAxeRequestId = true;

        /**
         * Axe绑定统计查询缓存
         */
        private Boolean axeBindStatsFromCache = true;

        /**
         * 自定义axe超时时间
         */
        private Boolean axeCustomExpire = true;

        /**
         * 解绑redis不存在, 查询数据库
         */
        private Boolean unbindQueryDb = false;
    }

    @Data
    public static class DataId {

        /**
         * 放音编码与文件名对应关系
         */
        private String audioCodeDataId;

        /**
         * 地市编码-机房对应关系json
         */
        private String areaLocationDataId;
    }

    @Data
    public static class Timeout {

        /**
         * axe绑定超时时间
         */
        private Duration axeBindExpire = Duration.ofDays(14);
    }

    @Data
    public static class CustomConfig {

        /**
         * axe 放音字段 同步美团接口
         */
        private String axeAudioVccId = "3155";
    }
}
