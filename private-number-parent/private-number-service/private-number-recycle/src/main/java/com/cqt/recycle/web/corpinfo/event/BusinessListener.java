package com.cqt.recycle.web.corpinfo.event;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.cqt.common.constants.DataIdConstant;
import com.cqt.common.constants.SystemConstant;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.corpinfo.dto.ExpireTimeDTO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.recycle.web.corpinfo.mapper.PrivateCorpBusinessInfoMapper;
import com.cqt.recycle.web.corpinfo.service.CreateTableService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * date: 2022/7/27 14:13
 * 业务配置同步监听
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessListener implements ApplicationListener<BusinessEvent> {

    private static final String BUSINESS_INFO_DATA_ID = DataIdConstant.PRIVATE_NUMBER_CORP_BUSINESS_INFO_DATA_ID;

    private final PrivateCorpBusinessInfoMapper privateCorpBusinessInfoMapper;

    private final ConfigService configService;

    private final ConfigService backConfigService;

    private final HideProperties hideProperties;

    private final NacosConfigProperties nacosConfigProperties;

    private final CreateTableService createTableService;

    private final NamingService backNamingService;

    private final NacosDiscoveryProperties nacosDiscoveryProperties;

    @Value("${spring.application.name}")
    private String appName;

    private final String createTableUrl = "http://%s:%s/private-hmyc-recycle/api/v1/corp-business-info/createBindTable/%s/%s";

    @Async("recycleExecutor")
    @SneakyThrows
    @Override
    public void onApplicationEvent(BusinessEvent event) {

        String curVccId = event.getVccId();
        String businessType = event.getBusinessType();
        if (StrUtil.isNotBlank(businessType)) {
            // 本机房建表
            createTableService.createTable(curVccId, businessType);
            // 创建另一个数据库, 有效绑定关系表, 该业务模式如果配置了分表的话
            if (createTableService.isSharding(hideProperties.getBindTableSharingBusinessType(), businessType)) {
                // 调另一机房服务-建表接口
                createTableToBack(curVccId, businessType);
            }
        }

        log.info("开始同步业务配置到nacos: {}", event.getTimestamp());
        TimeUnit.SECONDS.sleep(SystemConstant.NUMBER_THREE);
        // 企业业务配置信息
        List<PrivateCorpBusinessInfo> businessInfoList = privateCorpBusinessInfoMapper.selectList(null);
        Map<String, PrivateCorpBusinessInfoDTO> businessInfoMap = new HashMap<>(32);
        for (PrivateCorpBusinessInfo businessInfo : businessInfoList) {
            String vccId = businessInfo.getVccId();
            ExpireTimeDTO expireTimeDTO = privateCorpBusinessInfoMapper.selectExpireTime(vccId);
            PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO = new PrivateCorpBusinessInfoDTO();
            BeanUtil.copyProperties(businessInfo, privateCorpBusinessInfoDTO, true);
            if (Optional.ofNullable(expireTimeDTO).isPresent()) {
                privateCorpBusinessInfoDTO.setExpireStartTime(expireTimeDTO.getExpireStartTime());
                privateCorpBusinessInfoDTO.setExpireEndTime(expireTimeDTO.getExpireEndTime());
                privateCorpBusinessInfoDTO.setVccName(expireTimeDTO.getVccName());
            }
            businessInfoMap.put(vccId, privateCorpBusinessInfoDTO);
        }
        String group = nacosConfigProperties.getGroup();
        // 业务配置json
        String businessJson = JSON.toJSONString(businessInfoMap, true);
        log.info("业务配置json：{}", businessJson);
        // 发布本地nacos
        boolean publishConfig = configService.publishConfig(BUSINESS_INFO_DATA_ID, group, businessJson, "json");
        log.info("发布本地nacos业务配置结果：{}", publishConfig);

        if (StrUtil.isNotEmpty(hideProperties.getBackNacos())) {
            boolean b = backConfigService.publishConfig(BUSINESS_INFO_DATA_ID, group, businessJson, "json");
            log.info("发布back nacos业务配置结果：{}", b);
        }
    }

    /**
     * 调用异地接口创建表
     */
    private void createTableToBack(String vccId, String businessType) {
        if (ObjectUtil.isNotEmpty(backNamingService)) {
            try {
                List<Instance> instanceList = backNamingService.getAllInstances(appName, nacosDiscoveryProperties.getGroup());
                if (CollUtil.isNotEmpty(instanceList)) {
                    Instance instance = instanceList.get(0);
                    String url = String.format(createTableUrl, instance.getIp(), instance.getPort(), vccId, businessType);
                    String post = HttpUtil.post(url, "");
                    log.info("businessType: {}, back create table {}, result: {}", businessType, vccId, post);
                }
            } catch (NacosException e) {
                log.error("nacos error: ", e);
            }
        }
    }
}
