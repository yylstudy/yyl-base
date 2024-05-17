package com.cqt.broadnet.web.axb.jsch;

import com.jcraft.jsch.ChannelSftp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;

/**
 * @author linshiqiang
 * date 2023-03-29 20:45:00
 */
@Slf4j
public class JschConnectionPool {

    private final GenericObjectPool<ChannelSftp> pool;

    public JschConnectionPool(String host, int port, String username, String password,
                              Integer maxTotal, Integer minIdle, Integer maxIdle,
                              Duration timout) {
        JschFactory jschFactory = new JschFactory(host, port, username, password);
        GenericObjectPoolConfig<ChannelSftp> config = new GenericObjectPoolConfig<>();

        // 设置最大连接数
        config.setMaxTotal(maxTotal);

        // 设置空闲连接数
        config.setMinIdle(minIdle);

        // 设置最大空闲连接数
        config.setMaxIdle(maxIdle);

        // 设置连接等待时间，单位毫秒
        config.setTestOnReturn(true);
        config.setTestOnBorrow(true);
        config.setTestOnCreate(true);
        config.setBlockWhenExhausted(true);
        config.setTestWhileIdle(true);
        pool = new GenericObjectPool<>(jschFactory, config);
    }

    public ChannelSftp borrowObject() throws Exception {
        ChannelSftp channelSftp = pool.borrowObject();
        log.info("borrow pool active: {}, idle: {}", pool.getNumActive(), pool.getNumIdle());
        return channelSftp;
    }

    public void returnObject(ChannelSftp channelSftp) {
        if (channelSftp == null) {
            return;
        }
        if (channelSftp.isConnected()) {
            pool.returnObject(channelSftp);
        }
        log.info("return pool active: {}, idle: {}", pool.getNumActive(), pool.getNumIdle());
    }

    public void close() {
        pool.close();
    }

    public GenericObjectPool<ChannelSftp> getPool() {
        return pool;
    }

}
