package com.cqt.broadnet.web.bind.controller;

import com.cqt.broadnet.web.bind.service.impl.NumberBindAxbService;
import com.cqt.common.constants.SystemConstant;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.cqt.common.constants.GatewayConstant.SUPPLIER_ID;

/**
 * @author Xienx
 * @date 2023-05-25
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(SystemConstant.BIND_URI + "axb")
@Api(tags = "广电AXB绑定接口API")
public class NumberBindAxbController {

    private final NumberBindAxbService bindAxbService;

    @ApiOperation(value = "请求广电进行AXB绑定")
    @PostMapping("/binding/{vccId}")
    public Result binding(@Validated @RequestBody AxbBindingDTO bindingDTO,
                          @PathVariable("vccId") String vccId,
                          @RequestHeader(value = SUPPLIER_ID, required = false) String supplierId) {
        bindingDTO.setVccId(vccId);
        return bindAxbService.binding(bindingDTO, supplierId);
    }

    @ApiOperation(value = "请求广电进行AXB解绑")
    @PostMapping("/unbind/{vccId}")
    public Result unBind(@RequestBody @Validated UnBindDTO unBindDTO,
                         @PathVariable("vccId") String vccId,
                         @RequestHeader(value = SUPPLIER_ID, required = false) String supplierId) {
        unBindDTO.setVccId(vccId);
        return bindAxbService.unbind(unBindDTO, supplierId);
    }

    @ApiOperation(value = "请求广电延长绑定有效期")
    @PostMapping("/updateExpiration/{vccId}")
    public Result updateExpirationBind(@RequestBody @Validated UpdateExpirationDTO updateExpirationDTO,
                                       @PathVariable("vccId") String vccId,
                                       @RequestHeader(value = SUPPLIER_ID, required = false) String supplierId) {
        updateExpirationDTO.setVccId(vccId);
        return bindAxbService.updateExpirationBind(updateExpirationDTO, supplierId);
    }
}
