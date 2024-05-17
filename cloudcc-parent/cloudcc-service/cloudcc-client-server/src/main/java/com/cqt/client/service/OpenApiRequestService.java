package com.cqt.client.service;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONPath;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.client.config.nacos.CloudNettyProperties;
import com.cqt.model.agent.dto.AgentInfoEditDTO;
import com.cqt.model.agent.dto.SkillAgentDTO;
import com.cqt.model.agent.vo.AgentInfoVO;
import com.cqt.model.agent.vo.SkillAgentVO;
import com.cqt.model.client.base.ClientBase;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientGetTokenDTO;
import com.cqt.model.client.vo.ClientRequestVO;
import com.cqt.model.queue.entity.IvrServiceInfo;
import com.cqt.model.skill.entity.SkillInfo;
import com.cqt.rpc.call.CallControlRemoteService;
import com.cqt.rpc.sdk.SdkInterfaceRemoteService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-11-22 15:45
 */
@Service
public class OpenApiRequestService {

    @DubboReference
    private CallControlRemoteService callControlRemoteService;

    @DubboReference
    private SdkInterfaceRemoteService sdkInterfaceRemoteService;

    @Resource
    private CloudNettyProperties cloudNettyProperties;

    public ClientBase request(String requestBody) throws Exception {
        String msgType = Convert.toStr(JSONPath.read(requestBody, "$.msg_type"));
        if (StrUtil.isEmpty(msgType)) {
            return ClientResponseBaseVO.fail(SdkErrCode.PARAM_ERROR);
        }
        List<String> sdkType = cloudNettyProperties.getSdkType();

        if (sdkType.contains(msgType)) {
            return sdkInterfaceRemoteService.request(requestBody);
        }
        List<String> callType = cloudNettyProperties.getCallType();
        if (callType.contains(msgType)) {
            return callControlRemoteService.request(requestBody);
        }
        return ClientResponseBaseVO.fail(SdkErrCode.MSG_TYPE_INVALID);
    }

    public ClientRequestVO<List<SkillInfo>> getSkillServiceList(String companyCode, String serviceName) {
        return sdkInterfaceRemoteService.getSkillServiceList(companyCode, serviceName);
    }

    public ClientRequestVO<List<IvrServiceInfo>> getIvrServiceList(String companyCode, String serviceName) {
        return sdkInterfaceRemoteService.getIvrServiceList(companyCode, serviceName);
    }

    public ClientRequestVO<AgentInfoVO> getAgentInfo(String companyCode, String agentId) {
        return sdkInterfaceRemoteService.getAgentInfo(companyCode, agentId);
    }

    public ClientRequestVO<Void> updateAgentInfo(AgentInfoEditDTO agentInfoEditDTO) {
        return sdkInterfaceRemoteService.updateAgentInfo(agentInfoEditDTO);
    }

    public ClientRequestVO<List<SkillAgentVO>> getAgentList(SkillAgentDTO skillAgentDTO) {
        return sdkInterfaceRemoteService.getAgentList(skillAgentDTO);
    }

    public ClientResponseBaseVO getToken(ClientGetTokenDTO clientGetTokenDTO) throws Exception {
        return sdkInterfaceRemoteService.getToken(clientGetTokenDTO);
    }
}
