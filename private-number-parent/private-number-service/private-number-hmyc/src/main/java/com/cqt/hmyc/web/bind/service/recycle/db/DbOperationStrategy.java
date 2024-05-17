package com.cqt.hmyc.web.bind.service.recycle.db;

import com.cqt.model.bind.bo.MqBindInfoBO;

/**
 * @author linshiqiang
 * @date 2022/4/19 20:28
 * 数据库操作
 */
public interface DbOperationStrategy {

    /**
     * 获得号码类型
     *
     * @return 号码类型
     */
    String getBusinessType();

    /**
     * 回收操作
     *
     * @param mqBindInfoBO 数据
     */
    void operate(MqBindInfoBO mqBindInfoBO);

}
