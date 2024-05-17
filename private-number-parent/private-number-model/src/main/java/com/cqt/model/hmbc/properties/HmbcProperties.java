package com.cqt.model.hmbc.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 平台号码拨测配置
 *
 * @author Xienx
 * @date 2022年08月09日 15:07
 */
@Data
@Component
@ConfigurationProperties(prefix = "hmbc")
public class HmbcProperties implements Serializable {

    private static final long serialVersionUID = 195486147565696659L;

    /**
     * 外呼配置
     */
    private DialTest dialTest;

    /**
     * 位置更新配置
     */
    private LocationUpdating locationUpdating;

    /**
     * 推送配置
     */
    private PushInfo pushInfo;

    /**
     * 拨测失败的号码的过期时间，单位 天
     */
    private Integer failedNumberExpire = 7;

    @Data
    public static class LocationUpdating implements Serializable {

        private static final long serialVersionUID = -4460757582391553728L;

        /**
         * 位置更新最大重试次数, 默认3次
         */
        private Integer maxAttempts = 3;


        /**
         * 位置更新失败的等待间隔时间, 单位毫秒
         */
        private Integer interval = 3000;


        /**
         * 每次批量请求的号码数量 默认50条
         */
        private Integer perBatchLimit = 50;
    }

    @Data
    public static class DialTest implements Serializable {

        private static final long serialVersionUID = 4434466664432179209L;

        /**
         * 小号拨测重试次数, 默认三次
         * */
        private Integer callMaxAttempts = 2;

        /**
         * 拨测服务的地址
         */
        private String execCommandUrl;

        /**
         * 请求拨测服务的超时时间, 单位毫秒, 默认5000
         */
        private Integer timeout = 5000;

        /**
         * 请求拨测的间隔时间, 单位 毫秒
         */
        private Integer waitInterval = 100;

        /**
         * 请求拨测服务的失败重试次数
         */
        private Integer maxAttempts = 3;
        /**
         * 请求拨测服务的重试间隔时间, 单位毫秒
         */
        private Integer interval = 3000;

        /**
         * LUA脚本拨测参数格式 bgapi lua priavte-number-hmbc.lua {caller} {callee} {vccId}
         */
        private String luaParamFormat = "bgapi lua priavte-number-hmbc.lua %s %s %s ";

        /**
         * 拨测的主叫号码列表
         */
        private List<String> callerNumbers;

        /**
         * 未上平台的状态码
         * 默认为 5、用户停机；6、空号；7、停机
         */
        private Set<Integer> abnormalCodes;

        /**
         * GT编码与平台的映射关系
         */
        private Map<String, String> gtDsMap;

        /**
         * 话单查询最大次数 默认3次
         */
        private Integer cdrQueryMaxAttempts = 3;

        /**
         * 话单重查间隔, 单位为毫秒, 默认30000毫秒
         */
        private Integer cdrQueryInterval = 30000;

        /**
         * 拨测话单的业务标识-920007
         */
        private String serviceKey = "920007";

    }

    @Data
    public static class PushInfo implements Serializable {

        private static final long serialVersionUID = 6396129573563075543L;

        /**
         * 需要使用feign调用的接口地址配置
         */
        private Set<String> feignUrl;

        /**
         * 重推次数, 目前还未使用
         */
        private Integer maxAttempts = 3;

        /**
         * 重推间隔时间, 单位毫秒
         */
        private Integer interval = 300000;


        /**
         * 接口超时时间 单位毫秒
         */
        private Integer timeout = 5000;
    }

}
