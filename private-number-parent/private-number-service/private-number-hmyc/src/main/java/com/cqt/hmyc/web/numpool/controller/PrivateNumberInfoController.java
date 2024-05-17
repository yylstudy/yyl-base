package com.cqt.hmyc.web.numpool.controller;

import com.cqt.common.enums.AxbPoolTypeEnum;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.hmyc.web.cache.*;
import com.cqt.hmyc.web.numpool.manager.SyncNumberStrategyManager;
import com.cqt.hmyc.web.numpool.service.PrivateNumberInfoService;
import com.cqt.model.common.Result;
import com.cqt.model.numpool.dto.NumberChangeSyncDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/5/27 9:35
 */
@Api(tags = "本地内存-号码信息")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("api/v1/number")
public class PrivateNumberInfoController {

    private final PrivateNumberInfoService privateNumberInfoService;

    private final SyncNumberStrategyManager syncNumberStrategyManager;

    private final SyncNumberPoolJob syncNumberPoolJob;

    @ApiOperation("同步新增删除的号码信息到本地内存")
    @PostMapping("sync")
    public Result sync(@RequestBody NumberChangeSyncDTO numberChangeSyncDTO) {

        return syncNumberStrategyManager.sync(numberChangeSyncDTO);
    }

    @ApiOperation("刷新本地内存数据")
    @PostMapping("refresh")
    public Result refresh() {

        syncNumberPoolJob.clear();
        syncNumberPoolJob.refreshCorpBusiness();
        syncNumberPoolJob.refreshNumberPool();

        return Result.ok();
    }

    @ApiOperation("查询号码的业务类型")
    @GetMapping("business-type/{number}")
    public String getBusinessType(@PathVariable("number") String number) {
        return NumberTypeCache.getNumType(number);
    }

    @ApiOperation("查询所有号码池号码")
    @GetMapping("getAllNumberPool/{businessType}")
    public Map<String, Object> getAllNumberPool(@PathVariable("businessType") String businessType) {
        Map<String, Object> numberPool = new HashMap<>(16);
        numberPool.put("AXB_NUM_POOL_MAP", NumberPoolAxbCache.AXB_NUM_POOL_MAP);
        numberPool.put("AXB_NUM_POOL_MASTER_MAP", NumberPoolAxbCache.AXB_NUM_POOL_MASTER_MAP);
        numberPool.put("AXB_NUM_POOL_SLAVE_MAP", NumberPoolAxbCache.AXB_NUM_POOL_SLAVE_MAP);
        numberPool.put("AX_NUM_POOL_MAP", NumberPoolAxCache.AX_NUM_POOL_MAP);
        numberPool.put("AXE_NUM_POOL_MAP", NumberPoolAxeCache.AXE_NUM_POOL_MAP);
        numberPool.put("AXYB_X_NUM_POOL_MAP", NumberPoolAxybCache.X_NUM_POOL_MAP);
        numberPool.put("AXYB_Y_NUM_POOL_MAP", NumberPoolAxybCache.Y_NUM_POOL_MAP);
        numberPool.put("AXG_NUM_POOL_MAP", NumberPoolAxgCache.AXG_NUM_POOL_MAP);
        numberPool.put("AXBN_NUM_POOL_MAP", NumberPoolAxbnCache.AXBN_NUM_POOL_MAP);
        numberPool.put("OUT_NUM_POOL_MAP", NumberPoolOutCache.OUT_NUM_POOL_MAP);
        return numberPool;
    }

