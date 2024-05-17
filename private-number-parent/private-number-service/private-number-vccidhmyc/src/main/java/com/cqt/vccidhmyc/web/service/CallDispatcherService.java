package com.cqt.vccidhmyc.web.service;

import com.cqt.vccidhmyc.web.model.vo.CallDispatcherVO;

/**
 * @author linshiqiang
 * date:  2023-03-27 14:01
 */
public interface CallDispatcherService {

    /**
     * 话务调度
     *
     * @param callerNum 主叫
     * @param calledNum 被叫
     * @return 结果
     */
    CallDispatcherVO dispatcher(String callerNum, String calledNum);

}
