package com.cqt.call.event.incalls;

import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.call.service.DataStoreService;
import com.cqt.cloudcc.manager.config.CommonThreadPoolConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * date:  2023-08-21 18:28
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InCallsNumberEventListener {

    private final DataStoreService dataStoreService;

    /**
     * 监听
     */
    @Async(CommonThreadPoolConfig.SAVE_POOL_NAME)
    @EventListener(classes = {InCallsNumberEvent.class})
    public void listener(InCallsNumberEvent event) {
        try {
            String companyCode = event.getCompanyCode();
            String mainCallId = event.getMainCallId();
            String uuid = event.getUuid();
            String number = event.getNumber();
            if (OperateTypeEnum.INSERT.equals(event.getOperateTypeEnum())) {
                dataStoreService.addInCallNumbers(companyCode, mainCallId, number, uuid);
                return;
            }
            dataStoreService.deleteInCallNumbers(companyCode, mainCallId, number, uuid);
        } catch (Exception e) {
            log.error("[InCallsNumberEvent] listener error: ", e);
        }
    }
}
