package com.cqt.hmyc.web.bind.controller;

import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.hmyc.web.bind.service.axg.AxgBindService;
import com.cqt.model.bind.axg.dto.AxgBindingDTO;
import com.cqt.model.bind.dto.AppendTelDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.bind.dto.UpdateTelBindDTO;
import com.cqt.model.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author linshiqiang
 * @date 2021/9/9 14:53
 */
@Api(tags = "AXG 绑定关系操作")
@RestController
@RequestMapping("api/v1/bind/axg")
@RequiredArgsConstructor
public class NumberBindAxgController {

    private final static String TYPE = BusinessTypeEnum.AXYB.name();

    private final AxgBindService bindService;

    @ApiOperation("AXG模式码号绑定")
    @PostMapping("binding/{vccId}")
    public Result binding(@RequestBody @Validated AxgBindingDTO bindingDTO, @PathVariable("vccId") String vccId) {
        bindingDTO.setVccId(vccId);
        return bindService.binding(bindingDTO);
    }

    @ApiOperation("AXG模式码号解绑")
    @PostMapping("unbind/{vccId}")
    public Result unbind(@RequestBody @Validated UnBindDTO unBindDTO, @PathVariable("vccId") String vccId) {
        unBindDTO.setVccId(vccId);
        return bindService.unbind(unBindDTO);
    }

    @ApiOperation("AXG模式码号更新有效期")
    @PostMapping("updateExpiration/{vccId}")
    public Result updateExpirationBind(@RequestBody @Validated UpdateExpirationDTO updateExpirationDTO, @PathVariable("vccId") String vccId) {
        updateExpirationDTO.setVccId(vccId);
        return bindService.updateExpirationBind(updateExpirationDTO);
    }

    @ApiOperation("AXG模式更新已绑定号码")
    @PostMapping("updateTelBinded/{vccId}")
    public Result updateTelBind(@RequestBody @Validated UpdateTelBindDTO updateTelBindDTO, @PathVariable("vccId") String vccId) {
        updateTelBindDTO.setVccId(vccId);
        return bindService.updateTelBind(updateTelBindDTO);
    }

    @ApiOperation("AXG 追加号码")
    @PostMapping("appendTel/{vccId}")
    public Result appendTel(@RequestBody @Validated AppendTelDTO appendTelDTO, @PathVariable("vccId") String vccId) {
        appendTelDTO.setVccId(vccId);
//        return bindService.updateTel(updateTelBindDTO);
        return Result.ok();
    }
}
