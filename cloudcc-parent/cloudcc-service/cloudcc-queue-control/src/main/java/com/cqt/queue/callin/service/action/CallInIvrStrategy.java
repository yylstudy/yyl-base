package com.cqt.queue.callin.service.action;

import com.cqt.base.enums.CallInIvrActionEnum;
import com.cqt.base.model.ResultVO;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;

/**
 * @author linshiqiang
 * date:  2023-08-21 9:22
 */
public interface CallInIvrStrategy {

    /**
     * 动作类型
     *
     * @return CallInIvrActionEnum
     */
    CallInIvrActionEnum getAction();

    /**
     * 执行操作
     *
     * @param callInIvrActionDTO ivr参数
     * @return result
     * @throws Exception 异常
     */
    ResultVO<CallInIvrActionVO> execute(CallInIvrActionDTO callInIvrActionDTO) throws Exception;
}
