package com.cqt.hmyc.web.fotile.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.manager.PrivateMqProducer;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbMapper;
import com.cqt.hmyc.web.bind.service.AxbBindConverter;
import com.cqt.hmyc.web.fotile.model.dto.BindOperationDTO;
import com.cqt.hmyc.web.fotile.model.vo.BindOperationResultVO;
import com.cqt.hmyc.web.fotile.service.FotileBindOperateStrategy;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2022/7/22 16:20
 * 修改有效期操作策略
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FotileUpdateExpirationStrategyImpl implements FotileBindOperateStrategy {

    public static final Integer OPERATION_TYPE = 1;

    private final PrivateBindInfoAxbMapper privateBindInfoAxbMapper;

    private final RedissonUtil redissonUtil;

    private final AxbBindConverter axbBindConverter;

    private final PrivateMqProducer privateMqProducer;

    @Override
    public BindOperationResultVO deal(BindOperationDTO bindOperationDTO, String vccId) {
        String businessType = BusinessTypeEnum.AXB.name();
        String bindId = bindOperationDTO.getBindId();
        // 查询绑定信息
        PrivateBindInfoAxb privateBindInfoAxb;
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
            privateBindInfoAxb = privateBindInfoAxbMapper.selectById(bindId);
        }
        Optional<PrivateBindInfoAxb> infoAxbOptional = Optional.ofNullable(privateBindInfoAxb);
        if (!infoAxbOptional.isPresent()) {
            log.info("vccId: {}, bindId: {}, 绑定关系不存在, 不需要更新!", vccId, bindId);
            return BindOperationResultVO.success("绑定关系不存在, 不需要更新!");
        }

        // 有效期
        DateTime expireTime = DateUtil.parse(bindOperationDTO.getEffectTime(), DatePattern.PURE_DATETIME_PATTERN);
        long expiration = DateUtil.between(DateUtil.date(), expireTime, DateUnit.SECOND);
        if (expiration <= 0) {
            log.info("vccId: {}, bindId: {}, 有效期已过期, 不需要更新!", vccId, bindId);
            return BindOperationResultVO.success("有效期已过期, 不需要更新!");
        }
        privateBindInfoAxb.setExpiration(expiration);
        privateBindInfoAxb.setExpireTime(expireTime);
        String bindInfoJson = JSON.toJSONString(privateBindInfoAxb);

        // 更新数据库绑定信息
        PrivateBindInfoAxb bindInfoAxb = new PrivateBindInfoAxb();
        bindInfoAxb.setBindId(bindId);
        bindInfoAxb.setExpiration(expiration);
        bindInfoAxb.setExpireTime(expireTime);
        bindInfoAxb.setUpdateTime(DateUtil.date());
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
            int update = privateBindInfoAxbMapper.updateById(bindInfoAxb);
            log.info("vccId: {}, bindId: {}, 更新绑定关系结果: {}", vccId, bindId, update);
        }

        // 修改redis
        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, businessType, privateBindInfoAxb.getTelA(), privateBindInfoAxb.getTelX());
        String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, businessType, privateBindInfoAxb.getTelB(), privateBindInfoAxb.getTelX());
        boolean setAx = redissonUtil.setObject(axBindInfoKey, bindInfoJson, expiration, TimeUnit.SECONDS);
        boolean setBx = redissonUtil.setObject(bxBindInfoKey, bindInfoJson, expiration, TimeUnit.SECONDS);
        log.info("vccId: {}, update bind expire set ax: {}, set bx: {}", vccId, setAx, setBx);

        // 发mq
        BindRecycleDTO bindRecycleDTO = axbBindConverter.bindInfoAxb2BindRecycleDTO(privateBindInfoAxb);
        bindRecycleDTO.setNumType(businessType);
        if (log.isInfoEnabled()) {
            log.info("vccId: {}, send mq: {}", vccId, JSON.toJSONString(bindRecycleDTO));
        }
        privateMqProducer.sendLazy(Optional.of(bindRecycleDTO), (int) expiration);
        return BindOperationResultVO.success("更新成功");
    }

    @Override
    public Integer getOperationType() {
        return OPERATION_TYPE;
    }
}
