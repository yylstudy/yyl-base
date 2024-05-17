package com.cqt.vccidhmyc.web.controller;

import com.cqt.vccidhmyc.web.model.vo.CallDispatcherVO;
import com.cqt.vccidhmyc.web.service.CallDispatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * date:  2023-03-27 14:01
 */
@RestController
@RequiredArgsConstructor
public class CallDispatcherController {

    private final CallDispatcherService callDispatcherService;

    @GetMapping("/getIvrType")
    public CallDispatcherVO dispatcher(@RequestParam("CALLERNUM") String callerNum,
                                       @RequestParam("CALLEDNUM") String calledNum) {

        return callDispatcherService.dispatcher(callerNum, calledNum);
    }

}
