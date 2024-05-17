package com.repush.service;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
 */
@Service
@Slf4j
public class LockService {

    private static final String LOCAL_REDIS_KEY = "lost_msg_check_job";


    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 加机器锁
     *
     * @param currentIp
     * @return
     */
    public boolean lock(String currentIp) {
        // 对应setnx命令，可以成功设置,也就是key不存在
        return redisTemplate.opsForValue().setIfAbsent(LOCAL_REDIS_KEY, currentIp);
    }

    public Boolean unlock(String currentIp) {
        Object currentValue = redisTemplate.opsForValue().get(LOCAL_REDIS_KEY);
        if (ObjectUtil.isNotEmpty(currentValue) && currentIp.equals(currentValue)) {
            // 删除锁状态
            return redisTemplate.opsForValue().getOperations().delete(LOCAL_REDIS_KEY);
        }
        return false;
    }


}
