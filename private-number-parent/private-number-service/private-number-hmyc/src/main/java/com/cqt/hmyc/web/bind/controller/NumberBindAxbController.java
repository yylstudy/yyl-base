package com.cqt.hmyc.web.bind.controller;

import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.hmyc.config.exception.AuthException;
import com.cqt.hmyc.web.bind.adapter.BindingAdapter;
import com.cqt.hmyc.web.bind.service.axb.AxbBindService;
import com.cqt.hmyc.web.corpinfo.service.CorpBusinessService;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.bind.dto.UpdateTelBindDTO;
import com.cqt.model.common.Result;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2021/9/9 14:53
 */
@Api(tags = "AXB 绑定关系操作")
@RestController
@RequestMapping(SystemConstant.BIND_URI + "axb")
public class NumberBindAxbController {

    private final static String TYPE = BusinessTypeEnum.AXB.name();

    private final AxbBindService axbBindService;

    private final CorpBusinessService corpBusinessService;

    private final BindingAdapter<AxbBindingDTO> bindingAdapter;

    public NumberBindAxbController(AxbBindService axbBindService, CorpBusinessService corpBusinessService, BindingAdapter<AxbBindingDTO> bindingAdapter) {
        this.axbBindService = axbBindService;
        this.corpBusinessService = corpBusinessService;
        this.bindingAdapter = bindingAdapter;
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
    public Result binding(@RequestBody @Validated AxbBindingDTO bindingDTO, @PathVariable("vccId") String vccId) {
        bindingDTO.setVccId(vccId);
        return axbBindService.bindingAxb(bindingDTO);
    }

    @ApiOperation("AXB模式码号绑定bindingAdapter")
    @PostMapping("bindingAdapter/{vccId}")
    public Result bindingAdapter(@RequestBody Map<String, Object> params, @PathVariable("vccId") String vccId) {

        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = corpBusinessService.getCorpBusinessInfo(vccId);
        if (!businessInfoOptional.isPresent()) {
            throw new AuthException("appkey 不存在!");
        }
        PrivateCorpBusinessInfoDTO privateCorpBusinessInfoDTO = businessInfoOptional.get();
        Optional<Map<String, String>> adapterMapOptional = corpBusinessService.getAdapterMap(privateCorpBusinessInfoDTO, TYPE);
        AxbBindingDTO bindingDTO = bindingAdapter.change(params, adapterMapOptional, AxbBindingDTO.class);
        bindingDTO.setVccId(vccId);
        return axbBindService.bindingAxb(bindingDTO);
    }

    @ApiOperation("AXB模式码号解绑")
    @PostMapping("unbind/{vccId}")
    public Result unbind(@RequestBody @Validated UnBindDTO unBindDTO, @PathVariable("vccId") String vccId) {
        unBindDTO.setVccId(vccId);
        return axbBindService.unbind(unBindDTO);
    }

    @ApiOperation("AXB模式码号更新有效期")
    @PostMapping("updateExpiration/{vccId}")
    public Result updateExpirationBind(@RequestBody @Validated UpdateExpirationDTO updateExpirationDTO, @PathVariable("vccId") String vccId) {
        updateExpirationDTO.setVccId(vccId);
        return axbBindService.updateExpirationBind(updateExpirationDTO);
    }

    @ApiOperation("AXB模式更新已绑定号码")
    @PostMapping("updateTelBinded/{vccId}")
    public Result updateTelBind(@RequestBody @Validated UpdateTelBindDTO updateTelBindDTO, @PathVariable("vccId") String vccId) {
        updateTelBindDTO.setVccId(vccId);
        return axbBindService.updateTelBind(updateTelBindDTO);
    }
}
