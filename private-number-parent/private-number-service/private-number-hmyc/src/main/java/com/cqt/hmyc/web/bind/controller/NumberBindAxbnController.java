package com.cqt.hmyc.web.bind.controller;

import com.cqt.hmyc.web.bind.service.axbn.AxbnBindService;
import com.cqt.model.bind.axbn.dto.AxbnBindingDTO;
import com.cqt.model.bind.axbn.query.AxbnBindInfoQuery;
import com.cqt.model.bind.dto.AppendTelDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.bind.dto.UpdateTelBindDTO;
import com.cqt.model.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author linshiqiang
 * @date 2022/22/15 14:48
 */
@Api(tags = "AXBN 绑定关系操作")
@RestController
@RequestMapping("api/v1/bind/axbn")
public class NumberBindAxbnController {

    private final AxbnBindService bindService;

    public NumberBindAxbnController(AxbnBindService bindService) {
        this.bindService = bindService;
    }

    @ApiOperation("AXBN 模式码号绑定")
    @PostMapping("binding/{vccId}")
    public Result binding(@RequestBody @Validated AxbnBindingDTO bindingDTO, @PathVariable("vccId") String vccId) {
        bindingDTO.setVccId(vccId);
        return bindService.binding(bindingDTO);
    }

    @ApiOperation("AXBN 模式码号解绑")
    @PostMapping("unbind/{vccId}")
    public Result unbind(@RequestBody @Validated UnBindDTO unBindDTO, @PathVariable("vccId") String vccId) {
        unBindDTO.setVccId(vccId);
        return bindService.unbind(unBindDTO);
    }

    @ApiOperation("AXBN 模式码号更新有效期")
    @PostMapping("updateExpiration/{vccId}")
    public Result updateExpiration(@RequestBody @Validated UpdateExpirationDTO updateExpirationDTO, @PathVariable("vccId") String vccId) {
        updateExpirationDTO.setVccId(vccId);
        return bindService.updateExpiration(updateExpirationDTO);
    }

    @ApiOperation("AXBN 模式 查询绑定关系")
    @PostMapping("query/{vccId}")
    public Result query(@RequestBody @Validated AxbnBindInfoQuery bindInfoQuery, @PathVariable("vccId") String vccId) {
        bindInfoQuery.setVccId(vccId);
        return bindService.query(bindInfoQuery);
    }

    @ApiOperation("AXBN 模式更新已绑定号码（tel_a,tel_b,tel_b_other）")
    @PostMapping("updateTel/{vccId}")
    public Result updateTel(@RequestBody @Validated UpdateTelBindDTO updateTelBindDTO, @PathVariable("vccId") String vccId) {
        updateTelBindDTO.setVccId(vccId);
//        return bindService.updateTel(updateTelBindDTO);
        return Result.ok();
    }

    @ApiOperation("AXBN 追加号码")
    @PostMapping("appendTel/{vccId}")
    public Result appendTel(@RequestBody @Validated AppendTelDTO appendTelDTO, @PathVariable("vccId") String vccId) {
        appendTelDTO.setVccId(vccId);
//        return bindService.updateTel(updateTelBindDTO);
        return Result.ok();
    }
}
