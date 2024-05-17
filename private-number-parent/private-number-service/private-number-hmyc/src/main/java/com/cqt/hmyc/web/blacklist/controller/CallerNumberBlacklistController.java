package com.cqt.hmyc.web.blacklist.controller;

import com.cqt.hmyc.web.blacklist.model.dto.CallerNumberBlacklistOperateDTO;
import com.cqt.hmyc.web.blacklist.service.CallerNumberBlacklistService;
import com.cqt.model.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author linshiqiang
 * date:  2024-02-04 10:15
 */
@Api(tags = "来电黑名单操作")
@RestController
@RequestMapping("/api/v1/bind/blacklist")
@RequiredArgsConstructor
public class CallerNumberBlacklistController {

    private final CallerNumberBlacklistService callerNumberBlacklistService;

    @ApiOperation("黑名单操作")
    @PostMapping("{businessType}/{vccId}")
    public Result callerBlacklist(@RequestBody @Validated CallerNumberBlacklistOperateDTO callerNumberBlacklistOperateDTO,
                                  @PathVariable("businessType") String businessType,
                                  @PathVariable("vccId") String vccId) {
        return callerNumberBlacklistService.callerBlacklist(callerNumberBlacklistOperateDTO, businessType, vccId);
    }
}
