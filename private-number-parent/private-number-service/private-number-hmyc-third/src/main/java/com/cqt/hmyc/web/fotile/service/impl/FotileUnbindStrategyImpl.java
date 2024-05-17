package com.cqt.hmyc.web.fotile.service.impl;

import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbMapper;
import com.cqt.hmyc.web.bind.service.AxbBindConverter;
import com.cqt.hmyc.web.fotile.model.dto.BindOperationDTO;
import com.cqt.hmyc.web.fotile.model.vo.BindOperationResultVO;
import com.cqt.hmyc.web.fotile.service.FotileBindOperateStrategy;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/7/22 16:20
 * 解绑操作策略
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FotileUnbindStrategyImpl implements FotileBindOperateStrategy {

    public static final Integer OPERATION_TYPE = 2;

    private final PrivateBindInfoAxbMapper privateBindInfoAxbMapper;

    private final RedissonUtil redissonUtil;

    private final AxbBindConverter axbBindConverter;

    @Override
    public BindOperationResultVO deal(BindOperationDTO bindOperationDTO, String vccId) {
        String bindId = bindOperationDTO.getBindId();
        // 查询绑定信息
        PrivateBindInfoAxb privateBindInfoAxb;
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
            privateBindInfoAxb = privateBindInfoAxbMapper.selectById(bindId);
        }
        Optional<PrivateBindInfoAxb> infoAxbOptional = Optional.ofNullable(privateBindInfoAxb);
        if (!infoAxbOptional.isPresent()) {
            log.info("vccId: {}, bindId: {}, 绑定关系不存在, 不需要解绑!", vccId, bindId);
            return BindOperationResultVO.success("绑定关系不存在, 不需要解绑!");
        }

        // 删除数据库中的绑定关系
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
            int delete = privateBindInfoAxbMapper.deleteById(bindId);
            log.info("vccId: {}, bindId: {}, 解绑结果: {}", vccId, bindId, delete);
        }

        // 修改redis
        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, BusinessTypeEnum.AXB.name(), privateBindInfoAxb.getTelA(), privateBindInfoAxb.getTelX());
        String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, BusinessTypeEnum.AXB.name(), privateBindInfoAxb.getTelB(), privateBindInfoAxb.getTelX());
        boolean delAx = redissonUtil.delKey(axBindInfoKey);
        boolean delBx = redissonUtil.delKey(bxBindInfoKey);
        log.info("vccId: {}, bindId: {}, 解绑redis结果: AX: {}, BX: {}", vccId, bindId, delAx, delBx);

        return BindOperationResultVO.success("解绑成功");
    }

    @Override
    public Integer getOperationType() {
        return OPERATION_TYPE;
    }
}
