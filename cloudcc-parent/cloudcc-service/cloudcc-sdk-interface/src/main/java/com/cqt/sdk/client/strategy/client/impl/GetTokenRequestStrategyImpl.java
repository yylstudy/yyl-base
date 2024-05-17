package com.cqt.sdk.client.strategy.client.impl;

import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.base.enums.SdkErrCode;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientGetTokenDTO;
import com.cqt.model.client.validategroup.AgentIdGroup;
import com.cqt.model.client.validategroup.OsGroup;
import com.cqt.model.client.vo.ClientGetTokenVO;
import com.cqt.sdk.client.strategy.client.ClientRequestStrategy;
import com.cqt.sdk.client.util.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 获取token
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetTokenRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final AESUtil aesUtil;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.get_token;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientGetTokenDTO getTokenDTO = convert(requestBody, ClientGetTokenDTO.class, OsGroup.class, AgentIdGroup.class);
        String companyCode = getTokenDTO.getCompanyCode();
        String agentId = getTokenDTO.getAgentId();
        String os = getTokenDTO.getOs();
        AgentInfo agentInfo = commonDataOperateService.getAgentInfo(getTokenDTO.getCompanyCode(), getTokenDTO.getAgentId());
        if (Objects.isNull(agentInfo)) {
            return ClientResponseBaseVO.fail(SdkErrCode.AGENT_NOT_EXIST);
        }
        // check password
        String encryptUserPwd = aesUtil.encrypt(getTokenDTO.getAgentPwd());
        if (!encryptUserPwd.equals(agentInfo.getPassword())) {
            return ClientResponseBaseVO.response(getTokenDTO, SdkErrCode.AGENT_PWD_ERROR);
        }

        // create token
        if (agentInfo.getState() == 1) {
            String token = commonDataOperateService.createToken(companyCode, agentId, os);
            // set cache
            commonDataOperateService.saveToken(companyCode, agentId, os, token);
            return ClientGetTokenVO.response(getTokenDTO, token, SdkErrCode.OK);
        }

        return ClientResponseBaseVO.response(getTokenDTO, SdkErrCode.GET_TOKEN_FAIL);
    }

}
