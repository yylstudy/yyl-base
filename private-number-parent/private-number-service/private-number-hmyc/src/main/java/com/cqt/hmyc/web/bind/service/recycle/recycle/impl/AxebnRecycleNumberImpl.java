package com.cqt.hmyc.web.bind.service.recycle.recycle.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.mapper.axebn.PrivateBindInfoAxebnMapper;
import com.cqt.hmyc.web.bind.service.axebn.AxebnBindConverter;
import com.cqt.hmyc.web.bind.service.push.BindPushService;
import com.cqt.hmyc.web.bind.service.recycle.recycle.RecycleNumberStrategy;
import com.cqt.hmyc.web.cache.NumberPoolAxebnCache;
import com.cqt.model.bind.axebn.entity.PrivateBindInfoAxebn;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/4/19 20:07
 * AXEBN此模式 废弃
 */
@Slf4j
@Service
@Deprecated
public class AxebnRecycleNumberImpl implements RecycleNumberStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXEBN.name();

    @Resource
    private RedissonUtil redissonUtil;

    @Resource
    private HideProperties hideProperties;

    @Resource
    private PrivateBindInfoAxebnMapper privateBindInfoAxebnMapper;

    @Resource
    private AxebnBindConverter axebnBindConverter;

    @Resource
    private BindPushService bindPushService;

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

    @Override
    public void recycle(BindRecycleDTO bindRecycleDTO) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXEBN, bindRecycleDTO.getVccId());
            recycleAxebn(bindRecycleDTO);
        }
    }

    /**
     * AXEBN X号码回收
     */
    private void recycleAxebn(BindRecycleDTO bindRecycleDTO) {
        String vccId = bindRecycleDTO.getVccId();
        String bindId = bindRecycleDTO.getBindId();
        String numType = bindRecycleDTO.getNumType();
        String cityCode = bindRecycleDTO.getCityCode();
        String requestId = bindRecycleDTO.getRequestId();
        String telX = bindRecycleDTO.getTelX();
        String extNum = bindRecycleDTO.getExtNum();
        List<String> extNumList = StrUtil.split(extNum, ",");
        if (CollUtil.isEmpty(extNumList)) {
            return;
        }
        // 有问题?
        String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, numType, telX, extNumList.get(0));
        String bindInfo = redissonUtil.getString(extBindInfoKey);
        if (StrUtil.isNotEmpty(bindInfo)) {
            PrivateBindInfoAxebn privateBindInfoAxebn = JSON.parseObject(bindInfo, PrivateBindInfoAxebn.class);
            if (ObjectUtil.isNotNull(privateBindInfoAxebn)) {
                Date expireTime = privateBindInfoAxebn.getExpireTime();
                if (ObjectUtil.isNotNull(expireTime)) {
                    int compare = DateUtil.compare(expireTime, DateUtil.date());
                    if (compare > 0) {
                        LambdaQueryWrapper<PrivateBindInfoAxebn> wrapper = new LambdaQueryWrapper<>();
                        wrapper.select(PrivateBindInfoAxebn::getExpireTime, PrivateBindInfoAxebn::getRequestId);
                        wrapper.eq(PrivateBindInfoAxebn::getBindId, bindId);
                        wrapper.last("limit 1");
                        PrivateBindInfoAxebn bindInfoAxebn = privateBindInfoAxebnMapper.selectOne(wrapper);
                        int update = 0;
                        if (ObjectUtil.isNotNull(bindInfoAxebn) && !expireTime.equals(bindInfoAxebn.getExpireTime())) {
                            if (!bindInfoAxebn.getRequestId().equals(requestId)) {
                                int delete = privateBindInfoAxebnMapper.deleteById(bindId);
                                log.info("AXEBN vccId: {}, 旧 requestId: {},  新 requestId: {}, 绑定关系回收未删除db记录, xe已被分配, 删除结果: {} ",
                                        vccId, requestId, privateBindInfoAxebn.getRequestId(), delete);
                                return;
                            }
                            PrivateBindInfoAxebn infoAxebn = new PrivateBindInfoAxebn();
                            infoAxebn.setExpireTime(expireTime);
                            infoAxebn.setBindId(bindId);
                            update = privateBindInfoAxebnMapper.updateById(infoAxebn);
                        }
                        log.info("AXEBN vccId: {}, requestId: {}, 绑定关系未过期不回收, 过期时间: {}, 可能延长绑定. {}", vccId, requestId, expireTime.toString(), update);
                        return;
                    }
                } else {
                    return;
                }
            }
        }
        if (hideProperties.getSwitchs().getSaveDb()) {
            int delete = privateBindInfoAxebnMapper.deleteById(bindRecycleDTO.getBindId());
            if (delete == 0) {
                log.info("AXEBN, vccId: {}, areaCode: {}, requestId: {}, ax绑定关系db: {},{}, 已回收!", vccId, cityCode, requestId, telX, extNum);
                return;
            }
            // 自动解绑 解绑通知推送
            PrivateBindInfoAxebn bindInfoAxebn = axebnBindConverter.bindRecycleDTO2bindInfoAxebn(bindRecycleDTO);
            MqBindInfoBO bindInfoBO = MqBindInfoBO.builder()
                    .vccId(vccId)
                    .numType(numType)
                    .privateBindInfoAxebn(bindInfoAxebn)
                    .build();
            bindPushService.pushUnBind(bindInfoBO);
        }
        log.info("AXEBN, 开始回收X号码, vccId: {}, areaCode: {}, requestId: {}, X: {}, 分机号: {}", vccId, cityCode, requestId, telX, extNum);

        // 判断X号码是否被删除, 不在地市池子里
        Optional<ArrayList<String>> poolOptional = NumberPoolAxebnCache.getPool(vccId, cityCode);
        if (!poolOptional.isPresent()) {
            return;
        }
        ArrayList<String> pool = poolOptional.get();
        if (!pool.contains(telX)) {
            log.warn("AXEBN, vccId: {}, areaCode: {},  X: {}, 已被删除, 不回收", vccId, cityCode, telX);
            return;
        }

        // 回收 A-En,
        String poolUnUsedExtKey = PrivateCacheUtil.getUsableExtPoolKey(vccId, numType, telX);
        boolean addSet = redissonUtil.addAllSet(poolUnUsedExtKey, extNumList);
        log.info("AXEBN, vccId: {}, areaCode: {}, requestId: {}, telX: {}, 回收分机号: {}, 结果: {}", vccId, cityCode, requestId, telX, extNum, addSet);
        // 回收 Bn-X
        List<String> telbList = StrUtil.split(bindRecycleDTO.getTelB(), ",");
        for (String telB : telbList) {
            String usablePoolSlotKey = PrivateCacheUtil.getUsablePoolSlotKey(vccId, numType, cityCode, telB);
            boolean set = redissonUtil.addSet(usablePoolSlotKey, telX);
            log.info("AXEBN, vccId: {}, areaCode: {}, requestId: {}, B号码: {}, 回收可用池X号码: {}, 结果: {}", vccId, cityCode, requestId, telB, telX, set);
        }
    }

}
