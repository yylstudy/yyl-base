package com.cqt.hmyc.web.corpinfo.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.cache.CorpBusinessCache;
import com.cqt.hmyc.web.corpinfo.mapper.PrivateCorpBusinessInfoMapper;
import com.cqt.model.corpinfo.dto.ExpireTimeDTO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/2/24 10:43
 */
@Slf4j
@Service
public class CorpBusinessService {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private final NamingService namingService;

    private final RedissonUtil redissonUtil;

    private final NacosDiscoveryProperties nacosConfigProperties;

    private final PrivateCorpBusinessInfoMapper privateCorpBusinessInfoMapper;

    public CorpBusinessService(NamingService namingService, RedissonUtil redissonUtil,
                               NacosDiscoveryProperties nacosConfigProperties, PrivateCorpBusinessInfoMapper privateCorpBusinessInfoMapper) {
        this.namingService = namingService;
        this.redissonUtil = redissonUtil;
        this.nacosConfigProperties = nacosConfigProperties;
        this.privateCorpBusinessInfoMapper = privateCorpBusinessInfoMapper;
    }

    /**
     * 刷新内存配置
     */
    public void refresh() throws NacosException {
        List<PrivateCorpBusinessInfo> businessInfoList = privateCorpBusinessInfoMapper.selectList(null);
        // 删除redis
        for (PrivateCorpBusinessInfo businessInfo : businessInfoList) {
            delRedisVccInfo(businessInfo.getVccId());
        }
        List<Instance> instanceList = namingService.getAllInstances(appName, nacosConfigProperties.getGroup());
        for (Instance instance : instanceList) {
            // 删除本地缓存
            String url = "http://" + instance.getIp() + ":" + instance.getPort() + contextPath + "/api/v1/corp-business-info/delLocalVccInfo";
            HttpUtil.post(url, "");
        }
    }

    /**
     * 获取企业业务信息
     *
     * @param vccId 企业id
     * @return Optional
     */
    public Optional<PrivateCorpBusinessInfoDTO> getCorpBusinessInfo(String vccId) {

        // 本地内存
        // redis
        // mysql
        // cache aside pattern 可以先更新数据库，然后删除缓存
        try {
            Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = CorpBusinessCache.get(vccId);
            if (businessInfoOptional.isPresent()) {
                return businessInfoOptional;
            }
            String vccInfoKey = PrivateCacheUtil.geVccInfoKey(vccId);
            String corpBusinessInfo = redissonUtil.getString(vccInfoKey);
            if (StrUtil.isEmpty(corpBusinessInfo)) {
                // 有效期
                Optional<PrivateCorpBusinessInfoDTO> businessInfoDtoOptional = getPrivateCorpBusinessInfoDtoByDb(vccId);
                if (!businessInfoDtoOptional.isPresent()) {
                    return Optional.empty();
                }
                PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO = businessInfoDtoOptional.get();
                CorpBusinessCache.put(vccId, privateCorpBusinessInfoDTO);
                redissonUtil.setString(vccInfoKey, JSON.toJSONString(privateCorpBusinessInfoDTO));
                return businessInfoDtoOptional;
            }
            PrivateCorpBusinessInfoDTO businessInfoDTO = JSON.parseObject(corpBusinessInfo, PrivateCorpBusinessInfoDTO.class);
            CorpBusinessCache.put(vccId, businessInfoDTO);
            return Optional.of(businessInfoDTO);
        } catch (RedisException e) {
            log.error("getCorpBusinessInfo error: ", e);
            return getPrivateCorpBusinessInfoDtoByDb(vccId);
        }
    }

    /**
     * 查数据库
     */
    private Optional<PrivateCorpBusinessInfoDTO> getPrivateCorpBusinessInfoDtoByDb(String vccId) {
        ExpireTimeDTO expireTimeDTO = privateCorpBusinessInfoMapper.selectExpireTime(vccId);
        PrivateCorpBusinessInfo privateCorpBusinessInfo = privateCorpBusinessInfoMapper.selectById(vccId);
        if (privateCorpBusinessInfo == null) {
            return Optional.empty();
        }
        PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO = new PrivateCorpBusinessInfoDTO();
        BeanUtil.copyProperties(privateCorpBusinessInfo, privateCorpBusinessInfoDTO, true);
        privateCorpBusinessInfoDTO.setExpireStartTime(expireTimeDTO.getExpireStartTime());
        privateCorpBusinessInfoDTO.setExpireEndTime(expireTimeDTO.getExpireEndTime());
        return Optional.of(privateCorpBusinessInfoDTO);
    }

    /**
     * 获取接口参数与通用字段适配器map
     * {
     * "AXB": {
     * "whole_area": "wholearea"
     * },
     * "AXE": {
     * "whole_area": "wholearea",
     * "ayb_audio_a_call_x": "audio_a_call_x",
     * "ayb_audio_a_called_x": "audio_a_called_x",
     * "ayb_audio_b_call_x": "audio_b_call_x",
     * "ayb_audio_b_called_x": "audio_b_called_x"
     * }
     * }
     *
     * @param privateCorpBusinessInfoDTO 企业信息
     * @param numType                    号码类型
     * @return Optional
     */
    public Optional<Map<String, String>> getAdapterMap(PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO, String numType) {

        Map<String, Map<String, String>> paramsMap = privateCorpBusinessInfoDTO.getBindingParamAdapterMap();
        if (CollUtil.isEmpty(paramsMap)) {
            return Optional.empty();
        }

        return Optional.ofNullable(paramsMap.get(numType));
    }

    /**
     * 删除企业信息, 内存/redis
     *
     * @param vccId 企业id
     * @return 成功
     */
    public Boolean delVccInfo(String vccId) {
        CorpBusinessCache.remove(vccId);
        redissonUtil.delKey(PrivateCacheUtil.geVccInfoKey(vccId));
        return true;
    }

    public Boolean delLocalVccInfo(String vccId) {
        log.info("del corp info : {}", vccId);
        CorpBusinessCache.remove(vccId);
        return true;
    }

    public Boolean delRedisVccInfo(String vccId) {
        return redissonUtil.delKey(PrivateCacheUtil.geVccInfoKey(vccId));
    }
}
