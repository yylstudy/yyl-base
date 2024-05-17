package com.cqt.broadnet.web.bind.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.broadnet.config.BizException;
import com.cqt.broadnet.web.bind.mapper.PrivateSupplierInfoMapper;
import com.cqt.broadnet.web.bind.service.PrivateSupplierInfoService;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.supplier.PrivateSupplierInfo;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-04-13 14:44
 * 供应商信息查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateSupplierInfoServiceImpl implements PrivateSupplierInfoService {

    private final RedissonUtil redissonUtil;
    private final PrivateSupplierInfoMapper privateSupplierInfoMapper;

    @Override
    public PrivateSupplierInfo getSupplierInfo(String supplierId) {
        String supplierInfoKey = PrivateCacheUtil.getSupplierInfoKey(supplierId);
        String json = redissonUtil.getStringX(supplierInfoKey);
        PrivateSupplierInfo supplierInfo;
        if (StrUtil.isEmpty(json)) {
            supplierInfo = privateSupplierInfoMapper.selectById(supplierId);
            if (supplierInfo == null) {
                throw new BizException("未查询到供应商: " + supplierId);
            }
            // 回写redis
            redissonUtil.setString(supplierInfoKey, JSON.toJSONString(supplierInfo));
            return supplierInfo;
        }
        supplierInfo = JSON.parseObject(json, PrivateSupplierInfo.class);
        return supplierInfo;
    }
}
