package com.cqt.sdk.client.controller;

import com.cqt.base.annotations.Auth;
import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.base.enums.agent.AgentStatusTransferActionEnum;
import com.cqt.model.client.base.ClientBase;
import com.cqt.model.client.dto.ClientChangeStatusDTO;
import com.cqt.model.client.dto.ClientGetTokenDTO;
import com.cqt.rpc.sdk.SdkInterfaceRemoteService;
import com.cqt.sdk.client.service.DataQueryService;
import com.cqt.sdk.client.strategy.client.impl.GetTokenRequestStrategyImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author linshiqiang
 * date:  2023-08-10 10:18
 */
@Api(tags = "SDK坐席状态操作接口")
@RestController
@RequestMapping("openapi")
@RequiredArgsConstructor
public class OpenApiController {

    private final SdkInterfaceRemoteService sdkInterfaceRemoteService;

    private final GetTokenRequestStrategyImpl getTokenRequestStrategyImpl;

    private final ObjectMapper objectMapper;

    private final DataQueryService dataQueryService;

    @Auth
    @ApiOperation("坐席状态操作入口")
    @PostMapping("agent-status-request")
    public ClientBase request(@RequestBody String requestBody) throws Exception {
        return sdkInterfaceRemoteService.request(requestBody);
    }

    /**
     * 强签单个坐席
     */
    @ApiOperation("强签单个坐席")
    @PostMapping("force-checkout")
    public ClientBase forceCheckout(@RequestBody @Validated ClientChangeStatusDTO clientChangeStatusDTO) throws Exception {
        clientChangeStatusDTO.setAction(AgentStatusTransferActionEnum.FORCE_CHECKOUT.name());
        clientChangeStatusDTO.setMsgType(MsgTypeEnum.change_status.name());
        return sdkInterfaceRemoteService.request(objectMapper.writeValueAsString(clientChangeStatusDTO));
    }

    @ApiOperation("获取token")
    @PostMapping("get-token")
    public ClientBase getToken(@RequestBody @Validated ClientGetTokenDTO clientGetTokenDTO) throws Exception {
        clientGetTokenDTO.setMsgType(MsgTypeEnum.get_token.name());
        return getTokenRequestStrategyImpl.deal(objectMapper.writeValueAsString(clientGetTokenDTO));
    }

    @Auth
    @ApiOperation("获人工服务列表")
    @GetMapping("get-skill-service-list")
    public ClientBase getSkillServiceList(@RequestParam("company_code") String companyCode,
                                          @RequestParam(name = "service_name", required = false) String serviceName) {
        return dataQueryService.getSkillServiceList(companyCode, serviceName);
    }

    @Auth
    @ApiOperation("获取IVR服务列表")
    @GetMapping("get-ivr-service-list")
    public ClientBase getIvrServiceList(@RequestParam("company_code") String companyCode,
                                        @RequestParam(name = "service_name", required = false) String serviceName) {
        return dataQueryService.getIvrServiceList(companyCode, serviceName);
    }
}
