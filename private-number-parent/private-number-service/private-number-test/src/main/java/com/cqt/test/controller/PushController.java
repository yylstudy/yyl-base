package com.cqt.test.controller;

import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.common.Result;
import com.cqt.model.push.dto.UnbindPushDTO;
import com.cqt.model.push.entity.PrivateBillInfo;
import com.cqt.model.push.entity.PrivateStatusInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * push 模块 测试接口
 *
 * @author hlx
 * @date 2022-03-03
 */
@RestController
@Slf4j
public class PushController {

    @PostMapping("/private-number/api/v1/bind/axb/binding/{vccId}")
    public Result binding(@RequestBody @Validated AxbBindingDTO bindingDTO, @PathVariable("vccId") String vccId) {
        log.info("bindingDTO: {}", bindingDTO);

        return Result.ok();
    }


    @PostMapping("/push/bill")
    public Result billPush(@RequestBody PrivateBillInfo privateBillInfo) {
        log.info("接收话单{}", privateBillInfo.toString());

        return Result.ok();
    }


    @PostMapping("/push/status")
    public Result statusPush(@RequestBody PrivateStatusInfo statusInfo){
        log.info("接收状态{}",statusInfo.toString());

        return Result.ok();
    }

    @PostMapping("/push/unbind")
    public Result unbindPush(@RequestBody UnbindPushDTO unbindPushDTO){
        log.info("接收解绑事件{}",unbindPushDTO.toString());

        return Result.ok();
    }

    @PostMapping("/push/aybBind")
    public Result aybBindPush(@RequestBody UnbindPushDTO unbindPushDTO){
        log.info("接收ayb绑定事件{}",unbindPushDTO.toString());

        return Result.ok();
    }

}
