package com.cqt.hmyc.web.bind.service.recycle.recycle;

import com.cqt.model.bind.dto.BindRecycleDTO;

/**
 * @author linshiqiang
 * @date 2022/4/19 20:04
 */
public interface RecycleNumberStrategy {

    /**
     * 获得号码类型
     *
     * @return 号码类型
     */
    String getBusinessType();

    /**
     * 回收操作
     *
     * @param bindRecycleDTO 回收实体
     */
    void recycle(BindRecycleDTO bindRecycleDTO);

}
