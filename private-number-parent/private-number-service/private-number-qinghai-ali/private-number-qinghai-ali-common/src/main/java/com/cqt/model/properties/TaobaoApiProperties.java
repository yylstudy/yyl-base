package com.cqt.model.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-01-28 10:46
 * 淘宝API 鉴权参数配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "taobao.auth")
public class TaobaoApiProperties {

    private String version = "2.0";

    private String format = "json";

    private String signMethod = "hmac-sha256";

    private String appKey;

    private String appSecret;

    /**
     * 供应商KEY
     */
    private String sessionKey;

    private String vendorKey = "QH_CMCC";

    private String requestUrl;

    private Integer timeout = 5000;

    private Integer maxDuration = 7200;

    /**
     * 默认异常音
     */
    private String notBindIvr = "exception.wav";

    /**
     * 请输入分机号
     */
    private String digitsIvr = "digitsIvr.wav";

    /**
     * 是否为测试环境
     */
    private Boolean test = false;

    /**
     * 测试同步接口地址
     */
    private String testSyncUrl;

    /**
     * 短信接口测试地址
     */
    private String testSmsUrl;

    /**
     * 模拟测试参数
     */
    private TestBind testBind = new TestBind();

    @Data
    public static class TestBind {

        private Boolean test = false;

        /**
         * 测试查询绑定接口
         */
        private String testAxbUrl;

        /**
         * AXB模式X号码
         */
        private String axbNumber;

        /**
         * AXE模式X号码
         */
        private String axeNumber;

        /**
         * 测试分机号
         */
        private String digitInfo;
    }
}
