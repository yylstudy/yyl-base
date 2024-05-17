package com.cqt.monitor.web.distributor;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @since 2022-12-02 9:30
 * 中间号 sn服务监控, 异常修改sbc dis组权重为0
 */
@Data
public class SbcDistributorMonitorConfig {

    /**
     * 营运商SBC dis组配置信息
     */
    private DistributorConfig operator;

    /**
     * 客户SBC dis组配置信息
     */
    private DistributorConfig customer;

    /**
     * as服务健康检测接口
     */
    private List<String> healthUrl;

    /**
     * 健康检测接口请求超时时间
     */
    private Integer timeout = 1000;

    /**
     * 健康检测接口最大重试次数
     */
    private Integer maxRetry = 3;

    /**
     * 钉钉告警业务分组名称
     */
    private String dingtalkGroup;

    /**
     * 异常时 断开的权重
     */
    private String errWeight = "0";

    /**
     * 正常时的权重
     */
    private String normalWeight = "10";

    /**
     * 是否开启自动恢复
     */
    private Boolean enableRecover = Boolean.FALSE;

    /**
     * 钉钉消息合并条数
     */
    private Integer mergeCount = 4;

    @Data
    public static class DistributorConfig {

        /**
         * 是否开启
         */
        private Boolean enable = Boolean.FALSE;

        /**
         * dis组名称
         */
        private String disListName;

        /**
         * dis组配置文件路径
         */
        private String distributorConfigPath;

        /**
         * SBC服务器ip列表
         */
        private List<String> server;

        /**
         * dis组节点对应SN/ACD的服务器ip
         * {
         * "snIp1": [
         * "nodeName1"
         * ],
         * "snIp2": [
         * "nodeName2"
         * ]
         * }
         */
        private Map<String, List<String>> nodeInfo;

    }

}
