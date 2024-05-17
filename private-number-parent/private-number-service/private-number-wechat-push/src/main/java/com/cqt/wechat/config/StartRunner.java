package com.cqt.wechat.config;

import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.model.numpool.entity.PrivateVccInfo;
import com.cqt.wechat.dao.VccInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author hlx
 * @date 2021-11-28
 */
@Component

@Slf4j
public class StartRunner {

    @Autowired
    private VccInfoMapper vccInfoMapper;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 数据库读取企业信息
     */
    public void run() {
        List<PrivateVccInfo> list = vccInfoMapper.selectList(null);
        Map<String, List<PrivateVccInfo>> vccIdMap = list.stream()
                .collect(Collectors.groupingBy(PrivateVccInfo::getVccId));
        vccIdMap.forEach((vccId, infos) -> {
            for (PrivateVccInfo privateVccInfo : infos) {
                String vccKey = String.format(PrivateCacheConstant.VCC_INFO_KEY, vccId);
                redisTemplate.opsForValue().set(vccKey, JSON.toJSONString(privateVccInfo));
            }
        });
        log.info("redis写入企业信息成功！");
    }
}
