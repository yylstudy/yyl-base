package com.cqt.monitor.web.switchroom.rabbitmq;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cqt.monitor.cache.LocalCache;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author linshiqiang
 * @since 2022/1/21 15:46
 */
@Slf4j
public class RabbitmqFactory {

    /**
     * rabbitmq 健康检测
     *
     * @param uri rabbitmq节点
     * @return success
     */
    public static Boolean healthCheck(RabbitmqUri uri) {

        if (uri == null) {
            return true;
        }
        AtomicInteger count = new AtomicInteger(0);
        Set<RabbitmqNode> nodeSet = uri.getHostAndPortSet();
        for (RabbitmqNode node : nodeSet) {
            if (1 == node.getMqCheckType()) {
                for (int i = 0; i < 3; i++) {
                    boolean ping = ping(node);
                    if (ping) {
                        return true;
                    }
                    count.incrementAndGet();
                }

            }
            if (0 == node.getMqCheckType()) {
                for (int i = 0; i < 3; i++) {
                    try (HttpResponse response = HttpRequest.get(node.getUri())
                            .basicAuth(node.getUsername(), node.getPassword())
                            .timeout(Convert.toInt(node.getTimeout(), 5000))
                            .execute()) {
                        if (!response.isOk()) {
                            return false;
                        }
                        String body = response.body();

                        if (!body.contains("ok")) {
                            return false;
                        }
                    } catch (Exception e) {
                        log.error("bind rabbitmq : {} fail: {}", node.getUri(), e.getMessage());
                        count.incrementAndGet();
                    }
                }
            }
        }
        return count.get() < 3;
    }

    public static Boolean ping(RabbitmqNode node) {
        Channel channel = null;
        try {
            // amqp://admin@172.16.251.10:5672/
            channel = getConnection(node).createChannel();
        } catch (Exception e) {
            log.error("{}, rabbitmq connect error: ", node, e);
            return false;
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (Exception e) {
                    log.error("{}, rabbitmq  channel.close(): ", node, e);
                }
            }
        }
        return true;
    }

    public static synchronized Connection getConnection(RabbitmqNode node) throws IOException, TimeoutException {
        Connection connection = LocalCache.getRabbitmqConnectionCache(node.getHost());
        if (connection == null) {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(node.getHost());
            connectionFactory.setPort(node.getPort());
            connectionFactory.setVirtualHost("/");
            connectionFactory.setUsername(node.getUsername());
            connectionFactory.setPassword(node.getPassword());
            connectionFactory.setRequestedHeartbeat(10);
            connection = connectionFactory.newConnection();
            LocalCache.putRabbitmqConnectionCache(node.getHost(), connection);
        }
        return connection;
    }

}
