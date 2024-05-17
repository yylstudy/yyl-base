package com.cqt.recycle.web.numpool.controller;

import com.cqt.model.common.Result;
import com.cqt.model.common.ResultVO;
import com.cqt.model.numpool.dto.NumberChangeSyncDTO;
import com.cqt.model.numpool.dto.NumberPoolQueryDTO;
import com.cqt.model.numpool.vo.NumberPoolVO;
import com.cqt.recycle.web.numpool.manager.PrivateCorpNumberPoolService;
import com.cqt.recycle.web.numpool.manager.SyncNumberStrategyManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author linshiqiang
 * @date 2022/5/27 11:13
 */
@Api(tags = "同步号码池 号码维护操作")
@RestController
@Slf4j
@RequestMapping("/api/v1/number")
@RequiredArgsConstructor
public class NumberPoolSyncController {

    private final SyncNumberStrategyManager syncNumberStrategyManager;
    private final PrivateCorpNumberPoolService privateCorpNumberPoolService;

    @ApiOperation("同步新增删除的号码信息到号码隐藏本地内存")
    @PostMapping("sync")
    public Result sync(@RequestBody @Validated NumberChangeSyncDTO numberChangeSyncDTO) {

        return syncNumberStrategyManager.sync(numberChangeSyncDTO);
    }

    @ApiOperation(value = "查询号码池号码信息")
    @PostMapping("/axe/{vccId}")
    public ResultVO<List<NumberPoolVO>> numberPoolQuery(@PathVariable("vccId") String vccId, @Validated @RequestBody NumberPoolQueryDTO queryDTO) {
        return ResultVO.ok(privateCorpNumberPoolService.queryPoolNums(vccId, queryDTO));
    }
}
