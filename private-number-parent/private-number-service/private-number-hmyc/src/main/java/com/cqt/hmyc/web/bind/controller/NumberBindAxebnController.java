package com.cqt.hmyc.web.bind.controller;

import com.cqt.hmyc.web.bind.service.axebn.AxebnBindService;
import com.cqt.model.bind.axebn.dto.AxebnAppendTelDTO;
import com.cqt.model.bind.axebn.dto.AxebnBindQueryDTO;
import com.cqt.model.bind.axebn.dto.AxebnBindingDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author linshiqiang
 * @date 2022/3/7 14:15
 */
@Api(tags = "AXEBN模式 绑定关系操作")
@RestController
@RequestMapping("api/v1/bind/axebn")
@Deprecated
public class NumberBindAxebnController {

    private final AxebnBindService axebnBindService;

    public NumberBindAxebnController(AxebnBindService axebnBindService) {
        this.axebnBindService = axebnBindService;
    }

    @ApiOperation("AXEBN 模式码号绑定")
    @PostMapping("binding/{vccId}")
    public Result binding(@RequestBody @Validated AxebnBindingDTO axebnBindingDTO, @PathVariable("vccId") String vccId) {
        axebnBindingDTO.setVccId(vccId);
        return axebnBindService.binding(axebnBindingDTO);
    }

    @ApiOperation("AXEBN 模式码号解绑")
    @PostMapping("unbind/{vccId}")
    public Result unbind(@RequestBody @Validated UnBindDTO unbindDTO, @PathVariable("vccId") String vccId) {
        unbindDTO.setVccId(vccId);
        return axebnBindService.unbind(unbindDTO);
    }

    @ApiOperation("AXEBN 模式码号更新有效期")
    @PostMapping("updateExpiration/{vccId}")
    public Result updateExpiration(@RequestBody @Validated UpdateExpirationDTO updateExpirationDTO, @PathVariable("vccId") String vccId) {
        updateExpirationDTO.setVccId(vccId);
        return axebnBindService.updateExpirationBind(updateExpirationDTO);
    }

    @ApiOperation("AXEBN 查询绑定关系")
    @PostMapping("query/{vccId}")
    public Result query(@RequestBody @Validated AxebnBindQueryDTO bindQueryDTO, @PathVariable("vccId") String vccId) {
        bindQueryDTO.setVccId(vccId);
        return axebnBindService.query(bindQueryDTO);
    }

    @ApiOperation("AXEBN 追加tel_b")
    @PostMapping("appendTel/{vccId}")
    public Result appendTel(@RequestBody @Validated AxebnAppendTelDTO appendTelDTO, @PathVariable("vccId") String vccId) {
        appendTelDTO.setVccId(vccId);
        return axebnBindService.appendTel(appendTelDTO);
    }
}
