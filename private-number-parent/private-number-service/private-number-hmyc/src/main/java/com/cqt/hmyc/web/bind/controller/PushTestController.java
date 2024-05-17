package com.cqt.hmyc.web.bind.controller;

import com.cqt.model.common.Result;
import com.cqt.model.push.dto.AybBindPushDTO;
import com.cqt.model.push.dto.UnbindPushDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author linshiqiang
 * @date 2022/2/28 15:39
 */
@RestController
@Slf4j
public class PushTestController {

    @PostMapping("aybBind")
    public Result aybBind(@RequestBody AybBindPushDTO aybBindPushDTO) {
        log.info(String.valueOf(aybBindPushDTO));
        return Result.ok();
    }

    @PostMapping("unBind")
    public Result unBind(@RequestBody UnbindPushDTO unbindPushDTO) {
        log.info(String.valueOf(unbindPushDTO));
        return Result.ok();
    }
}
