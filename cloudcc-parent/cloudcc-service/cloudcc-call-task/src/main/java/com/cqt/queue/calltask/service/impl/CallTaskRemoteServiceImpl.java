package com.cqt.queue.calltask.service.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.enums.calltask.CallTaskEnum;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.mapper.task.IvrOutboundTaskNumberMapper;
import com.cqt.mapper.task.PredictOutboundTaskNumberMapper;
import com.cqt.model.calltask.entity.IvrOutboundTaskNumber;
import com.cqt.model.calltask.entity.PredictOutboundTaskNumber;
import com.cqt.rpc.queue.CallTaskRemoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-11-23 16:46
 */
@Slf4j
@DubboService
@Service
@RequiredArgsConstructor
public class CallTaskRemoteServiceImpl implements CallTaskRemoteService {

    private final CommonDataOperateService commonDataOperateService;

    private final IvrOutboundTaskNumberMapper ivrOutboundTaskNumberMapper;

    private final PredictOutboundTaskNumberMapper predictOutboundTaskNumberMapper;

    @Override
    public void answerIvrNotice(String companyCode, String taskId, String member) {
        List<String> list = StrUtil.split(member, StrUtil.COLON);
        String numberId = list.get(0);
        commonDataOperateService.removeNumber(taskId, member, CallTaskEnum.IVR);
        log.info("[answerIvrNotice] 企业: {}, taskId: {}, number: {}", companyCode, taskId, member);
        // 修改号码表接通
        IvrOutboundTaskNumber ivrOutboundTaskNumber = new IvrOutboundTaskNumber();
        ivrOutboundTaskNumber.setNumberId(numberId);
        ivrOutboundTaskNumber.setAnswerStatus(1);
        ivrOutboundTaskNumberMapper.updateById(ivrOutboundTaskNumber);
    }

    @Override
    public void answerPredictNotice(String companyCode, String taskId, String member) {
        List<String> list = StrUtil.split(member, StrUtil.COLON);
        String numberId = list.get(0);
        commonDataOperateService.removeNumber(taskId, member, CallTaskEnum.PREDICT_TASK);
        log.info("[answerPredictNotice] 企业: {}, taskId: {}, number: {}", companyCode, taskId, member);
        // 修改号码表接通
        PredictOutboundTaskNumber predictOutboundTaskNumber = new PredictOutboundTaskNumber();
        predictOutboundTaskNumber.setNumberId(numberId);
        predictOutboundTaskNumber.setAnswerStatus(1);
        predictOutboundTaskNumberMapper.updateById(predictOutboundTaskNumber);
    }
}
