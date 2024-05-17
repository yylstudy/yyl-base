package com.cqt.queue.callin.controller;

import com.cqt.base.model.ResultVO;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.cqt.queue.callin.service.action.CallInIvrStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * date:  2023-07-19 16:32
 */
@RestController
@RequiredArgsConstructor
public class CallInIvrActionController {

    private final CallInIvrStrategyFactory callInIvrStrategyFactory;

    @PostMapping({"call-in-ivr", "distributeAgent"})
    public ResultVO<CallInIvrActionVO> callInIvr(@RequestBody CallInIvrActionDTO callInIvrActionDTO) throws Exception {

        return callInIvrStrategyFactory.execute(callInIvrActionDTO);
    }

}
