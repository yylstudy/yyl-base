package com.cqt.call.strategy.event.callstatus.impl;

import com.cqt.base.enums.CallStatusEventEnum;
import com.cqt.base.enums.MediaStreamEnum;
import com.cqt.call.config.ThreadPoolConfig;
import com.cqt.call.service.DataStoreService;
import com.cqt.call.strategy.event.callstatus.CallStatusStrategy;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.client.vo.ClientCallbackVO;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-07-03 13:37
 * 音视频切换事件
 * <pre>
 *     {
 *     "event":"call_status",
 *     "server_id":"127.0.0.1:FS01",
 *     "company_code":"900001",
 *     "service_code":"00001",
 *     "timestamp":1686195600000,
 *     "uuid":"37a160c4-0413-11ee-aa9e-174c4c2dfe01",
 *     "data":{
 *         "caller_number":"003096_60001",
 *         "callee_number":"180605883468",
 *         "media_reneg_request": true,
 *         "last_audio":"",
 *         "last_video":"NONE",
 *         "audio":true,
 *         "video":true,
 *         "status":"MEDIARENEG"
 *     }
 * }
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaToggleCallStatusStrategyImpl implements CallStatusStrategy {

    private final CommonDataOperateService commonDataOperateService;

    private final DataStoreService dataStoreService;

    @Override
    public CallStatusEventEnum getCallStatus() {
        return CallStatusEventEnum.MEDIARENEG;
    }

    @Async(ThreadPoolConfig.BASE_POOL_NAME)
    @Override
    public void deal(CallStatusEventDTO callStatusEventDTO) {
        String companyCode = callStatusEventDTO.getCompanyCode();
        String uuid = callStatusEventDTO.getUuid();
        CallStatusEventDTO.EventData eventData = callStatusEventDTO.getData();
        Boolean mediaRenegRequest = eventData.getMediaRenegRequest();
        CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, uuid);
        Boolean changeMediaFlag = callUuidContext.getChangeMediaFlag();
        if (Boolean.TRUE.equals(mediaRenegRequest)) {
            if (Boolean.TRUE.equals(changeMediaFlag)) {
                // 当前为发起音视频切换的
                ClientCallbackVO clientCallbackVO = ClientCallbackVO.buildChangeMedia(callUuidContext,
                        callStatusEventDTO,
                        0,
                        "坐席发起音视频切换请求");
                dataStoreService.notifyClient(clientCallbackVO);
                writeCallUuidContext(callUuidContext, callStatusEventDTO);
            } else {
                String relationUUID = callUuidContext.findRelationUUID();
                CallUuidContext relationCallUuidContext = commonDataOperateService.getCallUuidContext(companyCode, relationUUID);
                ClientCallbackVO clientCallbackVO = ClientCallbackVO.buildChangeMedia(relationCallUuidContext,
                        callStatusEventDTO,
                        1,
                        "坐席发起音视频切换请求, 对方接通情况");
                dataStoreService.notifyClient(clientCallbackVO);
                writeCallUuidContext(callUuidContext, callStatusEventDTO);
            }
            return;
        }
        String relationUUID = callUuidContext.findRelationUUID();
        CallUuidContext relationCallUuidContext = commonDataOperateService.getCallUuidContext(companyCode, relationUUID);
        ClientCallbackVO clientCallbackVO = ClientCallbackVO.buildChangeMedia(relationCallUuidContext,
                callStatusEventDTO,
                2,
                "音视频切换应答");
        dataStoreService.notifyClient(clientCallbackVO);
        writeCallUuidContext(callUuidContext, callStatusEventDTO);
    }

    private void writeCallUuidContext(CallUuidContext callUuidContext, CallStatusEventDTO callStatusEventDTO) {
        CallStatusEventDTO.EventData eventData = callStatusEventDTO.getData();
        callUuidContext.getCurrent().setChangeMediaFlag(null);
        callUuidContext.getCurrent().setVideo(MediaStreamEnum.valueOf(eventData.getVideo()).getCode());
        callUuidContext.getCurrent().setAudio(MediaStreamEnum.valueOf(eventData.getAudio()).getCode());
        commonDataOperateService.saveCallUuidContext(callUuidContext);
    }

}
