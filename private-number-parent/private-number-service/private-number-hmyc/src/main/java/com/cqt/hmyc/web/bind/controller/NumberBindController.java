package com.cqt.hmyc.web.bind.controller;

import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryStrategyManager;
import com.cqt.model.bind.query.BindInfoQuery;
import com.cqt.model.bind.vo.BindInfoVO;
import com.cqt.model.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

/**
 * @author linshiqiang
 * @date 2021/9/10 18:02
 */
@Api(tags = "绑定关系查询")
@RestController
@RequestMapping("api/v1/query")
public class NumberBindController {

    private final BindInfoQueryStrategyManager bindInfoQueryStrategyManager;

    public NumberBindController(BindInfoQueryStrategyManager bindInfoQueryStrategyManager) {
        this.bindInfoQueryStrategyManager = bindInfoQueryStrategyManager;
    }

    @ApiOperation("绑定关系查询")
    @GetMapping("bindInfo")
    public BindInfoVO queryBindInfo(BindInfoQuery bindInfoQuery) {

        return bindInfoQueryStrategyManager.query(bindInfoQuery);

    }

    @ApiOperation("X模式绑定关系查询")
    @PostMapping("bindInfoX")
    public Result queryBindInfoX(@RequestBody BindInfoQuery bindInfoQuery) {

        return Result.ok(bindInfoQueryStrategyManager.query(bindInfoQuery));

    }
}
