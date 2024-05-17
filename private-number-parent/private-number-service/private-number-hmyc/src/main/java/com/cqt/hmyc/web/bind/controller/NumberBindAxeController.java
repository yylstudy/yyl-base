package com.cqt.hmyc.web.bind.controller;

import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.util.TraceIdUtil;
import com.cqt.hmyc.config.exception.AuthException;
import com.cqt.hmyc.web.bind.service.axe.AxeBindService;
import com.cqt.hmyc.web.corpinfo.service.CorpBusinessService;
import com.cqt.model.bind.axe.dto.AxeBindingDTO;
import com.cqt.model.bind.axe.dto.AxeUtilizationDTO;
import com.cqt.model.bind.axe.vo.AxeBindingVO;
import com.cqt.model.bind.axe.vo.AxeUtilizationVO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;
import com.cqt.model.common.ResultVO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2021/9/9 14:53
 */
@Api(tags = "AXE 绑定关系操作")
@RestController
@RequestMapping(SystemConstant.BIND_URI + "axe")
@RequiredArgsConstructor
public class NumberBindAxeController {

    private final static String TYPE = BusinessTypeEnum.AXE.name();

    private final AxeBindService axeBindService;

    private final CorpBusinessService corpBusinessService;

    /**
     * AXE 模式码号绑定
     *
     * @param bindingDTO 入参 AxeBindingDTO
     * @param vccId      企业id
     * @return Result
     */
    @ApiOperation("AXE 模式码号绑定")
    @PostMapping("binding/{vccId}")
    public ResultVO<AxeBindingVO> binding(@RequestBody @Validated AxeBindingDTO bindingDTO, @PathVariable("vccId") String vccId) {
        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = corpBusinessService.getCorpBusinessInfo(vccId);
        if (!businessInfoOptional.isPresent()) {
            throw new AuthException("appkey 不存在!");
        }
        bindingDTO.setVccId(vccId);
        try {
            String traceId = TraceIdUtil.buildTraceId(vccId, bindingDTO.getAreaCode(), bindingDTO.getRequestId());
            TraceIdUtil.setTraceId(traceId);
            return axeBindService.binding(bindingDTO, businessInfoOptional.get(), TYPE);
        } finally {
            TraceIdUtil.remove();
        }
    }

    @ApiOperation("AXE 模式码号解绑")
    @PostMapping("unbind/{vccId}")
    public Result unbind(@RequestBody @Validated UnBindDTO unBindDTO, @PathVariable("vccId") String vccId) {
        unBindDTO.setVccId(vccId);
        return axeBindService.unbind(unBindDTO, TYPE);
    }

    @ApiOperation("AXE 模式码号更新有效期")
    @PostMapping("updateExpiration/{vccId}")
    public Result updateExpirationBind(@RequestBody @Validated UpdateExpirationDTO updateExpirationDTO, @PathVariable("vccId") String vccId) {
        updateExpirationDTO.setVccId(vccId);
        return axeBindService.updateExpirationBind(updateExpirationDTO, TYPE);
    }

    @ApiOperation("AXE分机号余量查询")
    @PostMapping("pool/utilization/{vccId}")
    public ResultVO<List<AxeUtilizationVO>> utilization(@RequestBody AxeUtilizationDTO utilizationDTO,
                                                        @PathVariable("vccId") String vccId) {
        return axeBindService.utilization(utilizationDTO, vccId);
    }
}
