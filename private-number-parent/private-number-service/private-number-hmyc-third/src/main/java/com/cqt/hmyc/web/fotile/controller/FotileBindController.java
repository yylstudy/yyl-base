package com.cqt.hmyc.web.fotile.controller;

import com.cqt.hmyc.web.fotile.model.dto.BindOperationDTO;
import com.cqt.hmyc.web.fotile.model.vo.BindOperationResultVO;
import com.cqt.hmyc.web.fotile.service.FotileBindOperateStrategyManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author linshiqiang
 * @date 2022/7/22 16:13
 */
@Api(tags = "接收方太业务平台同步的绑定关系操作")
@RestController
@RequestMapping("/api/v1/fotile")
@RequiredArgsConstructor
public class FotileBindController {

    private final FotileBindOperateStrategyManager bindOperateStrategyManager;


    @ApiOperation("号码绑定操作")
    @PostMapping("bind/{vccId}")
    public BindOperationResultVO bindOperation(@RequestBody @Validated BindOperationDTO bindOperationDTO,
                                               @PathVariable("vccId") String vccId) {

        return bindOperateStrategyManager.operate(bindOperationDTO, vccId);
    }
}
