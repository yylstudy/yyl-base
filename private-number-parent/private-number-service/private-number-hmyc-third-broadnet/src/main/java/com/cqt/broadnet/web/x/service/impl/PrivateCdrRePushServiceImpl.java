package com.cqt.broadnet.web.x.service.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.web.x.mapper.PrivateCdrRePushMapper;
import com.cqt.broadnet.web.x.service.PrivateCdrRePushService;
import com.cqt.model.push.entity.PrivateCdrRePush;
import com.cqt.rabbitmq.retry.model.PushRetryDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-02-20 14:53
 * 话单重推失败入库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateCdrRePushServiceImpl implements PrivateCdrRePushService {

    private final PrivateNumberBindProperties privateNumberBindProperties;

    private final PrivateCdrRePushMapper privateCdrRePushMapper;

    @Override
    public void savePrivateCdrRePush(PushRetryDataDTO pushRetryDataDTO) {
        PrivateCdrRePush privateCdrRePush = new PrivateCdrRePush();
        privateCdrRePush.setId(pushRetryDataDTO.getUniqueId());
        privateCdrRePush.setCdrPushUrl(pushRetryDataDTO.getPushUrl());
        privateCdrRePush.setVccId(pushRetryDataDTO.getVccId());
        privateCdrRePush.setVccName(pushRetryDataDTO.getVccName());
        privateCdrRePush.setFailReason(pushRetryDataDTO.getErrorMessage());
        privateCdrRePush.setJsonStr(pushRetryDataDTO.getPushData());
        privateCdrRePush.setRepushFailTime(DateUtil.date());
        privateCdrRePush.setCreateTime(DateUtil.date());
        privateCdrRePush.setCreateBy(privateNumberBindProperties.getSupplierId());
        privateCdrRePush.setUpdateTime(DateUtil.date());
        privateCdrRePush.setUpdateBy(privateNumberBindProperties.getSupplierId());
        try {
            int insert = privateCdrRePushMapper.insert(privateCdrRePush);
            log.info("id: {}, 短信话单重推失败数据, 不再推送原因: {}, 入库: {}", pushRetryDataDTO.getUniqueId(),
                    pushRetryDataDTO.getErrorMessage(), insert);
        } catch (Exception e) {
            log.error("短信话单重推失败数据: {}, 入库失败: ", JSON.toJSONString(pushRetryDataDTO), e);
        }
    }
}
