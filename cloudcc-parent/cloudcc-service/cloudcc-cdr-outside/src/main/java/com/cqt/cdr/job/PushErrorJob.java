package com.cqt.cdr.job;


import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqt.base.contants.CommonConstant;
import com.cqt.cdr.entity.PushErr;
import com.cqt.cdr.mapper.DictItemMapper;
import com.cqt.cdr.mapper.DictMapper;
import com.cqt.cdr.service.PushErrService;
import com.cqt.cdr.util.AccessRemote;
import com.cqt.feign.freeswitch.FreeswitchApiFeignClient;
import com.cqt.model.cdr.dto.RemoteQualityCdrDTO;
import com.cqt.model.cdr.entity.CdrDatapushPushEntity;
import com.cqt.model.cdr.entity.PushMisscallDataEntity;
import com.cqt.model.cdr.vo.RemoteCdrVO;
import com.cqt.mybatisplus.config.core.RequestDataHelper;
import com.cqt.xxljob.annotations.XxlJobRegister;
import com.cqt.xxljob.enums.ExecutorRouteStrategyEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class PushErrorJob {

    @Resource
    private PushErrService pushErrService;

    @Resource
    private AccessRemote accessRemote;


    /**
     * 10秒执行一次错误话单重试
     */
    @XxlJobRegister(jobDesc = "定时处理推送异常话单",
            cron = "0/10 * * * * ?",
            triggerStatus = 1,
            executorParam = "3",
            executorRouteStrategy = ExecutorRouteStrategyEnum.CONSISTENT_HASH)
    @XxlJob("handlePushError")
    public void handlePushError() {
        String LOG_TAG = UUID.randomUUID() + "| 处理cc_push_err表消息 |";
        String month = DateUtil.format(DateUtil.date(), CommonConstant.MONTH_FORMAT);
        RequestDataHelper.setRequestData(CommonConstant.MONTH_KEY, month);
        Page<PushErr> result = pushErrService.page(new Page<>(1, 5000), new QueryWrapper<PushErr>().lambda().lt(PushErr::getReqcount, 3).isNotNull(PushErr::getUrl));
        List<PushErr> data = result.getRecords();
        for (PushErr pushErr : data) {
            try {
                switch (pushErr.getType()) {
                    case "cdrpush":
                        handleTotal(LOG_TAG, pushErr);
                        break;
                    case "busy":
                        PushMisscallDataEntity pushMisscallDataEntity = new ObjectMapper().readValue(pushErr.getJson(), PushMisscallDataEntity.class);
                        handleQuality(LOG_TAG, pushErr, pushMisscallDataEntity);
                        break;
                    case "quality":
                    case "aftersafe":
                        RemoteQualityCdrDTO remoteQualityCdrDTO = new ObjectMapper().readValue(pushErr.getJson(), RemoteQualityCdrDTO.class);
                        handleQuality(LOG_TAG, pushErr, remoteQualityCdrDTO);
                        break;
                }
            } catch (JsonProcessingException e) {
                log.info(LOG_TAG + "数据：{}，解析异常", pushErr);
                // 发送失败 失败次数+1
                pushErrService.update(new UpdateWrapper<PushErr>().lambda().set(PushErr::getReqcount, pushErr.getReqcount() + 1).eq(PushErr::getId, pushErr.getId()));
            }
        }
    }


    /**
     * 处理质检消息
     *
     * @param LOG_TAG
     * @param pushErr
     * @throws JsonProcessingException
     */
    private <T> void handleQuality(String LOG_TAG, PushErr pushErr, T t) {
        RemoteCdrVO remoteCdrVO = accessRemote.sendQualityCdr(pushErr.getUrl(), t, LOG_TAG);
        // 发送成功删除消息
        if (remoteCdrVO != null && remoteCdrVO.getCode().equals("200")) {
            log.info(LOG_TAG + "发送成功，删除数据：{}", pushErr);
            pushErrService.removeById(pushErr);
            return;
        }
        // 发送失败 失败次数+1
        pushErrService.update(new UpdateWrapper<PushErr>().lambda().set(PushErr::getReqcount, pushErr.getReqcount() + 1).eq(PushErr::getId, pushErr.getId()));
    }

    /**
     * 处理全量消息
     *
     * @param LOG_TAG
     * @param pushErr
     */
    private void handleTotal(String LOG_TAG, PushErr pushErr) {
        try {
            // 解析异常
            List<CdrDatapushPushEntity> cdrDatapushPushEntitys = new ObjectMapper().readValue(pushErr.getJson(), new TypeReference<List<CdrDatapushPushEntity>>() {
            });
            RemoteCdrVO remoteCdrVO = accessRemote.sendCdr(pushErr.getUrl(), accessRemote.getRemoteCdrDTO(cdrDatapushPushEntitys), LOG_TAG);
            if (remoteCdrVO != null && remoteCdrVO.getCode().equals("200")) {
                log.info(LOG_TAG + "发送成功，删除数据：{}", pushErr);
                pushErrService.removeById(pushErr);
                return;
            }
            // 发送失败 失败次数+1
            pushErrService.update(new UpdateWrapper<PushErr>().lambda().set(PushErr::getReqcount, pushErr.getReqcount() + 1).eq(PushErr::getId, pushErr.getId()));
        } catch (Exception e) {
            log.error(LOG_TAG + "数据：{}，发送异常，失败次数+1：", pushErr, e);
            // 发送失败 失败次数+1
            pushErrService.update(new UpdateWrapper<PushErr>().lambda().set(PushErr::getReqcount, pushErr.getReqcount() + 1).eq(PushErr::getId, pushErr.getId()));
        }
    }
}
