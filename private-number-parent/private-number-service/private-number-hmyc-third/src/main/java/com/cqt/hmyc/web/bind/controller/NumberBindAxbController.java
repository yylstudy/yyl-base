package com.cqt.hmyc.web.bind.controller;

import com.cqt.common.constants.SystemConstant;
import com.cqt.hmyc.web.bind.service.axb.AxbBindService;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



/**
 * @author linshiqiang
 * @date 2021/9/9 14:53
 */
@Api(tags = "AXB 绑定关系操作")
@RestController
@RequestMapping(SystemConstant.BIND_URI + "axb")
public class NumberBindAxbController {

    private final AxbBindService axbBindService;

    @Autowired
    public NumberBindAxbController(AxbBindService axbBindService) {
        this.axbBindService = axbBindService;
    }


    /**
     * AXB模式码号绑定
     *
     * @param bindingDTO 入参 AxbBindingDTO
     * @param vccId      企业id
     * @return Result
     */
    @ApiOperation("AXB模式码号绑定")
    @PostMapping("binding/{vccId}")
    public Result binding(@RequestBody @Validated AxbBindingDTO bindingDTO,
                          @PathVariable("vccId") String vccId,
                          @RequestHeader(value = "supplier_id", required = false) String supplierId) {
        bindingDTO.setVccId(vccId);
        return axbBindService.binding(bindingDTO, supplierId);
    }

    @ApiOperation("AXB模式码号解绑")
    @PostMapping("unbind/{vccId}")
    public Result unbind(@RequestBody @Validated UnBindDTO unBindDTO,
                         @PathVariable("vccId") String vccId,
                         @RequestHeader(value = "supplier_id", required = false) String supplierId) {
        unBindDTO.setVccId(vccId);
        return axbBindService.unbind(unBindDTO, supplierId);
    }

    @ApiOperation("AXB模式码号更新有效期")
    @PostMapping("updateExpiration/{vccId}")
    public Result updateExpirationBind(@RequestBody @Validated UpdateExpirationDTO updateExpirationDTO,
                                       @PathVariable("vccId") String vccId,
                                       @RequestHeader(value = "supplier_id", required = false) String supplierId) {
        updateExpirationDTO.setVccId(vccId);
        return axbBindService.updateExpirationBind(updateExpirationDTO, supplierId);
    }
}
