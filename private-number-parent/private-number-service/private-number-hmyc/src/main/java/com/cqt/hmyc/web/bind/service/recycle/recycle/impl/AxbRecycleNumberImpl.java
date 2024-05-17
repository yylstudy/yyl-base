package com.cqt.hmyc.web.bind.service.recycle.recycle.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.AxbPoolTypeEnum;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbMapper;
import com.cqt.hmyc.web.bind.service.axb.AxbBindConverter;
import com.cqt.hmyc.web.bind.service.push.BindPushService;
import com.cqt.hmyc.web.bind.service.recycle.recycle.RecycleNumberStrategy;
import com.cqt.hmyc.web.cache.NumberPoolAxbCache;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/4/19 20:07
 */
@Slf4j
@Service
public class AxbRecycleNumberImpl implements RecycleNumberStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AXB.name();

    @Resource
    private RedissonUtil redissonUtil;

    @Resource
    private HideProperties hideProperties;

    @Resource
    private PrivateBindInfoAxbMapper privateBindInfoAxbMapper;

    @Resource
    private AxbBindConverter axbBindConverter;

    @Resource
    private BindPushService bindPushService;

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

    @Override
    public void recycle(BindRecycleDTO bindRecycleDTO) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, bindRecycleDTO.getVccId());
            recycleAxb(bindRecycleDTO);
        }
    }

    /**
     * AXB X号码回收
     */
    private void recycleAxb(BindRecycleDTO bindRecycleDTO) {
        String requestId = bindRecycleDTO.getRequestId();
        String telX = bindRecycleDTO.getTelX();
        String telA = bindRecycleDTO.getTelA();
        String telB = bindRecycleDTO.getTelB();
        String cityCode = bindRecycleDTO.getCityCode();
        String vccId = bindRecycleDTO.getVccId();
        String numType = bindRecycleDTO.getNumType();
        String bindId = bindRecycleDTO.getBindId();

        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, telX);
        String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telB, telX);
        String axBindInfo = redissonUtil.getString(axBindInfoKey);
        String bxBindInfo = redissonUtil.getString(bxBindInfoKey);
        boolean axExist = StrUtil.isNotEmpty(axBindInfo);
        boolean bxExist = StrUtil.isNotEmpty(bxBindInfo);
        if (axExist && bxExist) {
            PrivateBindInfoAxb bindInfoAxb = JSON.parseObject(axBindInfo, PrivateBindInfoAxb.class);
            if (ObjectUtil.isNotNull(bindInfoAxb)) {
                Date expireTime = bindInfoAxb.getExpireTime();
                if (ObjectUtil.isNotNull(expireTime)) {
                    int compare = DateUtil.compare(expireTime, DateUtil.date());
                    if (compare > 0) {
                        LambdaQueryWrapper<PrivateBindInfoAxb> wrapper = new LambdaQueryWrapper<>();
                        wrapper.select(PrivateBindInfoAxb::getExpireTime, PrivateBindInfoAxb::getRequestId);
                        wrapper.eq(PrivateBindInfoAxb::getBindId, bindId);
                        wrapper.last("limit 1");
                        PrivateBindInfoAxb privateBindInfoAxb = privateBindInfoAxbMapper.selectOne(wrapper);
                        int update = 0;
                        if (ObjectUtil.isNotEmpty(privateBindInfoAxb)
                                && ObjectUtil.isNotNull(bindInfoAxb)
                                && !expireTime.equals(privateBindInfoAxb.getExpireTime())) {
                            if (!bindInfoAxb.getRequestId().equals(requestId)) {
                                int delete = privateBindInfoAxbMapper.deleteById(bindId);
                                log.info("AXB 旧 requestId: {},  新 requestId: {}, 绑定关系回收未删除db记录, xe已被分配, 删除结果: {} ",
                                        requestId, bindInfoAxb.getRequestId(), delete);
                                return;
                            }
                            PrivateBindInfoAxb infoAxb = new PrivateBindInfoAxb();
                            infoAxb.setExpireTime(expireTime);
                            infoAxb.setBindId(bindId);
                            update = privateBindInfoAxbMapper.updateById(infoAxb);
                        }
                        log.info("AXB vccId: {}, requestId: {}, 绑定关系未过期不回收, 过期时间: {}, 可能延长绑定. {}", vccId, requestId, expireTime.toString(), update);
                        return;
                    }
                } else {
                    return;
                }
            }
        }
        boolean notEmptyTelA = StrUtil.isNotEmpty(telA);
        boolean notEmptyTelB = StrUtil.isNotEmpty(telB);
        if (hideProperties.getSwitchs().getSaveDb() && notEmptyTelA && notEmptyTelB) {
            // 已删除 这时不需要回收, 万一X又被分配出去, 还未过期
            int delete = privateBindInfoAxbMapper.deleteById(bindId);
            if (delete == 0) {
                log.info("vccId: {}, areaCode: {}, requestId: {}, axb绑定关系db: X: {}, A: {}, B: {}, 已回收!", vccId, cityCode, requestId, telX, telA, telB);
                return;
            }
            if (BindIdUtil.isThirdSupplier(bindRecycleDTO.getSupplierId())) {
                log.info("third supplier: {}, vccId: {}, areaCode: {}, bindId:{}, delete :{}", bindRecycleDTO.getSupplierId(), vccId, cityCode, bindId, delete);
                return;
            }
            if (BindIdUtil.isDirectTelX(bindRecycleDTO.getDirectTelX())) {
                return;
            }
            // 自动解绑 解绑通知推送
            PrivateBindInfoAxb bindInfoAxb = axbBindConverter.bindRecycleDTO2bindInfoAxb(bindRecycleDTO);
            MqBindInfoBO bindInfoBO = MqBindInfoBO.builder()
                    .vccId(vccId)
                    .numType(numType)
                    .operateType(bindRecycleDTO.getOperateType())
                    .privateBindInfoAxb(bindInfoAxb)
                    .build();
            bindPushService.pushUnBind(bindInfoBO);
        }
        if (BindIdUtil.isThirdSupplier(bindRecycleDTO.getSupplierId())) {
            return;
        }

        if (BindIdUtil.isDirectTelX(bindRecycleDTO.getDirectTelX())) {
            return;
        }

        log.info("AXB开始回收X号码, vccId: {}, areaCode: {}, requestId: {}, X: {}, A: {}, B: {}", vccId, cityCode, requestId, telX, telA, telB);
        // 判断X号码是否被删除, 不在地市池子里
        Optional<HashSet<String>> poolOptional = NumberPoolAxbCache.getPool(AxbPoolTypeEnum.ALL, vccId, cityCode);
        if (!poolOptional.isPresent()) {
            return;
        }
        HashSet<String> pool = poolOptional.get();
        if (!pool.contains(telX)) {
            return;
        }

        // 1. X号码添加到A/B可用号码池中 sadd
        boolean addAxSet = false;
        boolean addBxSet = false;
        if (!axExist && notEmptyTelA) {
            String axPool = PrivateCacheUtil.getUsablePoolKey(vccId, numType, cityCode, telA);
            // 存在初始化标识
            String axInitKey = PrivateCacheUtil.getInitFlagKey(vccId, numType, cityCode, telA);
            String initFlag = redissonUtil.getString(axInitKey);
            if (StrUtil.isNotEmpty(initFlag)) {
                addAxSet = redissonUtil.addSet(axPool, telX);
            }
        }
        if (!bxExist && notEmptyTelB) {
            String bxPool = PrivateCacheUtil.getUsablePoolKey(vccId, numType, cityCode, telB);
            String bxInitKey = PrivateCacheUtil.getInitFlagKey(vccId, numType, cityCode, telB);
            String initFlag = redissonUtil.getString(bxInitKey);
            if (StrUtil.isNotEmpty(initFlag)) {
                addBxSet = redissonUtil.addSet(bxPool, telX);
            }
        }
        log.info("AXB, vccId: {}, areaCode: {}, requestId: {}, 回收X号码: {}, ax: {}, bx: {}", vccId, cityCode, requestId, telX, addAxSet, addBxSet);

    }

}
