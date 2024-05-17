package com.cqt.hmyc.web.bind.controller;

import com.cqt.common.constants.SystemConstant;
import com.cqt.hmyc.web.bind.service.ax.AxeBindService;
import com.cqt.model.bind.ax.dto.AxBindingDTO;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axe.dto.AxeBindingDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author huweizhong
 * date  2023/12/8 10:01
 */
@Api(tags = "AX 绑定关系操作")
@RestController
@RequiredArgsConstructor
@RequestMapping(SystemConstant.BIND_URI + "axe")
public class NumberBindAxeController {

    private final AxeBindService axBindService;

    /**
     * AX模式码号绑定
     *
     * @param bindingDTO 入参 AxBindingDTO
     * @param vccId      企业id
     * @return Result
     */
    @ApiOperation("AXE模式码号绑定")
    @PostMapping("binding/{vccId}")
    public Result binding(@RequestBody @Validated AxeBindingDTO bindingDTO,
                          @PathVariable("vccId") String vccId,
                          @RequestHeader(value = "supplier_id", required = false) String supplierId) {
        bindingDTO.setVccId(vccId);
        return axBindService.binding(bindingDTO, supplierId);
    }

    @ApiOperation("AXE模式码号解绑")
    @PostMapping("unbind/{vccId}")
    public Result unbind(@RequestBody @Validated UnBindDTO unBindDTO,
                         @PathVariable("vccId") String vccId,
                         @RequestHeader(value = "supplier_id", required = false) String supplierId) {
        unBindDTO.setVccId(vccId);
        return axBindService.unbind(unBindDTO, supplierId);
    }

    @ApiOperation("AXE模式码号更新有效期")
    @PostMapping("updateExpiration/{vccId}")
    public Result updateExpirationBind(@RequestBody @Validated UpdateExpirationDTO updateExpirationDTO,
                                       @PathVariable("vccId") String vccId,
                                       @RequestHeader(value = "supplier_id", required = false) String supplierId) {
        updateExpirationDTO.setVccId(vccId);
        return axBindService.updateAxeExpirationBind(updateExpirationDTO, supplierId);
    }
}
