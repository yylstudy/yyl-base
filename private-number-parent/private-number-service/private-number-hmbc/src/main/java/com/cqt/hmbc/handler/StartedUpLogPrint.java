package com.cqt.hmbc.handler;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;

/**
 * 服务启动成功后的一些日志输出
 *
 * @author Xienx
 * date 2023年01月30日 9:38
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartedUpLogPrint implements ApplicationRunner {
    private final ServerProperties serverProperties;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    @Override
    public void run(ApplicationArguments args) {
        String contextPath = Convert.toStr(serverProperties.getServlet().getContextPath(), StrUtil.EMPTY);
        
        String baseUrl = String.format("%s://%s:%s%s",
                "http",
                nacosDiscoveryProperties.getIp(),
                nacosDiscoveryProperties.getPort(),
                contextPath);
        String versionUrl = baseUrl + "/get-version";
        String healthUrl = baseUrl + "/actuator/health";
        String docUrl = baseUrl + "/doc.html";

        StringBuilder sb = new StringBuilder();
        sb.append("\n----------------------------------------------------------\n\t");
        sb.append("Application is running! Access URLs:\n\t");
        sb.append("Version: \t")
                .append(versionUrl)
                .append("\n\t");

        sb.append("Health: \t")
                .append(healthUrl)
                .append("\n\t");

        sb.append("Swagger文档: \t")
                .append(docUrl)
                .append("\n\t");

        sb.append("NACOS_SERVER: \t\t")
                .append(nacosDiscoveryProperties.getServerAddr())
                .append("\n");
        sb.append("----------------------------------------------------------");

        log.info("{}", sb);
    }
}
