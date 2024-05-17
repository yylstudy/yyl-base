package com.cqt.hmyc.web.dailtest.controller;

import com.cqt.hmyc.web.dailtest.service.DialTestService;
import com.cqt.model.common.Result;
import com.cqt.model.dailtest.dto.DialTestDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author linshiqiang
 * @date 2021/10/11 10:18
 */
@Api(tags = "拨测")
@RestController
@RequestMapping("api/v1/dailtest")
public class DialTestController {

    private final DialTestService dialTestService;

    public DialTestController(DialTestService dialTestService) {
        this.dialTestService = dialTestService;
    }

    @ApiOperation("发起拨测")
    @PostMapping("/{vccId}")
    public Result start(@PathVariable("vccId") String vccId, @Validated @RequestBody DialTestDTO dialTestDTO) {
        dialTestDTO.setVccId(vccId);
        return dialTestService.start(dialTestDTO);
    }

}
