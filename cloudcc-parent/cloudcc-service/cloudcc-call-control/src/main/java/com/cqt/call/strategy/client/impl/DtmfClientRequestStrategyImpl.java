package com.cqt.call.strategy.client.impl;

import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientDtmfDTO;
import com.cqt.model.freeswitch.dto.api.SendDtmfDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 二次拨号（DTMF）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DtmfClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.dtmf;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientDtmfDTO clientDtmfDTO = convert(requestBody, ClientDtmfDTO.class);
        String companyCode = clientDtmfDTO.getCompanyCode();
        String uuid = clientDtmfDTO.getUuid();
        CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
        SendDtmfDTO sendDtmfDTO = SendDtmfDTO.build(clientDtmfDTO);
        sendDtmfDTO.setUuid(callUuidContext.findRelationUUID());
        FreeswitchApiVO apiVO = freeswitchRequestService.sendDtmf(sendDtmfDTO);
        if (Objects.nonNull(apiVO) && apiVO.getResult()) {
            return ClientResponseBaseVO.response(clientDtmfDTO, "0", "二次拨号成功!", false);
        }
        return ClientResponseBaseVO.response(clientDtmfDTO, "1", "二次拨号失败!", false);
    }
}
