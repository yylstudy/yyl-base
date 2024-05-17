package com.cqt.broadnet.web.x.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.cqt.broadnet.common.cache.CorpBusinessConfigCache;
import com.cqt.broadnet.common.model.axb.dto.BindIdMapperDTO;
import com.cqt.broadnet.config.BizException;
import com.cqt.broadnet.web.x.mapper.PrivateNumberInfoMapper;
import com.cqt.broadnet.web.x.service.PrivateCorpBusinessInfoService;
import com.cqt.common.constants.PrivateCacheConstant;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.redis.util.RedissonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-02-20 16:41
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateCorpBusinessInfoServiceImpl implements PrivateCorpBusinessInfoService {

    private final ObjectMapper objectMapper;

    private final RedissonUtil redissonUtil;

    private final PrivateNumberInfoMapper privateNumberInfoMapper;

    @SneakyThrows
    @Override
    public PrivateCorpBusinessInfoDTO getPrivateCorpBusinessInfoDTO(String secretNo) {
        String numberInfoKey = String.format(PrivateCacheConstant.PRIVATE_NUMBER_INFO, secretNo);
        String numberInfoJson = "";
        String vccId = "";
        try {
            numberInfoJson = redissonUtil.getStringX(numberInfoKey);
        } catch (Exception e) {
            log.error("key: {}, redis get异常: ", numberInfoKey, e);
        }
        if (StrUtil.isEmpty(numberInfoJson)) {
            PrivateNumberInfo privateNumberInfo = privateNumberInfoMapper.selectById(secretNo);
            if (ObjectUtil.isEmpty(privateNumberInfo)) {
                log.error("X号码: {}, 不存在本平台", secretNo);
                throw new BizException(MessageFormatter.format("中间号: {} 不存在本平台!", secretNo).getMessage());
            }
            vccId = privateNumberInfo.getVccId();
            return CorpBusinessConfigCache.get(vccId).orElseThrow(() -> new BizException("未找到中间号归属"));
        }
        PrivateNumberInfo numberInfo = objectMapper.readValue(numberInfoJson, PrivateNumberInfo.class);
        if (Optional.ofNullable(numberInfo).isPresent()) {
            vccId = numberInfo.getVccId();
        }
        return CorpBusinessConfigCache.get(vccId).orElseThrow(() -> new BizException("未找到中间号归属"));
    }

    @Override
    public Optional<BindInfoApiVO> getBindInfoVO(String callId) throws JsonProcessingException {
        String broadNetCallIdWithBindIdKey = PrivateCacheUtil.getBroadNetCallIdWithBindIdKey(callId);
        String bindInfo;
        try {
            bindInfo = redissonUtil.getString(broadNetCallIdWithBindIdKey);
        } catch (Exception e) {
            log.error("key: {}, redis getBindInfoVO异常: ", broadNetCallIdWithBindIdKey, e);
            return Optional.empty();
        }
        if (StrUtil.isNotEmpty(bindInfo)) {
            BindInfoApiVO bindInfoApiVO = objectMapper.readValue(bindInfo, BindInfoApiVO.class);
            return Optional.of(bindInfoApiVO);
        }
        return Optional.empty();
    }

    @Override
    public String getAreaCode(String xNum) {

        String format = String.format(PrivateCacheConstant.PRIVATE_NUMBER_INFO, xNum);
        String string = redissonUtil.getStringX(format);
        if (StringUtils.isEmpty(string)) {
            log.info("{} 号码查询缓存为空,rediskey:{}", xNum, format);
            PrivateNumberInfo privateNumberInfo = privateNumberInfoMapper.selectById(xNum);
            if (ObjectUtil.isEmpty(privateNumberInfo)) {
                throw new BizException("中间号不存在本平台!");
            }
            return privateNumberInfo.getAreaCode();
        }
        PrivateNumberInfo privateNumberInfo = JSONObject.parseObject(string, PrivateNumberInfo.class);
        return privateNumberInfo.getAreaCode();
    }

    @Override
    public String getCqtBindId(String broadBindId) throws JsonProcessingException {
        String bindMapperKey = PrivateCacheUtil.getBindMapperKey(broadBindId);
        String data = redissonUtil.getString(bindMapperKey);
        if (StrUtil.isNotEmpty(data)) {
            BindIdMapperDTO bindIdMapperDTO = objectMapper.readValue(data, BindIdMapperDTO.class);
            if (StrUtil.isNotEmpty(bindIdMapperDTO.getCqtBindId())) {
                log.info("broad net bindId: {}, cqt bindId: {}", broadBindId, bindIdMapperDTO.getCqtBindId());
                return bindIdMapperDTO.getCqtBindId();
            }
        }
        return broadBindId;
    }
}
