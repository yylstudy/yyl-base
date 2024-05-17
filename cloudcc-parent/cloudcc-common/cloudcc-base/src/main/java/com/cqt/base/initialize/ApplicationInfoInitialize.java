package com.cqt.base.initialize;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-07-12 13:53
 * 应用信息打印初始化
 */
@Component
public class ApplicationInfoInitialize implements ApplicationListener<ApplicationReadyEvent> {

    public static final String SERVER_IP = NetUtil.getLocalIp();

    private static final String SCHEME = "http";

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationInfoInitialize.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ConfigurableApplicationContext applicationContext = event.getApplicationContext();
        Environment env = applicationContext.getEnvironment();
        String ip = SERVER_IP;
        String port = env.getProperty("server.port");
        String path = Convert.toStr(env.getProperty("server.servlet.context-path"), "");
        String nacosServer = env.getProperty("NACOS_SERVER");
        String backNacosServer = env.getProperty("BACK_NACOS_SERVER");
        StringBuffer print = new StringBuffer();
        print.append(StrUtil.LF)
                .append("----------------------------------------------------------")
                .append(StrUtil.LF)
                .append("Application is running! Access URLs:").append(StrUtil.LF)
                .append(StrFormatter.format("{}Version: {}://{}:{}{}/get-version", StrUtil.TAB, SCHEME, ip, port, path))
                .append(StrUtil.LF)
                .append(StrFormatter.format("{}Health: {}://{}:{}{}/actuator/health", StrUtil.TAB, SCHEME, ip, port, path))
                .append(StrUtil.LF)
                .append(StrFormatter.format("{}Prometheus: {}://{}:{}{}/actuator/prometheus", StrUtil.TAB, SCHEME, ip, port, path))
                .append(StrUtil.LF)
                .append(StrFormatter.format("{}Docs: {}://{}:{}{}/doc.html", StrUtil.TAB, SCHEME, ip, port, path))
                .append(StrUtil.LF)
                .append(StrFormatter.format("{}NACOS_SERVER: {}", StrUtil.TAB, nacosServer))
                .append(StrUtil.LF)
                .append(StrFormatter.format("{}BACK_NACOS_SERVER: {}", StrUtil.TAB, backNacosServer))
                .append(StrUtil.LF)
                .append("----------------------------------------------------------");
        LOGGER.info("{}", print);
    }
}
