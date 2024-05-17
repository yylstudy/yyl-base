package com.cqt.hmyc.web.bind.service.recycle.recycle.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.mapper.ax.PrivateBindInfoAxMapper;
import com.cqt.hmyc.web.bind.service.ax.AxBindConverter;
import com.cqt.hmyc.web.bind.service.push.BindPushService;
import com.cqt.hmyc.web.bind.service.recycle.recycle.RecycleNumberStrategy;
import com.cqt.hmyc.web.cache.NumberPoolAxCache;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAx;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/4/19 20:07
 */
@Slf4j
@Service
public class AxRecycleNumberImpl implements RecycleNumberStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.AX.name();

    @Resource
    private RedissonUtil redissonUtil;

    @Resource
    private HideProperties hideProperties;

    @Resource
    private PrivateBindInfoAxMapper privateBindInfoAxMapper;

    @Resource
    private AxBindConverter axBindConverter;

    @Resource
    private BindPushService bindPushService;

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

    @Override
    public void recycle(BindRecycleDTO bindRecycleDTO) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AX, bindRecycleDTO.getVccId());
            recycleAx(bindRecycleDTO);
        }
    }

    /**
     * AX X回收
     */
    private void recycleAx(BindRecycleDTO bindRecycleDTO) {
        String vccId = bindRecycleDTO.getVccId();
        String bindId = bindRecycleDTO.getBindId();
        String numType = bindRecycleDTO.getNumType();
        String cityCode = bindRecycleDTO.getCityCode();
        String requestId = bindRecycleDTO.getRequestId();
        String telX = bindRecycleDTO.getTelX();
        String telxBindInfoKey = PrivateCacheUtil.getTelxBindInfoKey(vccId, numType, telX);
        String bindInfo = redissonUtil.getString(telxBindInfoKey);
        if (StrUtil.isNotEmpty(bindInfo)) {
            PrivateBindInfoAx privateBindInfoAx = JSON.parseObject(bindInfo, PrivateBindInfoAx.class);
            if (ObjectUtil.isNotNull(privateBindInfoAx)) {
                Date expireTime = privateBindInfoAx.getExpireTime();
                if (ObjectUtil.isNotNull(expireTime)) {
                    int compare = DateUtil.compare(expireTime, DateUtil.date());
                    if (compare > 0) {
                        LambdaQueryWrapper<PrivateBindInfoAx> wrapper = new LambdaQueryWrapper<>();
                        wrapper.select(PrivateBindInfoAx::getExpireTime, PrivateBindInfoAx::getRequestId);
                        wrapper.eq(PrivateBindInfoAx::getBindId, bindId);
                        wrapper.last("limit 1");
                        PrivateBindInfoAx bindInfoAx = privateBindInfoAxMapper.selectOne(wrapper);
                        int update = 0;
                        if (ObjectUtil.isNotEmpty(bindInfoAx)
                                && ObjectUtil.isNotNull(bindInfoAx)
                                && !expireTime.equals(bindInfoAx.getExpireTime())) {
                            if (!bindInfoAx.getRequestId().equals(requestId)) {
                                int delete = privateBindInfoAxMapper.deleteById(bindId);
                                log.info("AX vccId: {}, 旧 requestId: {},  新 requestId: {}, 绑定关系回收未删除db记录, xe已被分配, 删除结果: {} ",
                                        vccId, requestId, privateBindInfoAx.getRequestId(), delete);
                                return;
                            }
                            PrivateBindInfoAx infoAx = new PrivateBindInfoAx();
                            infoAx.setExpireTime(expireTime);
                            infoAx.setBindId(bindId);
                            update = privateBindInfoAxMapper.updateById(infoAx);
                        }
                        log.info("AX vccId: {}, requestId: {}, 绑定关系未过期不回收, 过期时间: {}, 可能延长绑定. {}", vccId, requestId, expireTime.toString(), update);
                        return;
                    }
                } else {
                    return;
                }
            }
        }
        if (hideProperties.getSwitchs().getSaveDb()) {
            int delete = privateBindInfoAxMapper.deleteById(bindRecycleDTO.getBindId());
            if (delete == 0) {
                log.info("AX, vccId: {}, areaCode: {}, requestId: {}, X号码: {}, 已回收!", vccId, cityCode, requestId, telX);
                return;
            }
            if (BindIdUtil.isThirdSupplier(bindRecycleDTO.getSupplierId())) {
                log.info("third supplier: {}, vccId: {}, areaCode: {}, bindId:{}, delete :{}", bindRecycleDTO.getSupplierId(), vccId, cityCode, bindId, delete);
                return;
            }
            // 自动解绑 解绑通知推送
            PrivateBindInfoAx bindInfoAx = axBindConverter.bindRecycleDTO2bindInfoAx(bindRecycleDTO);
            MqBindInfoBO bindInfoBO = MqBindInfoBO.builder()
                    .vccId(vccId)
                    .numType(numType)
                    .privateBindInfoAx(bindInfoAx)
                    .build();
            bindPushService.pushUnBind(bindInfoBO);
        }
        log.info("AX, 开始回收X号码, vccId: {}, areaCode: {}, requestId: {}, X号码: {}", vccId, cityCode, requestId, telX);

        // 判断X号码是否被删除, 不在地市池子里
        Optional<List<String>> poolOptional = NumberPoolAxCache.getPool(vccId, cityCode);
        if (!poolOptional.isPresent()) {
            log.warn("本地内存, vccId: {}, area_code: {}, 号码池为空.", vccId, cityCode);
            return;
        }
        List<String> pool = poolOptional.get();
        if (!pool.contains(telX)) {
            log.warn("AX, vccId: {}, areaCode: {},  X: {}, 已被删除, 不回收", vccId, cityCode, telX);
            return;
        }
        String axUsablePoolKey = PrivateCacheUtil.getAxUsablePoolKey(vccId, numType, cityCode);
        boolean addSet = redissonUtil.addSet(axUsablePoolKey, telX);
        log.info("AX, vccId: {}, areaCode: {}, requestId: {}, 回收X号码: {}, 结果: {}", vccId, cityCode, requestId, telX, addSet);
    }

    private Boolean isThirdSupplier(String supplierId) {
        return StrUtil.isNotEmpty(supplierId) && !GatewayConstant.LOCAL.equals(supplierId);
    }
}
