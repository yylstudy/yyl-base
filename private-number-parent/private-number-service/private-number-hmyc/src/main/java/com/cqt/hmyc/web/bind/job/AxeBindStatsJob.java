package com.cqt.hmyc.web.bind.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.service.axe.AxeBindService;
import com.cqt.hmyc.web.corpinfo.mapper.PrivateCorpBusinessInfoMapper;
import com.cqt.model.bind.axe.dto.AxeUtilizationDTO;
import com.cqt.model.bind.axe.vo.AxeUtilizationVO;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.redis.util.RedissonUtil;
import com.cqt.xxljob.annotations.XxlJobRegister;
import com.cqt.xxljob.enums.ExecutorRouteStrategyEnum;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-08-29 10:58
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AxeBindStatsJob {

    private final RedissonUtil redissonUtil;

    private final AxeBindService axeBindService;

    private final PrivateCorpBusinessInfoMapper privateCorpBusinessInfoMapper;

    @XxlJobRegister(jobDesc = "AXE按地市绑定次数统计并写入redis",
            cron = "0 10 6 * * ?",
            triggerStatus = 1,
            executorRouteStrategy = ExecutorRouteStrategyEnum.CONSISTENT_HASH)
    @XxlJob("axeBindStatsSaveRedisJob")
    public void axeBindStatsSaveRedisJob() {
        List<String> vccIdList = getVccIdList();
        for (String vccId : vccIdList) {
            try {
                List<AxeUtilizationVO> list = axeBindService.utilizationFromDb(new AxeUtilizationDTO(), vccId);
                if (CollUtil.isEmpty(list)) {
                    continue;
                }
                for (AxeUtilizationVO axeUtilizationVO : list) {
                    String axeBindStatsKey = PrivateCacheUtil.getAxeBindStatsKey(vccId, axeUtilizationVO.getAreaCode());
                    redissonUtil.setObject(axeBindStatsKey, axeUtilizationVO.getUsedCount());
                }
            } catch (Exception e) {
                log.error(" vccId:{} axeBindStatsSaveRedisJob err: ", vccId, e);
            }
        }
    }

    /**
     * 获取开通此业务模式的企业id
     */
    private List<String> getVccIdList() {
        LambdaQueryWrapper<PrivateCorpBusinessInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(PrivateCorpBusinessInfo::getVccId, PrivateCorpBusinessInfo::getBusinessType);
        List<PrivateCorpBusinessInfo> list = privateCorpBusinessInfoMapper.selectList(queryWrapper);
        return list.stream()
                .filter(item -> StrUtil.isNotEmpty(item.getBusinessType()))
                .filter(item -> ReUtil.isMatch(item.getBusinessType(), BusinessTypeEnum.AXE.name()))
                .map(PrivateCorpBusinessInfo::getVccId)
                .collect(Collectors.toList());
    }
}
