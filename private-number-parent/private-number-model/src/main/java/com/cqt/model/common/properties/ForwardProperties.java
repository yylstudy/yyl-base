package com.cqt.model.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @since 2021/9/16 15:06
 */
@Data
@Component
@ConfigurationProperties(prefix = "forward")
public class ForwardProperties {

    private String corpBusinessInfoDataId = "private-number-corp-business-info.json";

    /**
     * 地市编码-机房对应关系json
     */
    private String areaLocationDataId;

    /**
     * 动态路由json
     */
    private String gatewayRouterDataId;

    /**
     * 转发服务地址
     */
    private String backServiceName = "private-number-hmyc";

    /**
     * 江苏移动供应商服务名称
     */
    private String backThirdServiceName = "private-number-hmyc-third";

    /**
     * 广电供应商服务名称
     */
    private String backBroadNetThirdServiceName = "private-number-hmyc-third-broadnet";

    /**
     * 异地nacos集群信息
     */
    private BackNacos backNacos;

    /**
     * 请求异地接口 超时时间
     */
    private Integer httpTimeout;

    /**
     * 当前机房 A机房/ B机房
     */
    private String curLocation;

    /**
     * 每5秒 拉取异地nacos服务列表
     */
    private String pollingCron;

    /**
     * 定时拉取异地服务3次, 清空本地缓存
     */
    private Integer pollingFailCount;

    /**
     * 需转发的uri
     */
    private List<String> forwardUriList = new ArrayList<>();

    /**
     * hcode 列表url
     */
    private String hCodeListUrl;

    /**
     * 企业id
     */
    private String vccId;

    /**
     * 是否记录请求日志
     */
    private Boolean enableLog;

    private Long tsTimeout = 300L;

    /**
     * 根据绑定id查询不到供应商id时, 根据特定vccId指定供应商id
     * 3538 hdh
     */
    private Boolean supplierSwitch = false;

    private String supplierSwitchVccId = "3538";

    private ConnectionPoolConfig connectionPoolConfig = new ConnectionPoolConfig();

    /**
     * 第三方url 不鉴权, 请求转发到private-number-hmyc-third服务
     * ex: 方太同步中间业务平台接口
     * private-number-hmyc-thirdcom.cqt.hmyc.web.fotile.controller.FotileBindController#bindOperation
     */
    private List<String> thirdHmycUriList = new ArrayList<>();

    /**
     * 忽略鉴权的url
     * ex: 查询绑定接口-给扬州电信
     * private-number-hmyc.com.cqt.hmyc.web.bind.controller.NumberBindInfoApiController#queryBindInfo
     */
    private List<String> ignoreAuthUrlList = new ArrayList<>();

    /**
     * 供应商id和处理服务对应关系
     */
    private Map<String, String> supplierWithServiceName = new HashMap<>(16);

    @Data
    public static class BackNacos {

        private String serverAddr;

        private String username;

        private String password;

        private String namespace;

        private String group;
    }

    @Data
    public static class ConnectionPoolConfig {
        /**
         * 最大空闲连接数
         */
        Integer maxIdleConnections = 200;

        /**
         * 使用适合单用户应用程序的调整参数创建一个新的连接池。
         * 此池中的调整参数可能会在未来的 OkHttp 版本中发生变化。
         * 目前，此池最多可容纳 5 个空闲连接，这些连接将在 5 分钟不活动后被驱逐。
         */
        Long keepAliveDuration = 5L;

        /**
         * 设置并发执行的最大请求数。上面这个请求在内存中排队，等待正在运行的调用完成。
         * 如果在调用时超过maxRequests个请求在进行中，则这些请求将保持在进行中。
         */
        Integer maxRequests = 800;

        /**
         * 设置每个主机并发执行的最大请求数。这会通过 URL 的主机名限制请求。请注意，对单个 IP 地址的并发请求仍可能超出此限制：
         * 多个主机名可能共享一个 IP 地址或通过同一个 HTTP 代理进行路由。
         * 如果调用此方法时正在进行的请求超过maxRequestsPerHost ，则这些请求将继续进行。
         * 与主机的 WebSocket 连接不计入此限制
         */
        Integer maxRequestsPerHost = 100;

        /**
         * 设置新连接的默认连接超时。值 0 表示没有超时，否则当转换为毫秒时，值必须介于 1 和Integer.MAX_VALUE之间。
         * 将 TCP 套接字连接到目标主机时应用连接超时。默认值为 10 秒。
         */
        Integer connectTimeout = 10000;

        /**
         * 设置新连接的默认读取超时。值 0 表示没有超时，否则当转换为毫秒时，值必须介于 1 和Integer.MAX_VALUE之间。
         * 读取超时适用于 TCP 套接字和单个读取 IO 操作，包括对Source of the Response 。默认值为 10 秒。
         */
        Integer readTimeout = 10000;

        /**
         * 设置新连接的默认写入超时。值 0 表示没有超时，否则当转换为毫秒时，值必须介于 1 和Integer.MAX_VALUE之间。
         * 写入超时适用于单个写入 IO 操作。默认值为 10 秒
         */
        Integer writeTimeout = 10000;
    }


}
