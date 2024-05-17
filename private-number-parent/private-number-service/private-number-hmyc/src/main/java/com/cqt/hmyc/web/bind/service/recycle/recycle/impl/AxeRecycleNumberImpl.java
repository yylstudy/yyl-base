package com.cqt.hmyc.web.bind.service.recycle.recycle.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.NumberTypeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.event.AxeBindStatsEvent;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbMapper;
import com.cqt.hmyc.web.bind.mapper.axe.PrivateBindInfoAxeMapper;
import com.cqt.hmyc.web.bind.service.axb.AxbBindConverter;
import com.cqt.hmyc.web.bind.service.axe.AxeBindConverter;
import com.cqt.hmyc.web.bind.service.push.BindPushService;
import com.cqt.hmyc.web.bind.service.recycle.recycle.RecycleNumberStrategy;
import com.cqt.hmyc.web.cache.NumberPoolAxeCache;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/4/19 20:07
 * AXE 回收
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AxeRecycleNumberImpl implements RecycleNumberStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXE.name();

    private final RedissonUtil redissonUtil;

    private final HideProperties hideProperties;

    private final PrivateBindInfoAxeMapper privateBindInfoAxeMapper;

    private final PrivateBindInfoAxbMapper privateBindInfoAxbMapper;

    private final AxeBindConverter axeBindConverter;

    private final AxbBindConverter axbBindConverter;

    private final BindPushService bindPushService;

    private final AxbRecycleNumberImpl axbRecycleNumberImpl;

    private final ApplicationContext applicationContext;

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

    @Override
    public void recycle(BindRecycleDTO bindRecycleDTO) {
        try (HintManager hintManager = HintManager.getInstance()) {
            String numberHash = BindIdUtil.getHash(bindRecycleDTO.getTelX());
            String sharingKey = bindRecycleDTO.getVccId() + StrUtil.AT + numberHash;
            hintManager.addDatabaseShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, numberHash);
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, sharingKey);
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, bindRecycleDTO.getVccId());
            recycleAxe(bindRecycleDTO);
        }
    }

    /**
     * AXE 分机号回收
     */
    private void recycleAxe(BindRecycleDTO bindRecycleDTO) {
        String bindId = bindRecycleDTO.getBindId();
        String numType = bindRecycleDTO.getNumType();
        String requestId = bindRecycleDTO.getRequestId();
        String telX = bindRecycleDTO.getTelX();
        String extNum = bindRecycleDTO.getExtNum();
        String cityCode = bindRecycleDTO.getCityCode();
        String vccId = bindRecycleDTO.getVccId();
        String bindInfo = redissonUtil.getString(PrivateCacheUtil.getExtBindInfoKey(vccId, numType, telX, extNum));
        if (StrUtil.isNotEmpty(bindInfo)) {
            PrivateBindInfoAxe privateBindInfoAxe = JSON.parseObject(bindInfo, PrivateBindInfoAxe.class);
            if (ObjectUtil.isNotNull(privateBindInfoAxe)) {
                Date expireTime = privateBindInfoAxe.getExpireTime();
                if (ObjectUtil.isNotNull(expireTime)) {
                    int compare = DateUtil.compare(expireTime, DateUtil.date());
                    if (compare > 0) {
                        LambdaQueryWrapper<PrivateBindInfoAxe> wrapper = new LambdaQueryWrapper<>();
                        wrapper.select(PrivateBindInfoAxe::getExpireTime, PrivateBindInfoAxe::getRequestId);
                        wrapper.eq(PrivateBindInfoAxe::getBindId, bindId);
                        wrapper.last("limit 1");
                        PrivateBindInfoAxe bindInfoAxe = privateBindInfoAxeMapper.selectOne(wrapper);
                        int update = 0;
                        if (ObjectUtil.isNotEmpty(bindInfoAxe)
                                && ObjectUtil.isNotNull(bindInfoAxe)
                                && !expireTime.equals(bindInfoAxe.getExpireTime())) {
                            if (!bindInfoAxe.getRequestId().equals(requestId)) {
                                int delete = privateBindInfoAxeMapper.deleteById(bindId);
                                log.info("AXE 旧 requestId: {},  新 requestId: {}, 绑定关系回收未删除db记录, xe已被分配, 删除结果: {} ",
                                        requestId, privateBindInfoAxe.getRequestId(), delete);
                                return;
                            }
                            PrivateBindInfoAxe infoAxe = new PrivateBindInfoAxe();
                            infoAxe.setExpireTime(expireTime);
                            infoAxe.setBindId(bindId);
                            update = privateBindInfoAxeMapper.updateById(infoAxe);
                        }
                        log.info("AXE requestId: {}, 绑定关系未过期不回收, 过期时间: {}, 可能延长绑定. {}", requestId, expireTime, update);
                        return;
                    }
                } else {
                    return;
                }
            }
        }
        if (hideProperties.getSwitchs().getSaveDb()) {
            int delete = privateBindInfoAxeMapper.deleteById(bindId);
            if (delete == 0) {
                log.info("AXE areaCode: {}, bindId: {}, axe绑定关系db: {},{}, 已回收!", cityCode, bindId, telX, extNum);
                return;
            }
            if (BindIdUtil.isThirdSupplier(bindRecycleDTO.getSupplierId())) {
                log.info("third supplier: {}, vccId: {}, areaCode: {}, bindId:{}, delete :{}", bindRecycleDTO.getSupplierId(), vccId, cityCode, bindId, delete);
                return;
            }
            // 自动解绑 解绑通知推送
            PrivateBindInfoAxe bindInfoAxe = axeBindConverter.bindRecycleDTO2bindInfo(bindRecycleDTO);
            MqBindInfoBO bindInfoBO = MqBindInfoBO.builder()
                    .vccId(vccId)
                    .numType(numType)
                    .privateBindInfoAxe(bindInfoAxe)
                    .build();
            bindPushService.pushUnBind(bindInfoBO);
            // axe-ax 失效 导致axe-ayb失效，但axe-ayb失效不会影响axe-ax, 且所有基于该AXE关系生成的AYB绑定都解绑
            if (ObjectUtil.isNotEmpty(bindRecycleDTO.getAybFlag()) && bindRecycleDTO.getAybFlag() == 1) {
                // recycleAyb(bindId);
            }
        }
        // redis不存在绑定关系, 说明已过期, 需删除db记录
        log.info("AXE 开始回收X号码, areaCode: {}, bindId: {}, X: {}, 分机号: {}", cityCode, bindId, telX, extNum);

        // 判断X号码是否被删除, 不在地市池子里
        Optional<List<String>> poolOptional = NumberPoolAxeCache.getPool(vccId, cityCode);
        if (!poolOptional.isPresent()) {
            return;
        }
        List<String> pool = poolOptional.get();
        if (!pool.contains(telX)) {
            log.warn("AXE, vccId: {}, bindId: {}, areaCode: {},  X: {}, 已被删除, 不回收", vccId, bindId, cityCode, telX);
            return;
        }

        // 未使用分机号set
        String usableExtPoolListKey = PrivateCacheUtil.getUsableExtPoolListKey(vccId, numType, cityCode, telX);
        boolean offer = redissonUtil.offerLastDeque(usableExtPoolListKey, extNum);
        log.info("AXE, areaCode: {}, bindId: {},  回收X号码: {}, 分机号: {}, 结果: {}", cityCode, bindId, telX, extNum, offer);
        if (offer) {
            applicationContext.publishEvent(new AxeBindStatsEvent(this, vccId, cityCode, OperateTypeEnum.DELETE));
        }
    }

    /**
     * 回收AYB, AXE_AXE过期, AXE_AYB全部回收
     */
    private void recycleAyb(String bindId) {
        LambdaQueryWrapper<PrivateBindInfoAxb> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PrivateBindInfoAxb::getSourceBindId, bindId);
        List<PrivateBindInfoAxb> bindInfoAxbList = privateBindInfoAxbMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(bindInfoAxbList)) {
            return;
        }
        for (PrivateBindInfoAxb bindInfoAxb : bindInfoAxbList) {
            BindRecycleDTO bindRecycleDTO = axbBindConverter.bindInfoAxb2BindRecycleDTO(bindInfoAxb);
            bindRecycleDTO.setNumType(BusinessTypeEnum.AXB.name());
            axbRecycleNumberImpl.recycle(bindRecycleDTO);
            // ayb解绑推送
            MqBindInfoBO bindInfoBO = MqBindInfoBO.builder()
                    .vccId(bindRecycleDTO.getVccId())
                    // TODO 类型修改为AXE_AYB
                    .numType(NumberTypeEnum.AXEYB_AYB.name())
                    .privateBindInfoAxb(bindInfoAxb)
                    .build();
            bindPushService.pushUnBind(bindInfoBO);
        }


    }
}
