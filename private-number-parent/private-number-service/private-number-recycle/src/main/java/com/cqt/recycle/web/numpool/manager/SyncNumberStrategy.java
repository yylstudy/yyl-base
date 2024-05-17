package com.cqt.recycle.web.numpool.manager;

import com.cqt.model.numpool.dto.NumberChangeSyncDTO;

/**
 * @author linshiqiang
 * @date 2022/5/26 11:12
 * 同步号码到内存策略
 */
public interface SyncNumberStrategy {

    /**
     * 同步号码
     *
     * @param numberChangeSyncDTO 号码
     * @return
     */
    void sync(NumberChangeSyncDTO numberChangeSyncDTO);

    /**
     * 获得业务模式
     *
     * @return 业务模式
     */
    String getBusinessType();
}
