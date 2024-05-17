package com.cqt.queue.calltask.service;

import com.cqt.base.enums.calltask.CallTaskEnum;
import com.cqt.base.model.ResultVO;
import com.cqt.model.calltask.dto.CallTaskOperateDTO;

/**
 * @author linshiqiang
 * date:  2023-10-25 14:55
 */
public interface OutboundCallTaskStrategy {

    CallTaskEnum getCallTaskEnum();

    ResultVO<Integer> addTask(CallTaskOperateDTO callTaskOperateDTO);

    ResultVO<Void> startTask(CallTaskOperateDTO callTaskOperateDTO);

    ResultVO<Void> stopTask(CallTaskOperateDTO callTaskOperateDTO);

    ResultVO<Void> updateTask(CallTaskOperateDTO callTaskOperateDTO);

    ResultVO<Void> delTask(CallTaskOperateDTO callTaskOperateDTO);

}
