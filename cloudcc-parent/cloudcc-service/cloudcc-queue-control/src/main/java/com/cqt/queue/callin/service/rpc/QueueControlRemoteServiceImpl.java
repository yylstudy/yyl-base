package com.cqt.queue.callin.service.rpc;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.model.ResultVO;
import com.cqt.base.util.CacheUtil;
import com.cqt.base.util.TraceIdUtil;
import com.cqt.cloudcc.manager.context.CompanyInfoContext;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.freeswitch.dto.api.PlaybackDTO;
import com.cqt.model.queue.dto.CallInIvrActionDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import com.cqt.model.queue.vo.CallInIvrActionVO;
import com.cqt.model.skill.entity.SkillInfo;
import com.cqt.queue.callin.service.DataStoreService;
import com.cqt.queue.callin.service.idle.IdleStrategyFactory;
import com.cqt.rpc.queue.QueueControlRemoteService;
import com.cqt.starter.redis.util.RedissonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-07-18 16:20
 */
@Slf4j
@DubboService
@Service
@RequiredArgsConstructor
public class QueueControlRemoteServiceImpl implements QueueControlRemoteService {

    private final CommonDataOperateService commonDataOperateService;

    private final FreeswitchRequestService freeswitchRequestService;

    private final IdleStrategyFactory idleStrategyFactory;

    private final DataStoreService dataStoreService;

    private final ObjectMapper objectMapper;

    private final RedissonUtil redissonUtil;

    @Override
    public ResultVO<CallInIvrActionVO> distributeAgent(CallInIvrActionDTO callInIvrActionDTO) throws Exception {
        boolean trace = true;
        try {
            String companyCode = callInIvrActionDTO.getCompanyCode();
            String skillId = callInIvrActionDTO.getSkillId();
            String callerNumber = callInIvrActionDTO.getCallerNumber();
            String uuid = callInIvrActionDTO.getUuid();
            if (StrUtil.isEmpty(TraceIdUtil.getTraceId())) {
                trace = false;
                TraceIdUtil.setTraceId(TraceIdUtil.buildTraceId(companyCode, callerNumber, skillId, uuid));
            }
            CompanyInfoContext.set(commonDataOperateService.getCompanyInfoDTO(companyCode));
            return execute(callInIvrActionDTO);
        } finally {
            CompanyInfoContext.remove();
            if (trace) {
                TraceIdUtil.remove();
            }
        }
    }

    private ResultVO<CallInIvrActionVO> execute(CallInIvrActionDTO callInIvrActionDTO) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("[呼入ivr转技能] 参数: {}", objectMapper.writeValueAsString(callInIvrActionDTO));
        }
        // 查询技能配置
        SkillInfo skillInfo = commonDataOperateService.getSkillInfo(callInIvrActionDTO.getSkillId());
        if (Objects.isNull(skillInfo)) {
            return ResultVO.fail("人工服务配置不存在!");
        }

        String companyCode = callInIvrActionDTO.getCompanyCode();
        // 技能等待音和排队时间设置
        initIvrParams(callInIvrActionDTO, skillInfo);

        // 等待放音, 分配坐席池桥接结束放音
        playWaitTone(callInIvrActionDTO);

        // 用户排队信息
        // 查询来电uuid信息
        CallUuidContext callUuidContext = commonDataOperateService.getCallUuidContext(companyCode, callInIvrActionDTO.getUuid());
        if (Objects.isNull(callUuidContext)) {
            return ResultVO.fail("uuid不存在!");
        }
        UserQueueUpDTO userQueueUpDTO = UserQueueUpDTO.build(callInIvrActionDTO);
        userQueueUpDTO.setQueueStrategy(skillInfo.getQueueStrategy());
        userQueueUpDTO.setPriorityDatasource(skillInfo.getPriorityDatasource());
        userQueueUpDTO.setPriorityEnable(skillInfo.getPriorityEnable());
        userQueueUpDTO.setIdleStrategy(skillInfo.getIdleStrategy());
        userQueueUpDTO.setAudio(callUuidContext.getAudio());
        userQueueUpDTO.setVideo(callUuidContext.getVideo());
        saveUserQueueInfo(callUuidContext, userQueueUpDTO);

        String skillId = userQueueUpDTO.getSkillId();
        String userQueueIdleKey = CacheUtil.getUserQueueIdleKey(companyCode, skillId);
        redissonUtil.offerBlockList(userQueueIdleKey, objectMapper.writeValueAsString(userQueueUpDTO));
        return ResultVO.ok("进入闲时队列!");
    }

    private void saveUserQueueInfo(CallUuidContext callUuidContext, UserQueueUpDTO userQueueUpDTO) {
        if (Objects.isNull(callUuidContext.getUserQueueUpDTO())) {
            callUuidContext.setUserQueueUpDTO(userQueueUpDTO);
            commonDataOperateService.saveCallUuidContext(callUuidContext);
        }
    }

    /**
     * 等待放音, 分配坐席池桥接结束放音
     *
     * @param callInIvrActionDTO ivr参数
     */
    private void playWaitTone(CallInIvrActionDTO callInIvrActionDTO) {
        if (StrUtil.isEmpty(callInIvrActionDTO.getSkillWaitTone())) {
            log.info("[呼入转技能] 企业: {}, 技能: {}, 未找到等待音",
                    callInIvrActionDTO.getCompanyCode(), callInIvrActionDTO.getSkillId());
            return;
        }
        PlaybackDTO playbackDTO = PlaybackDTO.buildWaitPlayback(callInIvrActionDTO);
        freeswitchRequestService.playback(playbackDTO);
    }

    /**
     * 初始化ivr参数
     *
     * @param callInIvrActionDTO ivr参数
     */
    private void initIvrParams(CallInIvrActionDTO callInIvrActionDTO, SkillInfo skillInfo) {
        long timestamp = System.currentTimeMillis();
        callInIvrActionDTO.setTimestamp(timestamp);
        if (callInIvrActionDTO.getCurrentTimes() == 1) {
            callInIvrActionDTO.setFirstTimestamp(timestamp);
        }
        // 排队等待时间
        callInIvrActionDTO.setTimeout(skillInfo.getQueueTimeout());
        // 等待音
        callInIvrActionDTO.setFileId(skillInfo.getWaitingTone());
        String companyCode = callInIvrActionDTO.getCompanyCode();
        String fileId = callInIvrActionDTO.getFileId();
        callInIvrActionDTO.setSkillWaitTone(commonDataOperateService.getFilePath(companyCode, fileId));
    }

    @Override
    public Boolean addUserLevelQueue(UserQueueUpDTO userQueueUpDTO) {
        return dataStoreService.addUserLevelQueue(userQueueUpDTO);
    }

    @Override
    public boolean removeQueueUp(UserQueueUpDTO userQueueUpDTO) {
        return dataStoreService.removeQueueUp(userQueueUpDTO);
    }
}
