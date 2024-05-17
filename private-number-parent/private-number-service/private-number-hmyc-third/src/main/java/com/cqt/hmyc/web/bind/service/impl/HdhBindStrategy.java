package com.cqt.hmyc.web.bind.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.JustForTest;
import com.cqt.cloud.api.basesetting.BaseSettingFeignClient;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.constants.ThirdConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.common.util.ThirdUtils;
import com.cqt.hmyc.config.exception.ParamsException;
import com.cqt.hmyc.config.properties.ThirdProperties;
import com.cqt.hmyc.config.rabbitmq.RabbitMqConfig;
import com.cqt.hmyc.web.bind.cache.LocalCacheService;
import com.cqt.hmyc.web.bind.manager.PrivateMqProducer;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbHisMapper;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbMapper;
import com.cqt.hmyc.web.bind.mapper.axe.PrivateBindInfoAxeHisMapper;
import com.cqt.hmyc.web.bind.mapper.axe.PrivateBindInfoAxeMapper;
import com.cqt.hmyc.web.bind.service.AxbBindConverter;
import com.cqt.hmyc.web.bind.service.AxeBindConverter;
import com.cqt.hmyc.web.bind.service.BindStrategy;
import com.cqt.hmyc.web.bind.service.PrivateSupplierInfoService;
import com.cqt.hmyc.web.model.hdh.auth.HdhAuthInfoVO;
import com.cqt.hmyc.web.model.hdh.axb.*;
import com.cqt.hmyc.web.model.hdh.axe.HdhAxeResult;
import com.cqt.hmyc.web.model.hdh.axe.HdhAxeUnBindDTO;
import com.cqt.model.bind.ax.dto.AxBindingDTO;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxbHis;
import com.cqt.model.bind.axb.vo.AxbBindingVO;
import com.cqt.model.bind.axe.dto.AxeBindingDTO;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxeHis;
import com.cqt.model.bind.axe.vo.AxeBindingVO;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.MessageDTO;
import com.cqt.model.common.Result;
import com.cqt.redis.util.RedissonUtil;
import groovy.transform.ASTTest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 和多号 调用
 *
 * @author dingsh
 * date 2016/10/31
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class HdhBindStrategy implements BindStrategy {

    /**
     * 供应商id, 以type开头的供应商是同一类.
     */
    private static final String SUPPLIER_PREFIX = "hdh";

    private final RedissonUtil redissonUtil;

    private final AxbBindConverter axbBindConverter;

    private final AxeBindConverter axeBindConverter;

    private final ThirdProperties thirdProperties;

    private final ThreadPoolTaskExecutor saveExecutor;

    private final BaseSettingFeignClient baseSettingFeignClient;

    private final PrivateMqProducer privateMqProducer;

    private final PrivateBindInfoAxbMapper privateBindInfoAxbMapper;

    private final PrivateBindInfoAxeMapper privateBindInfoAxeMapper;

    private final PrivateBindInfoAxbHisMapper privateBindInfoAxbHisMapper;


    private final PrivateSupplierInfoService privateSupplierInfoService;

    @SneakyThrows
    @Override
    public Result binding(AxbBindingDTO axbBindingDTO, String supplierId) {
        axbBindingDTO.setModel(2);
        // 验证请求id 是否绑定过
        String requestKye = PrivateCacheUtil.getRequestIdKey(axbBindingDTO.getVccId(), BusinessTypeEnum.AXB.name(), axbBindingDTO.getRequestId());
        String requestBody = redissonUtil.getString(requestKye);
        if (StringUtils.isNotBlank(requestBody)) {
            log.info("当前 requestId： {} 已绑定 ", axbBindingDTO.getRequestId());
            return Result.ok(JSONObject.parseObject(requestBody));
        }
        HdhAuthInfoVO authInfoVO = privateSupplierInfoService.getAuthInfo(supplierId, OperateTypeEnum.BINDING);

        String vccId = axbBindingDTO.getVccId();
        // 实体转换
        HdhAxbBindDTO hdhAxbBindDTO = axbBindConverter.axbBindingDTO2HdhAxbBindDto(axbBindingDTO);
        parseEntity(axbBindingDTO, hdhAxbBindDTO);
        hdhAxbBindDTO.setAppId(authInfoVO.getHdhAuthDTO().getAppId());
        // 放音编码字段处理
        JSONObject audio = audioCreat(axbBindingDTO);
        hdhAxbBindDTO.setAudio(audio);
        // 字段解析
        hdhAxbBindDTO = ThirdUtils.trimBlankString(hdhAxbBindDTO);

        String msgRequest = JSON.toJSONString(hdhAxbBindDTO);
        // 调用第三方
        return resolveResult(supplierId, request(authInfoVO, vccId, supplierId, msgRequest), axbBindingDTO, requestKye);
    }

    @Override
    public Result axeBinding(AxeBindingDTO axeBindingDTO, String supplierId) {
        axeBindingDTO.setModel(2);
        // 验证请求id 是否绑定过
        String requestKye = PrivateCacheUtil.getRequestIdKey(axeBindingDTO.getVccId(), BusinessTypeEnum.AXE.name(), axeBindingDTO.getRequestId());
        String requestBody = redissonUtil.getString(requestKye);
        if (StringUtils.isNotBlank(requestBody)) {
            log.info("当前 requestId： {} 已绑定 ", axeBindingDTO.getRequestId());
            return Result.ok(JSONObject.parseObject(requestBody));
        }
        HdhAuthInfoVO authInfoVO = privateSupplierInfoService.getAuthInfo(supplierId, OperateTypeEnum.BINDING);
        String vccId = axeBindingDTO.getVccId();
        HdhAxeBindDTO hdhAxeBindDTO = axeBindConverter.axeBindingDTO2HdhAxeBindDto(axeBindingDTO);
        if (axeBindingDTO.getEnableRecord() != null){
            hdhAxeBindDTO.setRecord(String.valueOf(axeBindingDTO.getEnableRecord()));
        }
        parseAxeEntity(axeBindingDTO,hdhAxeBindDTO);
        hdhAxeBindDTO.setAppId(authInfoVO.getHdhAuthDTO().getAppId());
        JSONObject audio = audioAxeCreat(axeBindingDTO);
        hdhAxeBindDTO.setAudio(audio);
        // 字段解析
        hdhAxeBindDTO = ThirdUtils.trimBlankString(hdhAxeBindDTO);
        String msgRequest = JSON.toJSONString(hdhAxeBindDTO);

        return resolveAxeResult(supplierId, request(authInfoVO, vccId, supplierId, msgRequest), axeBindingDTO, requestKye);
    }

    @Override
    public Result axeUnbind(UnBindDTO unBindDTO, String supplierId) {
        String vccId = unBindDTO.getVccId();
        String cqtBindId = unBindDTO.getBindId();
        String jsonMapper = getThirdBind(vccId, cqtBindId);
        if (StringUtils.isNotBlank(jsonMapper)) {
            JSONObject jsonObject = JSONObject.parseObject(jsonMapper);
            String hdhBindId = jsonObject.getString("bindId");
            String number = jsonObject.getString("number");
            // 传输实体构建
            HdhAxeUnBindDTO hdhAxeUnBindDTO = new HdhAxeUnBindDTO();
            HdhAuthInfoVO authInfoVO = privateSupplierInfoService.getAuthInfo(supplierId, OperateTypeEnum.UNBIND);
            hdhAxeUnBindDTO.setAppId(authInfoVO.getHdhAuthDTO().getAppId());
            hdhAxeUnBindDTO.setBindId(hdhBindId);
            hdhAxeUnBindDTO.setCoolDown(1);
            String msgRequest = JSONObject.toJSONString(hdhAxeUnBindDTO);
            HdhAxeResult hdhAxeResult = JSONObject.parseObject(request(authInfoVO, vccId, supplierId, msgRequest), HdhAxeResult.class);
            if (ThirdConstant.HDH_SUCCESS_CODE.equals(hdhAxeResult.getCode())) {
                PrivateBindInfoAxe build = PrivateBindInfoAxe.builder()
                        .bindId(unBindDTO.getBindId())
                        .vccId(unBindDTO.getVccId())
                        .telX(number)
                        .build();
                // 删除数据库 redis
                saveExecutor.execute(() -> {
                    MqBindInfoBO mqBindInfoBO = MqBindInfoBO.builder()
                            .operateType(OperateTypeEnum.DELETE.name())
                            .vccId(vccId)
                            .numType(BusinessTypeEnum.AXE.name())
                            .privateBindInfoAxe(build)
                            .build();
                    privateMqProducer.sendMessage(RabbitMqConfig.BIND_DB_EXCHANGE,RabbitMqConfig.BIND_DB_DELETE_QUEUE,mqBindInfoBO);
                });
                return Result.ok();
            }
            return Result.fail(Integer.valueOf(hdhAxeResult.getCode()), hdhAxeResult.getMessage());
        } else {
            // 未查询到第三方 绑定id
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
    }

    @Override
    public Result unbind(UnBindDTO unBindDTO, String supplierId) {
        String vccId = unBindDTO.getVccId();
        String cqtBindId = unBindDTO.getBindId();
        String jsonMapper = getThirdBind(vccId, cqtBindId);
        if (StringUtils.isNotBlank(jsonMapper)) {
            JSONObject jsonObject = JSONObject.parseObject(jsonMapper);
            String hdhBindId = jsonObject.getString("bindId");
            String requestId = jsonObject.getString("requestId");
            // 传输实体构建
            HdhAxbUnBindDTO hdhAxbUnBindDTO = new HdhAxbUnBindDTO();
            HdhAuthInfoVO authInfoVO = privateSupplierInfoService.getAuthInfo(supplierId, OperateTypeEnum.UNBIND);
            hdhAxbUnBindDTO.setAppId(authInfoVO.getHdhAuthDTO().getAppId());
            hdhAxbUnBindDTO.setBindId(hdhBindId);
            String msgRequest = JSONObject.toJSONString(hdhAxbUnBindDTO);
            HdhAxbResult hdhAxbResult = JSONObject.parseObject(request(authInfoVO, vccId, supplierId, msgRequest), HdhAxbResult.class);
            if (ThirdConstant.HDH_SUCCESS_CODE.equals(hdhAxbResult.getCode())) {
                // 删除数据库 redis
                saveExecutor.execute(() -> {
                    try (HintManager hintManager = HintManager.getInstance()) {
                        hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
                        int update = privateBindInfoAxbMapper.deleteById(cqtBindId);
                        log.info("vccId: {}, update axb : {}", vccId, update);

                        // 删除redis
                        String requestKye = PrivateCacheUtil.getRequestIdKey(vccId, BusinessTypeEnum.AXB.name(), requestId);
                        String bindMapperKey = PrivateCacheUtil.getThirdBindMapperKey(vccId, cqtBindId);
                        String cqtBindMapperKey = PrivateCacheUtil.getBindMapperKey(vccId, hdhBindId);
                        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, BusinessTypeEnum.AXB.name(), cqtBindId);
                        redissonUtil.delKey(bindMapperKey);
                        redissonUtil.delKey(requestKye);
                        redissonUtil.delKey(cqtBindMapperKey);
                        redissonUtil.delKey(bindIdKey);
                    } catch (Exception e) {
                        log.error("vccId: {}, update axb error: {}", vccId, e);
                    }
                });
                return Result.ok();
            }
            return Result.fail(Integer.valueOf(hdhAxbResult.getCode()), hdhAxbResult.getMessage());
        } else {
            // 未查询到第三方 绑定id
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
    }

    @Override
    public Result updateExpirationBind(UpdateExpirationDTO updateExpirationDTO, String supplierId) {
        String vccId = updateExpirationDTO.getVccId();
        String cqtBindId = updateExpirationDTO.getBindId();
        Long delay = updateExpirationDTO.getExpiration();
        Long oldDelay = delay;
        ZoneId zoneId;
        zoneId = ZoneId.systemDefault();
        Date date = new Date();

        // 获取对应的第三方bindId
        String jsonMapper = getThirdBind(vccId, cqtBindId);
        if (StringUtils.isNotBlank(jsonMapper)) {
            JSONObject jsonObject = JSONObject.parseObject(jsonMapper);
            String hdhBindId = jsonObject.getString("bindId");
            String requestId = jsonObject.getString("requestId");
            // 获取绑定关系过期时间

            String oldExpiration = jsonObject.getString("expiration");
            Date oldDate = ThirdUtils.string2Date(oldExpiration);
            Date newDate = ThirdUtils.plusSeconds(date.toInstant().atZone(zoneId).toLocalDateTime(), delay);
            // 判断是否延长
            int flag = DateUtil.compare(oldDate, newDate);
            if (flag >= 1) {
                log.info("当前延长时间早于过期时间");
                return Result.fail(-1, "当前延长时间早于过期时间");
            }
            delay = DateUtil.between(oldDate, newDate, DateUnit.SECOND);
            // 传输实体构建
            HdhAxbDelayBindDTO hdhAxbDelayBindDTO = new HdhAxbDelayBindDTO();
            HdhAuthInfoVO authInfoVO = privateSupplierInfoService.getAuthInfo(supplierId, OperateTypeEnum.UPDATE_EXPIRE);

            hdhAxbDelayBindDTO.setAppId(authInfoVO.getHdhAuthDTO().getAppId());
            hdhAxbDelayBindDTO.setBindId(hdhBindId);
            hdhAxbDelayBindDTO.setDelta(Math.toIntExact(delay));
            String msgRequest = JSONObject.toJSONString(hdhAxbDelayBindDTO);
            HdhAxbResult hdhAxbResult = JSONObject.parseObject(request(authInfoVO, vccId, supplierId, msgRequest), HdhAxbResult.class);
            if (ThirdConstant.HDH_SUCCESS_CODE.equals(hdhAxbResult.getCode())) {
                // 更新数据库 有效时间 redis mq
                Long finalDelay = delay;
                saveExecutor.execute(() -> {
                    try (HintManager hintManager = HintManager.getInstance()) {
                        hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
                        PrivateBindInfoAxb privateBindInfoAxb = privateBindInfoAxbMapper.selectById(cqtBindId);
                        privateBindInfoAxb.setUpdateTime(date);
                        privateBindInfoAxb.setExpiration(oldDelay);
                        Date newExpireTime = ThirdUtils.plusSeconds(privateBindInfoAxb.getExpireTime().toInstant().atZone(zoneId).toLocalDateTime(), finalDelay);
                        privateBindInfoAxb.setExpireTime(newExpireTime);
                        int update = privateBindInfoAxbMapper.updateById(privateBindInfoAxb);
                        // 修改redis
                        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(privateBindInfoAxb.getVccId(), BusinessTypeEnum.AXB.name(), privateBindInfoAxb.getTelA(), privateBindInfoAxb.getTelX());
                        String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(privateBindInfoAxb.getVccId(), BusinessTypeEnum.AXB.name(), privateBindInfoAxb.getTelB(), privateBindInfoAxb.getTelX());
                        String bindInfoJson = JSON.toJSONString(privateBindInfoAxb);
                        redissonUtil.setObject(axBindInfoKey, bindInfoJson, oldDelay, TimeUnit.SECONDS);
                        redissonUtil.setObject(bxBindInfoKey, bindInfoJson, oldDelay, TimeUnit.SECONDS);
                        // 映射关系 过期时间更新
                        String bindMapperKey = PrivateCacheUtil.getThirdBindMapperKey(vccId, cqtBindId);
                        jsonObject.put("expiration", ThirdUtils.date2String(newExpireTime));
                        String requestKye = PrivateCacheUtil.getRequestIdKey(vccId, BusinessTypeEnum.AXB.name(), requestId);
                        redissonUtil.setObject(bindMapperKey, jsonObject, oldDelay, TimeUnit.SECONDS);
                        redissonUtil.setTTL(requestKye, oldDelay);
                        String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, BusinessTypeEnum.AXB.name(), cqtBindId);
                        redissonUtil.setTTL(bindIdKey, oldDelay);
                        // 第三方与本地映射
                        String binMapperKey = PrivateCacheUtil.getBindMapperKey(vccId, cqtBindId);
                        redissonUtil.setTTL(binMapperKey, oldDelay);
                        // 发送mq
                        saveMq(privateBindInfoAxb);
                        log.info("vccId: {}, update axb : {}", vccId, update);
                    } catch (Exception e) {
                        log.error("vccId: {}, update axb error: {}", vccId, e);
                    }
                });
                return Result.ok();
            }
            return Result.fail(Integer.valueOf(hdhAxbResult.getCode()), hdhAxbResult.getMessage());
        } else {
            // 未查询到第三方 绑定id
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
    }

    @Override
    public Boolean match(String supplierId) {
        return supplierId.startsWith(SUPPLIER_PREFIX);
    }

    @Override
    public Result updateAxeExpirationBind(UpdateExpirationDTO updateExpirationDTO, String supplierId) {
        String vccId = updateExpirationDTO.getVccId();
        String cqtBindId = updateExpirationDTO.getBindId();
        Long delay = updateExpirationDTO.getExpiration();
        Long oldDelay = delay;
        ZoneId zoneId;
        zoneId = ZoneId.systemDefault();
        Date date = new Date();

        // 获取对应的第三方bindId
        String jsonMapper = getThirdBind(vccId, cqtBindId);
        if (StringUtils.isNotBlank(jsonMapper)) {
            JSONObject jsonObject = JSONObject.parseObject(jsonMapper);
            String hdhBindId = jsonObject.getString("bindId");
            String requestId = jsonObject.getString("requestId");
            // 获取绑定关系过期时间
            String oldExpiration = jsonObject.getString("expiration");
            Date oldDate = ThirdUtils.string2Date(oldExpiration);
            Date newDate = ThirdUtils.plusSeconds(date.toInstant().atZone(zoneId).toLocalDateTime(), delay);
            // 判断是否延长
            int flag = DateUtil.compare(oldDate, newDate);
            if (flag >= 1) {
                log.info("当前延长时间早于过期时间");
                return Result.fail(-1, "当前延长时间早于过期时间");
            }
            delay = DateUtil.between(oldDate, newDate, DateUnit.SECOND);
            // 传输实体构建
            HdhAxbDelayBindDTO hdhAxbDelayBindDTO = new HdhAxbDelayBindDTO();
            HdhAuthInfoVO authInfoVO = privateSupplierInfoService.getAuthInfo(supplierId, OperateTypeEnum.UPDATE_EXPIRE);

            hdhAxbDelayBindDTO.setAppId(authInfoVO.getHdhAuthDTO().getAppId());
            hdhAxbDelayBindDTO.setBindId(hdhBindId);
            hdhAxbDelayBindDTO.setDelta(Math.toIntExact(delay));
            String msgRequest = JSONObject.toJSONString(hdhAxbDelayBindDTO);
            HdhAxbResult hdhAxbResult = JSONObject.parseObject(request(authInfoVO, vccId, supplierId, msgRequest), HdhAxbResult.class);
            if (ThirdConstant.HDH_SUCCESS_CODE.equals(hdhAxbResult.getCode())) {
                // 更新数据库 有效时间 redis mq
                Long finalDelay = delay;
                saveExecutor.execute(() -> {
                    try (HintManager hintManager = HintManager.getInstance()) {
                        hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, vccId);
                        PrivateBindInfoAxe privateBindInfoAxe = privateBindInfoAxeMapper.selectById(cqtBindId);
                        privateBindInfoAxe.setUpdateTime(date);
                        privateBindInfoAxe.setExpiration(oldDelay);
                        Date newExpireTime = ThirdUtils.plusSeconds(privateBindInfoAxe.getExpireTime().toInstant().atZone(zoneId).toLocalDateTime(), finalDelay);
                        privateBindInfoAxe.setExpireTime(newExpireTime);
                        MqBindInfoBO mqBindInfoBO = MqBindInfoBO.builder()
                                .operateType(OperateTypeEnum.UPDATE.name())
                                .vccId(vccId)
                                .numType(BusinessTypeEnum.AXE.name())
                                .privateBindInfoAxe(privateBindInfoAxe)
                                .build();
                        // 映射关系 过期时间更新
                        String bindMapperKey = PrivateCacheUtil.getThirdBindMapperKey(vccId, cqtBindId);
                        jsonObject.put("expiration", ThirdUtils.date2String(newExpireTime));
                        String requestKye = PrivateCacheUtil.getRequestIdKey(vccId, BusinessTypeEnum.AXE.name(), requestId);
                        redissonUtil.setTTL(requestKye, oldDelay);
                        redissonUtil.setObject(bindMapperKey, jsonObject, oldDelay, TimeUnit.SECONDS);
                        privateMqProducer.sendMessage(RabbitMqConfig.BIND_DB_EXCHANGE,RabbitMqConfig.BIND_DB_UPDATE_QUEUE,mqBindInfoBO);
                    } catch (Exception e) {
                        log.error("vccId: {}, update axe error: {}", vccId, e);
                    }
                });
                return Result.ok();
            }
            return Result.fail(Integer.valueOf(hdhAxbResult.getCode()), hdhAxbResult.getMessage());
        } else {
            // 未查询到第三方 绑定id
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
    }

    /**
     * 请求第三方
     */
    private String request(HdhAuthInfoVO authInfoVO, String vccId, String supplierId, String msgRequest) {
        String url = authInfoVO.getUrl();
        log.info("vccId {}, 请求第三方供应商 type: {}, url: {}, requestBody: {}", vccId, supplierId, url, msgRequest);
        try (HttpResponse response = HttpRequest.post(url)
                .body(msgRequest)
                .header(HttpHeaders.AUTHORIZATION, authInfoVO.getHeader())
                .timeout(10000)
                .execute()) {
            String body = response.body();
            log.info("vccId {}, 请求第三方供应商: {}, url: {}, response: {}", vccId, supplierId, url, body);
            if (response.isOk()) {
                return body;
            }
            warnDingDing(url, supplierId, "请求状态码非200: " + response.getStatus());
        } catch (Exception e) {
            log.error("vccId {}, 请求第三方接口: {}, 异常: ", vccId, url, e);
            warnDingDing(url, supplierId, e.getMessage());
        }
        throw new ParamsException("第三方接口请求失败.");
    }

    /**
     * (hdh) 实体字段处理
     */
    private void parseEntity(AxbBindingDTO axbBindingDTO, HdhAxbBindDTO hdhAxbBindDTO) {
        String telA = axbBindingDTO.getTelA();
        String telB = axbBindingDTO.getTelB();
        String areaCode = axbBindingDTO.getAreaCode();
        String typeA = ThirdUtils.numberType(telA);
        hdhAxbBindDTO.setTelA(ThirdUtils.parseNumber(telA, typeA, Integer.valueOf(areaCode)));
        String typeB = ThirdUtils.numberType(telB);
        hdhAxbBindDTO.setTelB(ThirdUtils.parseNumber(telB, typeB, Integer.valueOf(areaCode)));
    }

    private void parseAxeEntity(AxeBindingDTO axeBindingDTO, HdhAxeBindDTO hdhAxeBindDTO) {
        String telA = axeBindingDTO.getTel();
        String areaCode = axeBindingDTO.getAreaCode();
        String typeA = ThirdUtils.numberType(telA);
        hdhAxeBindDTO.setTelA(ThirdUtils.parseNumber(telA, typeA, Integer.valueOf(areaCode)));
    }

    /**
     * (hdh) 解绑更新 获取bindId
     */
    private String getThirdBind(String vccId, String cqtBindId) {
        String bindMapperKey = PrivateCacheUtil.getThirdBindMapperKey(vccId, cqtBindId);
        // 获取对应的第三方bindId

        return redissonUtil.getString(bindMapperKey);
    }

    /**
     * (hdh) 放应编码处理
     */
    private JSONObject audioCreat(AxbBindingDTO axbBindingDTO) {
        Map<String, Object> audioMap = new HashMap<>(16);
        String prefix = axbBindingDTO.getVccId() + "/";
        String aCallX = LocalCacheService.HDH_AUDIO_CODE_CACHE.get(prefix + axbBindingDTO.getAudioACallXBefore());
        if (StringUtils.isNotBlank(aCallX)) {
            audioMap.put("ACallX", aCallX);
        }
        String bCallX = LocalCacheService.HDH_AUDIO_CODE_CACHE.get(prefix + axbBindingDTO.getAudioBCallXBefore());
        if (StringUtils.isNotBlank(bCallX)) {
            audioMap.put("BCallX", bCallX);
        }
        String aCallXAnswer = LocalCacheService.HDH_AUDIO_CODE_CACHE.get(prefix + axbBindingDTO.getAudioACallX());
        if (StringUtils.isNotBlank(aCallXAnswer)) {
            audioMap.put("AOrBCallXAnswer", aCallXAnswer);
        }
        if (audioMap.size() >= 1) {
            return new JSONObject(audioMap);
        }
        return null;
    }

    private JSONObject audioAxeCreat(AxeBindingDTO axeBindingDTO) {
        Map<String, Object> audioMap = new HashMap<>(16);
        String prefix = axeBindingDTO.getVccId() + "/";
        String cCallX = LocalCacheService.HDH_AUDIO_CODE_CACHE.get(prefix + axeBindingDTO.getAybAudioBCallXBefore());
        if (StringUtils.isNotBlank(cCallX)) {
            audioMap.put("CCallX", cCallX);
        }
        String cCallXAnswer = LocalCacheService.HDH_AUDIO_CODE_CACHE.get(prefix + axeBindingDTO.getAudioCalled());
        if (StringUtils.isNotBlank(cCallXAnswer)) {
            audioMap.put("CCallXAnswer", cCallXAnswer);
        }

        if (audioMap.size() >= 1) {
            return new JSONObject(audioMap);
        }
        return null;
    }

    /**
     * (hdh) 上传mq
     */
    private void saveMq(PrivateBindInfoAxb bindInfoAxb) {
        // 上传mq
        long expiration = bindInfoAxb.getExpiration();
        BindRecycleDTO bindRecycleDTO = axbBindConverter.bindInfoAxb2BindRecycleDTO(bindInfoAxb);
        bindRecycleDTO.setNumType(BusinessTypeEnum.AXB.name());
        privateMqProducer.sendLazy(Optional.of(bindRecycleDTO), (int) expiration);
    }

    private void saveMq(PrivateBindInfoAxe bindInfoAxe) {
        // 上传mq
        long expiration = bindInfoAxe.getExpiration();
        BindRecycleDTO bindRecycleDTO = axeBindConverter.bindInfoAxe2BindRecycleDTO(bindInfoAxe);
        bindRecycleDTO.setNumType(BusinessTypeEnum.AXE.name());
        privateMqProducer.sendLazy(Optional.of(bindRecycleDTO), (int) expiration);
    }

    /**
     * 失败钉钉告警
     */
    private void warnDingDing(String url, String supplierId, String eMessage) {
        try {
            saveExecutor.execute(() -> {
                String countKey = PrivateCacheUtil.getThirdSupplierExceptionCountKey(supplierId);
                boolean flag = redissonUtil.isExistZsetKey(countKey);
                if (!flag) {
                    redissonUtil.setObject(countKey, 0);
                } else {
                    Integer count = Integer.valueOf(redissonUtil.getString(countKey));
                    if (count > thirdProperties.getWarnMax()) {
                        // 钉钉告警
                        String message = String.format(ThirdConstant.warnMessage, ThirdUtils.date2String(new Date()), LocalCacheService.LOCAL_IP, url, eMessage);
                        MessageDTO messageDTO = MessageDTO.builder()
                                .type(ThirdConstant.WARN_TYPE)
                                .content(message)
                                .operateType(ThirdConstant.OPERATE_TYPE)
                                .build();
                        Result result = baseSettingFeignClient.sendMessage(messageDTO);
                        log.info("调用盯盯告警结果 : result: {}", JSONObject.toJSONString(result));
                        redissonUtil.delKey(countKey);
                    } else {
                        count++;
                        redissonUtil.setObject(countKey, count);
                    }

                }
            });
        } catch (Exception ex) {
            log.error("调用钉钉告警失败: ", ex);
        }
    }


    /**
     * (hdh) 绑定结果处理
     */
    private Result resolveResult(String supplierId, String resultJson, AxbBindingDTO axbBindingDTO, String requestKye) {
        Date bindDate = new Date();
        String bindTime = ThirdUtils.date2String(bindDate);
        log.info("当前绑定时间 bindTime：{}", bindTime);
        HdhAxbResult hdhAxbResult = JSONObject.parseObject(resultJson, HdhAxbResult.class);
        long expiration = axbBindingDTO.getExpiration() + 3600L;
        if (ThirdConstant.HDH_SUCCESS_CODE.equals(hdhAxbResult.getCode())) {
            String xNo = hdhAxbResult.getX_no();
            String reXno = ThirdUtils.getNumberUn86(xNo);
            hdhAxbResult.setX_no(reXno);
            // 成功处理 bindId
            String cqtBindId = BindIdUtil.getBindId(BusinessTypeEnum.AXB, axbBindingDTO.getAreaCode(), supplierId, xNo);
            log.info("当前 {} 平台 绑定id:{}, 本平台id:{}", supplierId, hdhAxbResult.getBindId(), cqtBindId);
            // 存绑定关系
            Map<String, String> bindMap = new HashMap<>(16);
            bindMap.put("supplierId", supplierId);
            bindMap.put("bindId", hdhAxbResult.getBindId());
            bindMap.put("requestId", axbBindingDTO.getRequestId());
            //
            Map<String, String> cdrBindMap = new HashMap<>(16);
            cdrBindMap.put("cqtBindId", cqtBindId);
            cdrBindMap.put("bindTime", bindTime);
            cdrBindMap.put("requestId", axbBindingDTO.getRequestId());
            cdrBindMap.put("vccId", axbBindingDTO.getVccId());
            cdrBindMap.put("supplierId", supplierId);
            // 存 ax bx
            axbBindingDTO.setBindId(cqtBindId);
            axbBindingDTO.setTelX(hdhAxbResult.getX_no());
            PrivateBindInfoAxb bindInfoAxb = axbBindConverter.axbBindingDto2BindInfoAxb(axbBindingDTO);
            // 地址编码 用区号
            bindInfoAxb.setCityCode(axbBindingDTO.getAreaCode());
            if (null == bindInfoAxb.getWholeArea()) {
                bindInfoAxb.setWholeArea(0);
            }
            if (null == bindInfoAxb.getType()) {
                bindInfoAxb.setType(0);
            }
            if (null == bindInfoAxb.getRecordFileFormat()) {
                bindInfoAxb.setRecordFileFormat("wav");
            }
            if (null == bindInfoAxb.getModel()) {
                bindInfoAxb.setModel(2);
            }
            if (null == bindInfoAxb.getRecordMode()) {
                bindInfoAxb.setRecordMode(1);
            }
            if (null == bindInfoAxb.getDualRecordMode()) {
                bindInfoAxb.setDualRecordMode(0);
            }
            if (null == bindInfoAxb.getMaxDuration()) {
                bindInfoAxb.setMaxDuration(7200);
            }
            bindInfoAxb.setEnableRecord(1);
            bindInfoAxb.setTelA(axbBindingDTO.getTelA());
            bindInfoAxb.setTelB(axbBindingDTO.getTelB());
            String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(axbBindingDTO.getVccId(), BusinessTypeEnum.AXB.name(), axbBindingDTO.getTelA(), hdhAxbResult.getX_no());
            String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(axbBindingDTO.getVccId(), BusinessTypeEnum.AXB.name(), axbBindingDTO.getTelB(), hdhAxbResult.getX_no());
            String bindInfoJson = JSON.toJSONString(bindInfoAxb);
            redissonUtil.setObject(axBindInfoKey, bindInfoJson, expiration, TimeUnit.SECONDS);
            redissonUtil.setObject(bxBindInfoKey, bindInfoJson, expiration, TimeUnit.SECONDS);
            // 第三方与本地映射
            String bindMapper = PrivateCacheUtil.getBindMapperKey(hdhAxbResult.getBindId());
            redissonUtil.setObject(bindMapper, JSON.toJSONString(cdrBindMap), expiration, TimeUnit.SECONDS);
            // 存requestId
            Map<String, String> requestMap = new HashMap<>(5);
            requestMap.put("tel_x", reXno);
            requestMap.put("bind_id", cqtBindId);
            redissonUtil.setObject(requestKye, JSON.toJSONString(requestMap), expiration, TimeUnit.SECONDS);
            // 保存数据库
            String vccId = axbBindingDTO.getVccId();
            saveExecutor.execute(() -> {
                try (HintManager hintManager = HintManager.getInstance()) {
                    String date = DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN);

                    hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
                    hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB_HIS, date);

                    ZoneId zoneId = ZoneId.systemDefault();
                    bindInfoAxb.setCreateTime(bindDate);
                    bindInfoAxb.setUpdateTime(bindInfoAxb.getCreateTime());
                    Date expireTime = ThirdUtils.plusSeconds(bindInfoAxb.getCreateTime().toInstant().atZone(zoneId).toLocalDateTime(), bindInfoAxb.getExpiration());

                    bindMap.put("expiration", ThirdUtils.date2String(expireTime));
                    String bindMapJson = JSON.toJSONString(bindMap);
                    redissonUtil.setObject(PrivateCacheUtil.getThirdBindMapperKey(axbBindingDTO.getVccId(), cqtBindId), bindMapJson, expiration, TimeUnit.SECONDS);
                    String bindIdKey = PrivateCacheUtil.getBindIdKey(axbBindingDTO.getVccId(), BusinessTypeEnum.AXB.name(), cqtBindId);
                    redissonUtil.setObject(bindIdKey, bindMapJson, expiration, TimeUnit.SECONDS);
                    bindInfoAxb.setExpireTime(expireTime);
                    bindInfoAxb.setSupplierId(supplierId);
                    bindInfoAxb.setSourceBindId(hdhAxbResult.getBindId());
                    PrivateBindInfoAxbHis privateBindInfoAxbHis = axbBindConverter.bindInfoAxb2BindInfoAxbHis(bindInfoAxb);
                    int insert = privateBindInfoAxbMapper.insert(bindInfoAxb);
                    int insertHis = privateBindInfoAxbHisMapper.insert(privateBindInfoAxbHis);
                    log.info("vccId: {}, insert axb : {}, insert axb  his: {}", vccId, insert, insertHis);
                } catch (Exception e) {
                    log.error("vccId: {}, insert axb error: {}", vccId, e);
                }
            });
            // 上传mq
            saveMq(bindInfoAxb);
            // 返回
            AxbBindingVO axbBindingVO = axbBindConverter.bindInfoAxb2AxbBindingVO(bindInfoAxb);
            return Result.ok(axbBindingVO);
        }
        return Result.fail(Integer.valueOf(hdhAxbResult.getCode()), hdhAxbResult.getMessage());
    }

    private Result resolveAxeResult(String supplierId, String resultJson, AxeBindingDTO axeBindingDTO, String requestKye) {
        Date bindDate = new Date();
        String bindTime = ThirdUtils.date2String(bindDate);
        log.info("当前绑定时间 bindTime：{}", bindTime);
        HdhAxeResult hdhAxeResult = JSONObject.parseObject(resultJson, HdhAxeResult.class);
        long expiration = axeBindingDTO.getExpiration() + 3600L;
        if (ThirdConstant.HDH_SUCCESS_CODE.equals(hdhAxeResult.getCode())) {
            String xNo = hdhAxeResult.getTelX();
            String reXno = ThirdUtils.getNumberUn86(xNo);
            hdhAxeResult.setTelX(reXno);
            // 成功处理 bindId
            String cqtBindId = BindIdUtil.getBindId(BusinessTypeEnum.AXE, axeBindingDTO.getAreaCode(), supplierId, xNo);
            log.info("当前 {} 平台 绑定id:{}, 本平台id:{}", supplierId, hdhAxeResult.getBindId(), cqtBindId);
            // 存绑定关系
            Map<String, String> bindMap = new HashMap<>(16);
            bindMap.put("supplierId", supplierId);
            bindMap.put("bindId", hdhAxeResult.getBindId());
            bindMap.put("requestId", axeBindingDTO.getRequestId());
            bindMap.put("number", hdhAxeResult.getTelX());
            //
            Map<String, String> cdrBindMap = new HashMap<>(16);
            cdrBindMap.put("cqtBindId", cqtBindId);
            cdrBindMap.put("bindTime", bindTime);
            cdrBindMap.put("requestId", axeBindingDTO.getRequestId());
            cdrBindMap.put("vccId", axeBindingDTO.getVccId());
            cdrBindMap.put("supplierId", supplierId);
            // 存 ax bx
            axeBindingDTO.setTelX(hdhAxeResult.getTelX());
            PrivateBindInfoAxe bindInfoAxe = axeBindConverter.axeBindingDto2BindInfoAxe(axeBindingDTO);
            bindInfoAxe.setBindId(cqtBindId);
            bindInfoAxe.setTelXExt(hdhAxeResult.getExtNumber());
            bindInfoAxe.setCreateTime(bindDate);
            // 地址编码 用区号
            bindInfoAxe.setCityCode(axeBindingDTO.getAreaCode());
            if (null == bindInfoAxe.getWholeArea()) {
                bindInfoAxe.setWholeArea(0);
            }
            if (null == bindInfoAxe.getType()) {
                bindInfoAxe.setType(0);
            }
            if (null == bindInfoAxe.getRecordFileFormat()) {
                bindInfoAxe.setRecordFileFormat("wav");
            }
            if (null == bindInfoAxe.getModel()) {
                bindInfoAxe.setModel(2);
            }
            if (null == bindInfoAxe.getRecordMode()) {
                bindInfoAxe.setRecordMode(1);
            }
            if (null == bindInfoAxe.getDualRecordMode()) {
                bindInfoAxe.setDualRecordMode(0);
            }
            if (null == bindInfoAxe.getMaxDuration()) {
                bindInfoAxe.setMaxDuration(7200);
            }
            bindInfoAxe.setEnableRecord(1);
            bindInfoAxe.setTel(axeBindingDTO.getTel());
            bindInfoAxe.setTelX(axeBindingDTO.getTelX());
            bindInfoAxe.setExpiration(expiration);
            ZoneId zoneId = ZoneId.systemDefault();

            Date expireTime = ThirdUtils.plusSeconds(bindInfoAxe.getCreateTime().toInstant().atZone(zoneId).toLocalDateTime(), bindInfoAxe.getExpiration());
            bindInfoAxe.setExpireTime(expireTime);
            String axBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(axeBindingDTO.getVccId(), BusinessTypeEnum.AXE.name(), axeBindingDTO.getTelX(), hdhAxeResult.getExtNumber());
            String bindInfoJson = JSON.toJSONString(bindInfoAxe);
            redissonUtil.setObject(axBindInfoKey, bindInfoJson, expiration, TimeUnit.SECONDS);
            // 第三方与本地映射
            String bindMapper = PrivateCacheUtil.getBindMapperKey(hdhAxeResult.getBindId());
            redissonUtil.setObject(bindMapper, JSON.toJSONString(cdrBindMap), expiration, TimeUnit.SECONDS);
            // 存requestId
            Map<String, String> requestMap = new HashMap<>(5);
            requestMap.put("tel_x", reXno);
            requestMap.put("ext", hdhAxeResult.getExtNumber());
            requestMap.put("bind_id", cqtBindId);
            redissonUtil.setObject(requestKye, JSON.toJSONString(requestMap), expiration, TimeUnit.SECONDS);
            // 保存数据库
            String vccId = axeBindingDTO.getVccId();
            saveExecutor.execute(() -> {
                MqBindInfoBO mqBindInfoBO = MqBindInfoBO.builder()
                        .operateType(OperateTypeEnum.INSERT.name())
                        .vccId(vccId)
                        .numType(BusinessTypeEnum.AXE.name())
                        .privateBindInfoAxe(bindInfoAxe)
                        .build();
                log.info("发送mq入库报文："+JSONObject.toJSONString(mqBindInfoBO));
                bindMap.put("expiration", ThirdUtils.date2String(expireTime));
                String bindMapJson = JSON.toJSONString(bindMap);
                redissonUtil.setObject(PrivateCacheUtil.getThirdBindMapperKey(axeBindingDTO.getVccId(), cqtBindId), bindMapJson, expiration, TimeUnit.SECONDS);
                String bindIdKey = PrivateCacheUtil.getBindIdKey(axeBindingDTO.getVccId(), BusinessTypeEnum.AXB.name(), cqtBindId);
                redissonUtil.setObject(bindIdKey, bindMapJson, expiration, TimeUnit.SECONDS);
                privateMqProducer.sendMessage(RabbitMqConfig.BIND_DB_EXCHANGE,RabbitMqConfig.BIND_DB_INSERT_QUEUE,mqBindInfoBO);
            });
            // 返回
            AxeBindingVO axeBindingVO = axeBindConverter.bindInfoAxe2AxeBindingVO(bindInfoAxe);
            axeBindingVO.setTelXExt(hdhAxeResult.getExtNumber());
            return Result.ok(axeBindingVO);
        }
        return Result.fail(Integer.valueOf(hdhAxeResult.getCode()), hdhAxeResult.getMessage());
    }



}
