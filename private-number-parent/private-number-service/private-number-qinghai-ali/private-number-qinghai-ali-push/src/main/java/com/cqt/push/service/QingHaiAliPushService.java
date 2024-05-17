package com.cqt.push.service;

import com.cqt.model.push.entity.AcrRecordOrg;
import com.cqt.model.push.entity.PrivateStatusInfo;

public interface QingHaiAliPushService {
    void toAliEndCallRequest(AcrRecordOrg customerReceivesDataInfo, Integer num);

    void toAliCallStatusReceiver(PrivateStatusInfo privateStatusInfo, Integer num);
}
