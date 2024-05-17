package com.cqt.hmyc.web.corpinfo.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.hmyc.web.corpinfo.mapper.PrivateCorpBusinessInfoMapper;
import com.cqt.hmyc.web.corpinfo.mapper.PrivateNumberInfoMapper;
import com.cqt.hmyc.web.corpinfo.service.IPrivateCorpBusinessInfoService;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;

/**
 * 企业配置表
 *
 * @author dingsh
 * @date 2022/07/27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IPrivateCorpBusinessInfoServiceImpl extends ServiceImpl<PrivateCorpBusinessInfoMapper, PrivateCorpBusinessInfo> implements IPrivateCorpBusinessInfoService {

    private final RedissonUtil redissonUtil;

    private final PrivateNumberInfoMapper privateNumberInfoMapper;

    @Override
    public String getVccId(String secretNo) {
        String xNumberBelongVccIdKey = String.format(PrivateCacheConstant.X_NUMBER_BELONG_VCC_ID_KEY, secretNo);
        String vccId = null;
        try {
            vccId = redissonUtil.getString(xNumberBelongVccIdKey);
        } catch (Exception e) {
            log.error("key: {}, redis get异常: ", xNumberBelongVccIdKey, e);
        }
        if (StrUtil.isEmpty(vccId)) {
            PrivateNumberInfo privateNumberInfo = privateNumberInfoMapper.selectById(secretNo);
            if (ObjectUtil.isEmpty(privateNumberInfo)) {
                log.error("X号码: {}, 不存在本平台", secretNo);
                throw new RuntimeException(MessageFormatter.format("中间号: {} 不存在本平台!", secretNo).getMessage());
            }
            vccId = privateNumberInfo.getVccId();
        }

        return vccId;
    }
}