    @ApiOperation("根据业务类型查询所有号码池号码")
    @GetMapping("getNumberPool/{businessType}")
    public Map<String, Object> getNumberPool(@PathVariable("businessType") String businessType) {
        Map<String, Object> numberPool = new HashMap<>(16);
        if (BusinessTypeEnum.AXB.name().equals(businessType)) {
            numberPool.put("AXB_NUM_POOL_MAP", NumberPoolAxbCache.AXB_NUM_POOL_MAP);
            numberPool.put("AXB_NUM_POOL_MASTER_MAP", NumberPoolAxbCache.AXB_NUM_POOL_MASTER_MAP);
            numberPool.put("AXB_NUM_POOL_SLAVE_MAP", NumberPoolAxbCache.AXB_NUM_POOL_SLAVE_MAP);
        }
        if (BusinessTypeEnum.AX.name().equals(businessType)) {
            numberPool.put("AX_NUM_POOL_MAP", NumberPoolAxCache.AX_NUM_POOL_MAP);

        }
        if (BusinessTypeEnum.AXE.name().equals(businessType)) {
            numberPool.put("AXE_NUM_POOL_MAP", NumberPoolAxeCache.AXE_NUM_POOL_MAP);

        }
        if (BusinessTypeEnum.AXYB.name().equals(businessType)) {
            numberPool.put("AXYB_X_NUM_POOL_MAP", NumberPoolAxybCache.X_NUM_POOL_MAP);
            numberPool.put("AXYB_Y_NUM_POOL_MAP", NumberPoolAxybCache.Y_NUM_POOL_MAP);
        }
        if (BusinessTypeEnum.AXG.name().equals(businessType)) {
            numberPool.put("AXG_NUM_POOL_MAP", NumberPoolAxgCache.AXG_NUM_POOL_MAP);

        }
        if (BusinessTypeEnum.AXBN.name().equals(businessType)) {
            numberPool.put("AXBN_NUM_POOL_MAP", NumberPoolAxbnCache.AXBN_NUM_POOL_MAP);

        }
        if (BusinessTypeEnum.X.name().equals(businessType)) {
            numberPool.put("OUT_NUM_POOL_MAP", NumberPoolOutCache.OUT_NUM_POOL_MAP);
        }
        return numberPool;
    }

    @ApiOperation("根据业务类型-vccId-areaCode查询所有号码池号码")
    @GetMapping("getNumberPoolByAreaCode/{businessType}/{vccId}/{areaCode}")
    public Map<String, Object> getNumberPoolByAreaCode(@PathVariable("businessType") String businessType,
                                                       @PathVariable("vccId") String vccId,
                                                       @PathVariable("areaCode") String areaCode) {
        Map<String, Object> numberPool = new HashMap<>(16);
        if (BusinessTypeEnum.AXB.name().equals(businessType)) {
            numberPool.put("AXB_NUM_POOL_MAP", NumberPoolAxbCache.getPool(AxbPoolTypeEnum.ALL, vccId, areaCode));
            numberPool.put("AXB_NUM_POOL_MASTER_MAP", NumberPoolAxbCache.getPool(AxbPoolTypeEnum.MASTER, vccId, areaCode));
            numberPool.put("AXB_NUM_POOL_SLAVE_MAP", NumberPoolAxbCache.getPool(AxbPoolTypeEnum.SLAVE, vccId, areaCode));
        }
        if (BusinessTypeEnum.AX.name().equals(businessType)) {
            numberPool.put("AX_NUM_POOL_MAP", NumberPoolAxCache.getPool(vccId, areaCode));
        }
        if (BusinessTypeEnum.AXE.name().equals(businessType)) {
            numberPool.put("AXE_NUM_POOL_MAP", NumberPoolAxeCache.getPool(vccId, areaCode));
        }
        if (BusinessTypeEnum.AXYB.name().equals(businessType)) {
            numberPool.put("AXYB_X_NUM_POOL_MAP", NumberPoolAxybCache.getPoolX(vccId, areaCode));
            numberPool.put("AXYB_Y_NUM_POOL_MAP", NumberPoolAxybCache.getPoolY(vccId, areaCode));
        }
        if (BusinessTypeEnum.AXG.name().equals(businessType)) {
            numberPool.put("AXG_NUM_POOL_MAP", NumberPoolAxgCache.getPool(vccId, areaCode));
        }
        if (BusinessTypeEnum.AXBN.name().equals(businessType)) {
            numberPool.put("AXBN_NUM_POOL_MAP", NumberPoolAxbnCache.getPool(vccId, areaCode));
        }
        if (BusinessTypeEnum.X.name().equals(businessType)) {
            numberPool.put("OUT_NUM_POOL_MAP", NumberPoolOutCache.getPool(vccId, areaCode));
        }
        return numberPool;
    }
}
