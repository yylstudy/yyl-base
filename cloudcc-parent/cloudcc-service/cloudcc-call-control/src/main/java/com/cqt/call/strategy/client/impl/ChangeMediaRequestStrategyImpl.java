package com.cqt.call.strategy.client.impl;

import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.call.converter.ModelConverter;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientChangeMediaDTO;
import com.cqt.model.client.vo.ClientChangeMediaVO;
import com.cqt.model.freeswitch.dto.api.MediaToggleDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 音视频切换
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeMediaRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    private final DataQueryService dataQueryService;

    private final DataStoreService dataStoreService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.change_media;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientChangeMediaDTO changeMediaDTO = convert(requestBody, ClientChangeMediaDTO.class);
        String companyCode = changeMediaDTO.getCompanyCode();
        String uuid = changeMediaDTO.getUuid();
        // 判断下是否处于通话状态
        CheckAgentAvailableVO checkAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.CALLING,
                Lists.newArrayList(AgentStatusEnum.CALLING),
                changeMediaDTO.getCompanyCode(), changeMediaDTO.getExtId(), changeMediaDTO.getAgentId());
        if (!checkAgentAvailableVO.getAvailable()) {
            return ClientResponseBaseVO.response(changeMediaDTO, "1", checkAgentAvailableVO.getMessage());
        }
        MediaToggleDTO mediaToggleDTO = ModelConverter.INSTANCE.client2fs(changeMediaDTO);
        FreeswitchApiVO freeswitchApiVO = freeswitchRequestService.mediaToggle(mediaToggleDTO);
        // 修改上下文
        CallUuidContext callUuidContext = dataQueryService.getCallUuidContext(companyCode, uuid);
        callUuidContext.getCurrent().setAudio(changeMediaDTO.getAudio());
        callUuidContext.getCurrent().setVideo(changeMediaDTO.getAudio());
        callUuidContext.getCurrent().setChangeMediaFlag(true);
        dataStoreService.saveCallUuidContext(callUuidContext);
        
        if (freeswitchApiVO.getResult()) {
            return ClientChangeMediaVO.response(changeMediaDTO, "0", "发起音视频切换成功!");
        }
        return ClientChangeMediaVO.response(changeMediaDTO, "1", "发起音视频切换失败!");
    }

}
