package com.cqt.broadnet.web.x.service.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author linshiqiang
 * date:  2023-02-20 15:06
 * 初始化话单重推队列,交换机,监听器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BillPushRetryInit {

    private final CallBillPushRetryImpl callBillPushRetryImpl;

    private final SmsBillPushRetryImpl smsBillPushRetryImpl;

    private final StatusBillPushRetryImpl statusBillPushRetryImpl;


//    @PostConstruct
    public void init() throws Exception {
        callBillPushRetryImpl.createDlx();
        smsBillPushRetryImpl.createDlx();
        statusBillPushRetryImpl.createDlx();
        callBillPushRetryImpl.addMessageListener(true);
        smsBillPushRetryImpl.addMessageListener(true);
        statusBillPushRetryImpl.addMessageListener(true);
        log.info("init bill push retry");
    }
}
