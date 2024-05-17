package com.cqt.hmyc.web.bind.controller;

import com.cqt.common.constants.SystemConstant;
import com.cqt.hmyc.web.bind.service.ax.AxBindService;
import com.cqt.model.bind.ax.dto.AxBindingDTO;
import com.cqt.model.bind.ax.dto.SetUpTelDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author linshiqiang
 * @date 2022/3/15 11:36
 */
@Api(tags = "AX 绑定关系操作")
@RestController
@RequestMapping(SystemConstant.BIND_URI + "ax")
public class NumberBindAxController {

    private final AxBindService axBindService;

    public NumberBindAxController(AxBindService axBindService) {
        this.axBindService = axBindService;
    }

    @ApiOperation("AX模式码号绑定")
    @PostMapping("binding/{vccId}")
    public Result binding(@RequestBody @Validated AxBindingDTO axBindingDTO, @PathVariable("vccId") String vccId) {
        axBindingDTO.setVccId(vccId);
        return axBindService.bindingAx(axBindingDTO);
    }

    @ApiOperation("AX模式码号解绑")
    @PostMapping("unbind/{vccId}")
    public Result unbind(@RequestBody @Validated UnBindDTO unBindDTO, @PathVariable("vccId") String vccId) {
        unBindDTO.setVccId(vccId);
        return axBindService.unbind(unBindDTO);
    }

    @ApiOperation("AX模式码号更新有效期")
    @PostMapping("updateExpiration/{vccId}")
    public Result updateExpiration(@RequestBody @Validated UpdateExpirationDTO updateExpirationDTO, @PathVariable("vccId") String vccId) {
        updateExpirationDTO.setVccId(vccId);
        return axBindService.updateExpiration(updateExpirationDTO);
    }

    @ApiOperation("AX模式 设置telB")
    @PostMapping("setupTelB/{vccId}")
    public Result setupTelB(@RequestBody @Validated SetUpTelDTO setUpTelB, @PathVariable("vccId") String vccId) {
        setUpTelB.setVccId(vccId);
        return axBindService.setupTelB(setUpTelB);
    }

}
