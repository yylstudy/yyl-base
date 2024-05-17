package com.cqt.hmyc.config.nacos;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.cqt.common.config.nacos.AbstractNacosConfig;
import com.cqt.common.constants.DataIdConstant;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.hmyc.web.bind.mapper.PrivateAreaLocationMapper;
import com.cqt.model.bind.entity.PrivateAreaLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @date 2022/8/25 09:15
 * 地市-机房对应关系 json配置
 */
@Slf4j
@Component(AreaLocationConfig.DATA_ID)
@RequiredArgsConstructor
public class AreaLocationConfig extends AbstractNacosConfig {

    public static final String DATA_ID = DataIdConstant.PRIVATE_AREA_LOCATION;

    private final NacosConfigProperties nacosConfigProperties;

    private final ConfigService configService;

    private final ObjectMapper objectMapper;

    private final RedissonClient redissonClient;

    private final PrivateAreaLocationMapper privateAreaLocationMapper;

    @Override
    public void onReceived(String content) {
        RLock lock = redissonClient.getLock(PrivateCacheConstant.LOCK_AREA_LOCATION_INIT);
        try {
            if (lock.tryLock()) {
                initAreaLocationConfig(content);
            }
        } catch (Exception e) {
            log.error("getAreaLocationConfig lock error: {}", e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    private void initAreaLocationConfig(String content) throws JsonProcessingException {
        TreeMap<String, String> map = objectMapper.readValue(content, new TypeReference<TreeMap<String, String>>() {
        });
        if (CollUtil.isEmpty(map)) {
            return;
        }
        List<PrivateAreaLocation> insertList = new ArrayList<>();
        List<PrivateAreaLocation> updateList = new ArrayList<>();
        List<PrivateAreaLocation> areaLocationList = privateAreaLocationMapper.selectList(null);
        Map<String, List<PrivateAreaLocation>> listMap = areaLocationList.stream()
                .collect(Collectors.groupingBy(PrivateAreaLocation::getAreaCode));
        map.forEach((key, value) -> {
            List<PrivateAreaLocation> locationList = listMap.get(key);
            if (CollUtil.isEmpty(locationList)) {

                PrivateAreaLocation areaLocation = PrivateAreaLocation.builder()
                        .areaCode(key)
                        .initLocation(value)
                        .updateLocation(value)
                        .createTime(DateUtil.date())
                        .updateTime(DateUtil.date())
                        .build();
                insertList.add(areaLocation);
            } else {
                PrivateAreaLocation privateAreaLocation = locationList.get(0);
                if (!value.equals(privateAreaLocation.getUpdateLocation())) {
                    privateAreaLocation.setUpdateLocation(value);
                    privateAreaLocation.setUpdateTime(DateUtil.date());
                    updateList.add(privateAreaLocation);
                }
            }
        });
        if (CollUtil.isNotEmpty(updateList)) {
            int updateBatch = privateAreaLocationMapper.updateBatch(updateList);
            log.info("地市编码-机房对应关系配置发生变化, 更新数据库完成: {}, {}", updateList.size(), updateBatch);

        }
        if (CollUtil.isNotEmpty(insertList)) {
            int insertBatch = privateAreaLocationMapper.insertBatch(insertList);
            log.info("地市编码-机房对应关系配置发生变化, 新增数据库完成: {}, {}", insertList.size(), insertBatch);
        }
    }


    @Override
    public String getDataId() {
        return DATA_ID;
    }

    @Override
    public String getGroup() {
        return nacosConfigProperties.getGroup();
    }

    @Override
    public ConfigService configService() {
        return configService;
    }

}
