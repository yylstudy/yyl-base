package com.cqt.hmyc.web.bind.service.recycle.recycle.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.mapper.axbn.PrivateBindAxbnRealTelMapper;
import com.cqt.hmyc.web.bind.mapper.axbn.PrivateBindInfoAxbnMapper;
import com.cqt.hmyc.web.bind.service.axbn.AxbnBindConverter;
import com.cqt.hmyc.web.bind.service.push.BindPushService;
import com.cqt.hmyc.web.bind.service.recycle.recycle.RecycleNumberStrategy;
import com.cqt.hmyc.web.cache.NumberPoolAxbnCache;
import com.cqt.model.bind.axbn.entity.PrivateBindAxbnRealTel;
import com.cqt.model.bind.axbn.entity.PrivateBindInfoAxbn;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/4/19 20:07
 */
@Slf4j
@Service
public class AxbnRecycleNumberImpl implements RecycleNumberStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXBN.name();

    @Resource
    private RedissonUtil redissonUtil;

    @Resource
    private HideProperties hideProperties;

    @Resource
    private PrivateBindInfoAxbnMapper privateBindInfoAxbnMapper;

    @Resource
    private PrivateBindAxbnRealTelMapper privateBindAxbnRealTelMapper;

    @Resource
    private AxbnBindConverter axbnBindConverter;

    @Resource
    private BindPushService bindPushService;

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

    @Override
    public void recycle(BindRecycleDTO bindRecycleDTO) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXBN, bindRecycleDTO.getVccId());
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_AXBN_REAL_TEL, bindRecycleDTO.getVccId());
            recycleAxbn(bindRecycleDTO);
        }
    }

    /**
     * AXBN X回收
     */
    private void recycleAxbn(BindRecycleDTO bindRecycleDTO) {
        String vccId = bindRecycleDTO.getVccId();
        String bindId = bindRecycleDTO.getBindId();
        String numType = bindRecycleDTO.getNumType();
        String cityCode = bindRecycleDTO.getCityCode();
        String requestId = bindRecycleDTO.getRequestId();
        String telX = bindRecycleDTO.getTelX();

        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, numType, bindId);
        String bindIdInfo = redissonUtil.getString(bindIdKey);
        if (StrUtil.isNotEmpty(bindIdInfo)) {
            log.warn("{} vccId: {}, bindId: {}, requestId: {}, 绑定关系存在, 不回收", numType, vccId, bindId, requestId);
            return;
        }
        if (hideProperties.getSwitchs().getSaveDb()) {
            // 删除真实号码记录
            LambdaQueryWrapper<PrivateBindAxbnRealTel> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PrivateBindAxbnRealTel::getBindId, bindId);
            privateBindAxbnRealTelMapper.delete(queryWrapper);
            int delete = privateBindInfoAxbnMapper.deleteById(bindRecycleDTO.getBindId());
            if (delete == 0) {
                log.warn("AX, vccId: {}, areaCode: {}, bindId: {}, X号码: {}, 已回收!", vccId, cityCode, bindId, telX);
                return;
            }
            // 自动解绑 解绑通知推送
            PrivateBindInfoAxbn bindInfoAxbn = axbnBindConverter.bindRecycleDTO2bindInfoAxbn(bindRecycleDTO);
            MqBindInfoBO bindInfoBO = MqBindInfoBO.builder()
                    .vccId(vccId)
                    .numType(numType)
                    .privateBindInfoAxbn(bindInfoAxbn)
                    .build();
            bindPushService.pushUnBind(bindInfoBO);
        }

        // 判断X号码是否被删除, 不在地市池子里
        Optional<HashSet<String>> poolOptional = NumberPoolAxbnCache.getPool(vccId, cityCode);
        if (!poolOptional.isPresent()) {
            log.warn("本地内存, vccId: {}, area_code: {}, 号码池为空.", vccId, cityCode);
            return;
        }

        // 移除已使用X号码集合
        List<Object> keyList = new ArrayList<>();
        keyList.add(PrivateCacheUtil.getUsedPoolSlotKey(vccId, numType, cityCode, bindRecycleDTO.getTelA()));
        keyList.add(PrivateCacheUtil.getUsedPoolSlotKey(vccId, numType, cityCode, bindRecycleDTO.getTelB()));
        if (StrUtil.isNotEmpty(bindRecycleDTO.getOtherTelB())) {
            List<String> otherTelList = StrUtil.split(bindRecycleDTO.getOtherTelB(), ",");
            for (String tel : otherTelList) {
                keyList.add(PrivateCacheUtil.getUsedPoolSlotKey(vccId, numType, cityCode, tel));
            }
        }
        List<String> xList = new ArrayList<>();
        xList.add(telX);
        String telY = bindRecycleDTO.getTelY();
        if (StrUtil.isNotEmpty(telY)) {
            List<String> list = StrUtil.split(telY, ",");
            xList.addAll(list);
        }
        for (Object obj : keyList) {
            boolean removeSetBatch = redissonUtil.removeSetBatch(obj.toString(), xList);
            log.info("{}, vccId: {}, areaCode: {}, bindId: {}, key:{}, 结果: {}", numType, vccId, cityCode, bindId, obj, removeSetBatch);
        }
    }
}
