package com.cqt.recycle.web.corpinfo.controller;

import com.alibaba.nacos.api.exception.NacosException;
import com.cqt.model.common.Result;
import com.cqt.model.common.ResultVO;
import com.cqt.model.corpinfo.dto.SyncCorpInfoDTO;
import com.cqt.recycle.web.corpinfo.service.CorpBusinessService;
import com.cqt.recycle.web.corpinfo.service.CreateTableService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author linshiqiang
 * @date 2022/2/24 10:42
 */
@Api(tags = "企业业务配置信息")
@RestController
@RequestMapping("api/v1/corp-business-info")
@RequiredArgsConstructor
public class CorpBusinessController {

    private final CorpBusinessService corpBusinessService;

    private final CreateTableService createTableService;

    @ApiOperation("同步企业信息")
    @PostMapping("sync")
    public Result syncCorpInfo(@RequestBody @Validated SyncCorpInfoDTO syncCorpInfoDTO) throws JsonProcessingException {

        return corpBusinessService.sync(syncCorpInfoDTO);
    }

    @ApiOperation("同步企业-供应商地市号码分配策略")
    @PostMapping("sync-supplier-strategy")
    public Result syncSupplierStrategy(@RequestParam(required = false) String strategyId,
                                       @RequestParam(required = false) String operateType) {

        return corpBusinessService.syncSupplierStrategy(strategyId, operateType);
    }

    @ApiOperation("绑定关系请求切换机房(A: 全部且到A机房, B: 全部切到B机房)")
    @GetMapping("switch/{place}")
    public Result switchPlace(@PathVariable String place) throws NacosException {

        return corpBusinessService.switchPlace(place);
    }

    @ApiOperation(value = "创建绑定关系表", hidden = true)
    @PostMapping("createBindTable/{vccId}/{businessType}")
    public ResultVO<Void> createBindTable(@PathVariable("vccId") String vccId,
                                          @PathVariable("businessType") String businessType) {
        createTableService.createTable(vccId, businessType);
        return ResultVO.ok();
    }
}
