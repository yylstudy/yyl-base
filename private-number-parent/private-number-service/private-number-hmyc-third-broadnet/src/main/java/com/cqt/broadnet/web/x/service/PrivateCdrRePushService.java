package com.cqt.broadnet.web.x.service;

import com.cqt.rabbitmq.retry.model.PushRetryDataDTO;

/**
 * @author linshiqiang
 * date:  2023-02-20 14:53
 * 话单重推失败数据入库
 */
public interface PrivateCdrRePushService {

    /**
     * 话单重推失败数据入库
     *
     * @param pushRetryDataDTO 参数
     */
    void savePrivateCdrRePush(PushRetryDataDTO pushRetryDataDTO);
}
