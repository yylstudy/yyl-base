package com.cqt.client.controller;

import com.cqt.base.annotations.Auth;
import com.cqt.client.service.OpenApiRequestService;
import com.cqt.client.service.SdkLoggerService;
import com.cqt.model.agent.dto.AgentInfoEditDTO;
import com.cqt.model.agent.dto.SdkLoggerDTO;
import com.cqt.model.agent.dto.SkillAgentDTO;
import com.cqt.model.agent.vo.AgentInfoVO;
import com.cqt.model.agent.vo.SkillAgentVO;
import com.cqt.model.client.base.ClientBase;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientGetTokenDTO;
import com.cqt.model.client.vo.ClientRequestVO;
import com.cqt.model.queue.entity.IvrServiceInfo;
import com.cqt.model.skill.entity.SkillInfo;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-11-22 15:41
 * 话务, 坐席http接口请求入口
 */
@CrossOrigin
@RestController
@RequestMapping("openapi")
@RequiredArgsConstructor
public class OpenApiRequestController {

    private final OpenApiRequestService openApiRequestService;

    private final SdkLoggerService sdkLoggerService;

    @Auth
    @PostMapping("request")
    public ClientBase request(@RequestBody String requestBody) throws Exception {

        return openApiRequestService.request(requestBody);
    }

    @Auth
    @ApiOperation("获人工服务列表")
    @GetMapping("get-skill-service-list")
    public ClientRequestVO<List<SkillInfo>> getSkillServiceList(@RequestParam("company_code") String companyCode,
                                                                @RequestParam(name = "service_name", required = false) String serviceName) {
        return openApiRequestService.getSkillServiceList(companyCode, serviceName);
    }

    @Auth
    @ApiOperation("获取IVR服务列表")
    @GetMapping("get-ivr-service-list")
    public ClientRequestVO<List<IvrServiceInfo>> getIvrServiceList(@RequestParam("company_code") String companyCode,
                                                                   @RequestParam(name = "service_name", required = false) String serviceName) {
        return openApiRequestService.getIvrServiceList(companyCode, serviceName);
    }

    @Auth
    @ApiOperation("获取坐席配置")
    @GetMapping("get-agent-info")
    public ClientRequestVO<AgentInfoVO> getAgentInfo(@RequestParam("company_code") String companyCode,
                                                     @RequestParam("agent_id") String agentId) {
        return openApiRequestService.getAgentInfo(companyCode, agentId);
    }

    @Auth
    @ApiOperation("更新坐席配置")
    @PostMapping("update-agent-info")
    public ClientRequestVO<Void> updateAgentInfo(@RequestBody @Validated AgentInfoEditDTO agentInfoEditDTO) {
        return openApiRequestService.updateAgentInfo(agentInfoEditDTO);
    }

    @Auth
    @PostMapping("get-agent-list")
    @ApiOperation(value = "坐席及状态列表")
    public ClientRequestVO<List<SkillAgentVO>> getAgentList(@RequestBody SkillAgentDTO skillAgentDTO) {
        return openApiRequestService.getAgentList(skillAgentDTO);
    }

    @Auth
    @PostMapping("sdk-logger")
    @ApiOperation(value = "sdk记录请求日志")
    public ClientRequestVO<Void> logger(@RequestBody SdkLoggerDTO skillAgentDTO) {
        return sdkLoggerService.logger(skillAgentDTO);
    }

    @PostMapping("get-token")
    @ApiOperation(value = "获取token")
    public ClientResponseBaseVO getToken(@RequestBody ClientGetTokenDTO clientGetTokenDTO) throws Exception {
        return openApiRequestService.getToken(clientGetTokenDTO);
    }
}
