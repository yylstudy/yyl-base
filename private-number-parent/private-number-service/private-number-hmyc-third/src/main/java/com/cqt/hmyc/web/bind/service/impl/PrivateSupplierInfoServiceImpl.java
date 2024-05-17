package com.cqt.hmyc.web.bind.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.constants.ThirdConstant;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.config.exception.AuthException;
import com.cqt.hmyc.web.bind.mapper.PrivateSupplierInfoMapper;
import com.cqt.hmyc.web.bind.service.PrivateSupplierInfoService;
import com.cqt.hmyc.web.model.hdh.auth.HdhAuthDTO;
import com.cqt.hmyc.web.model.hdh.auth.HdhAuthInfoVO;
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

    private final PrivateSupplierInfoMapper privateSupplierInfoMapper;

    private final RedissonUtil redissonUtil;

    @Override
    public HdhAuthInfoVO getAuthInfo(String supplierId, OperateTypeEnum operateTypeEnum) {
        String supplierInfoKey = PrivateCacheUtil.getSupplierInfoKey(supplierId);
        String info = redissonUtil.getString(SystemConstant.BIZ_REDIS, supplierInfoKey);
        log.info("redis查询供应商： {}， 配置信息： {}", supplierId, info);
        if (StrUtil.isEmpty(info)) {
            PrivateSupplierInfo privateSupplierInfo = privateSupplierInfoMapper.selectById(supplierId);
            if (ObjectUtil.isEmpty(privateSupplierInfo)) {
                log.info("supplierId: {}, db未找到记录.", supplierId);
                throw new AuthException("未找到供应商信息");
            }
            redissonUtil.setString(SystemConstant.BIZ_REDIS, supplierInfoKey, JSON.toJSONString(privateSupplierInfo));
            return buildHdhAuthInfoVO(operateTypeEnum, privateSupplierInfo);
        }
        PrivateSupplierInfo privateSupplierInfo = JSON.parseObject(info, PrivateSupplierInfo.class);
        return buildHdhAuthInfoVO(operateTypeEnum, privateSupplierInfo);
    }

    public HdhAuthInfoVO buildHdhAuthInfoVO(OperateTypeEnum operateTypeEnum, PrivateSupplierInfo privateSupplierInfo) {
        HdhAuthInfoVO hdhAuthInfoVO = new HdhAuthInfoVO();
        HdhAuthDTO hdhAuthDTO = JSON.parseObject(privateSupplierInfo.getSupplierAuthInfo(), HdhAuthDTO.class);
        hdhAuthInfoVO.setUrl(getUrl(operateTypeEnum, privateSupplierInfo));
        hdhAuthInfoVO.setHeader((getHeader(hdhAuthDTO.getSecret(), hdhAuthDTO.getPlatformId())));
        hdhAuthInfoVO.setHdhAuthDTO(hdhAuthDTO);
        return hdhAuthInfoVO;
    }

    private String getUrl(OperateTypeEnum operateTypeEnum, PrivateSupplierInfo privateSupplierInfo) {
        switch (operateTypeEnum) {
            case BINDING:
                return privateSupplierInfo.getBindingUrl();
            case UNBIND:
                return privateSupplierInfo.getUnbindUrl();
            case UPDATE_EXPIRE:
                return privateSupplierInfo.getUpdateExpirationUrl();
            default:
                return "";
        }
    }

    private String getHeader(String secret, String platformId) {
        long timestamp = System.currentTimeMillis() / 1000;
        // md5加密
        String signature = SecureUtil.md5(timestamp + secret).toUpperCase();
        return String.format(ThirdConstant.AUTHORIZATION, platformId, timestamp, signature);
    }
}
