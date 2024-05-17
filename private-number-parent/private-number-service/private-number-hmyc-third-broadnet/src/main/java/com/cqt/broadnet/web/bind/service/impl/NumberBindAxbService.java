package com.cqt.broadnet.web.bind.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.cqt.broadnet.common.constants.BroadNetCodeConstant;
import com.cqt.broadnet.common.constants.BroadNetHeaderConstant;
import com.cqt.broadnet.common.model.axb.converter.AxbBindConverter;
import com.cqt.broadnet.common.model.axb.dto.BroadNetAxbBindDTO;
import com.cqt.broadnet.common.model.axb.dto.BroadNetAxbUpdateBindDTO;
import com.cqt.broadnet.common.model.axb.vo.BaseBroadNetVO;
import com.cqt.broadnet.common.model.axb.vo.BroadNetAxbBindVO;
import com.cqt.broadnet.common.model.axb.vo.BroadNetAxbUpdateBindVO;
import com.cqt.broadnet.common.model.x.properties.PrivateNumberBindProperties;
import com.cqt.broadnet.config.BizException;
import com.cqt.broadnet.web.bind.mapper.axb.PrivateBindInfoAxbHisMapper;
import com.cqt.broadnet.web.bind.mapper.axb.PrivateBindInfoAxbMapper;
import com.cqt.broadnet.web.bind.service.PrivateSupplierInfoService;
import com.cqt.broadnet.web.bind.util.SignUtil;
import com.cqt.broadnet.web.manager.PrivateMqProducer;
import com.cqt.cloud.api.basesetting.BaseSettingFeignClient;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.constants.ThirdConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.JavaUtils;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.common.util.ThirdUtils;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxbHis;
import com.cqt.model.bind.axb.vo.AxbBindingVO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.MessageDTO;
import com.cqt.model.common.Result;
import com.cqt.model.supplier.PrivateSupplierInfo;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 广电AXB绑定接口管理
 *
 * @author Xienx
 * @date 2023-05-25 11:04:11:04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NumberBindAxbService implements BroadNetHeaderConstant, BroadNetCodeConstant {

    private final RedissonUtil redissonUtil;
    private final PrivateMqProducer privateMqProducer;
    private final ThreadPoolTaskExecutor saveExecutor;
    private final BaseSettingFeignClient baseSettingFeignClient;
    private final NacosDiscoveryProperties nacosDiscoveryProperties;
    private final PrivateBindInfoAxbMapper privateBindInfoAxbMapper;
    private final PrivateSupplierInfoService privateSupplierInfoService;
    private final PrivateBindInfoAxbHisMapper privateBindInfoAxbHisMapper;
    private final PrivateNumberBindProperties privateNumberBindProperties;

    /**
     * 请求广电AXB绑定
     *
     * @param axbBindingDTO AXB绑定请求参数
     * @param supplierId    供应商id
     * @return Result
     */
    public Result binding(AxbBindingDTO axbBindingDTO, String supplierId) {
        log.info("当前第三方 id: {}, 参数: {}", supplierId, JSON.toJSONString(axbBindingDTO));
        PrivateSupplierInfo supplierInfo = privateSupplierInfoService.getSupplierInfo(supplierId);
        String bindUrl = supplierInfo.getBindingUrl();
        JSONObject authInfo = JSON.parseObject(supplierInfo.getSupplierAuthInfo());
        Map<String, String> headers = new HashMap<>();
        headers.put(X_APP_ID, authInfo.getString(X_APP_ID));
        headers.put(X_APP_bizId, authInfo.getString(X_APP_bizId));

        // 生成广电请求参数
        BroadNetAxbBindDTO broadNetAxbBindDTO = new BroadNetAxbBindDTO();
        // 广电的号码需要加地区码前缀
        broadNetAxbBindDTO.setTelA("86" + axbBindingDTO.getTelA());
        broadNetAxbBindDTO.setTelB("86" + axbBindingDTO.getTelB());
        JavaUtils.INSTANCE
                .acceptIfHasText(axbBindingDTO.getTelX(), telX -> broadNetAxbBindDTO.setXNumber("86" + telX));

        broadNetAxbBindDTO.setRequestId(axbBindingDTO.getRequestId());
        broadNetAxbBindDTO.setAreaCode(Integer.parseInt(axbBindingDTO.getAreaCode()));
        broadNetAxbBindDTO.setExpiration(axbBindingDTO.getExpiration());
        // 设置A、B号码放音
        BroadNetAxbBindDTO.Extra extra = new BroadNetAxbBindDTO.Extra();
        extra.setRecord(axbBindingDTO.getEnableRecord());
        if (StringUtil.isNotEmpty(axbBindingDTO.getAudioACallX())){
            extra.setBeepA(axbBindingDTO.getAudioACallX().contains(".wav")? axbBindingDTO.getAudioACallX().replace(".wav","") : axbBindingDTO.getAudioACallX());
        }
        if (StringUtil.isNotEmpty(axbBindingDTO.getAudioBCallX())){
            extra.setBeepB(axbBindingDTO.getAudioBCallX().contains(".wav")? axbBindingDTO.getAudioBCallX().replace(".wav","") : axbBindingDTO.getAudioBCallX());
        }
        broadNetAxbBindDTO.setExtra(extra);
        broadNetAxbBindDTO.setSign(SignUtil.createSignHmacSha256(JSON.toJSONString(broadNetAxbBindDTO), privateNumberBindProperties.getSecretKey(),JSONObject.toJSONString(extra)));
        // 请求广电接口
        String respJson = request(bindUrl, JSON.toJSONString(broadNetAxbBindDTO), axbBindingDTO.getVccId(), Method.POST, supplierId, headers);
        return resolveResult(supplierId, respJson, axbBindingDTO);
    }


    /**
     * (广电) 解绑更新 获取bindId
     */
    private String getThirdBind(String vccId, String cqtBindId) {
        String bindMapperKey = PrivateCacheUtil.getThirdBindMapperKey(vccId, cqtBindId);
        // 获取对应的第三方bindId

        return redissonUtil.getString(bindMapperKey);
    }


    /**
     * 广电AXB解绑
     *
     * @param unBindDTO  AXB解绑请求参数
     * @param supplierId 供应商id
     * @return Result
     */
    public Result unbind(UnBindDTO unBindDTO, String supplierId) {
        log.info("当前第三方 id: {}, 参数: {}", supplierId, JSON.toJSONString(unBindDTO));
        String vccId = unBindDTO.getVccId();
        String cqtBindId = unBindDTO.getBindId();
        String bindInfoJson = getThirdBind(vccId, cqtBindId);
        // 未查询到第三方绑定id
        if (StrUtil.isEmpty(bindInfoJson)) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        JSONObject bindInfo = JSON.parseObject(bindInfoJson);
        String thirdBindId = bindInfo.getString("bindId");
        String requestId = bindInfo.getString("requestId");
        PrivateSupplierInfo supplierInfo = privateSupplierInfoService.getSupplierInfo(supplierId);
        // 解绑的接口地址
        String unBindUrl = supplierInfo.getUnbindUrl() + thirdBindId;

        JSONObject authInfo = JSON.parseObject(supplierInfo.getSupplierAuthInfo());
        Map<String, String> headers = new HashMap<>();
        headers.put(X_APP_ID, authInfo.getString(X_APP_ID));
        headers.put(X_APP_bizId, authInfo.getString(X_APP_bizId));
        // 请求广电接口
        String respJson = request(unBindUrl, null, vccId, Method.DELETE, supplierId, headers);
        BaseBroadNetVO<Void> unBindResp = JSON.parseObject(respJson, new TypeReference<BaseBroadNetVO<Void>>() {
        });
        // 接口响应失败
        if (!SUCCESS.equals(unBindResp.getCode())) {
            return Result.fail(unBindResp.getCode(), unBindResp.getMessage());
        }
        // 异步删除数据库 axb的绑定记录
        saveExecutor.execute(() -> {
            try (HintManager hintManager = HintManager.getInstance()) {
                hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
                int delRes = privateBindInfoAxbMapper.deleteById(cqtBindId);
                log.info("vccId: {}, delete axb record: {}", vccId, delRes);

                // 删除redis
                String requestKey = PrivateCacheUtil.getRequestIdKey(vccId, BusinessTypeEnum.AXB.name(), requestId);
                String bindMapperKey = PrivateCacheUtil.getThirdBindMapperKey(vccId, cqtBindId);
                String cqtBindMapperKey = PrivateCacheUtil.getBindMapperKey(vccId, thirdBindId);
                String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, BusinessTypeEnum.AXB.name(), cqtBindId);
                redissonUtil.delKey(bindIdKey);
                redissonUtil.delKey(requestKey);
                redissonUtil.delKey(bindMapperKey);
                redissonUtil.delKey(cqtBindMapperKey);
            } catch (Exception e) {
                log.error("vccId: {}, unbind axb error: ", vccId, e);
            }
        });
        return Result.ok();
    }

    /**
     * 广电AXB绑定延长有效期
     *
     * @param updateExpirationDTO AXB延长有效期参数
     * @param supplierId          供应商id
     * @return Result
     */
    public Result updateExpirationBind(UpdateExpirationDTO updateExpirationDTO, String supplierId) {
        log.info("当前第三方 id: {}, 参数: {}", supplierId, JSON.toJSONString(updateExpirationDTO));
        String cqtBindId = updateExpirationDTO.getBindId();
        String vccId = updateExpirationDTO.getVccId();
        String bindInfoJson = getThirdBind(vccId, cqtBindId);
        // 未查询到第三方绑定id
        if (StrUtil.isEmpty(bindInfoJson)) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        JSONObject bindInfo = JSON.parseObject(bindInfoJson);
        String thirdBindId = bindInfo.getString("bindId");
        String requestId = bindInfo.getString("requestId");
        String oldExpiration = bindInfo.getString("expiration");
        long expiration = updateExpirationDTO.getExpiration();
        Date now = new Date();
        Date oldExpirationDate = DateUtil.parseDateTime(oldExpiration);
        Date newExpirationDate = DateUtil.offsetSecond(now, Convert.toInt(expiration));
        // 判断是否延长
        if (oldExpirationDate.after(newExpirationDate)) {
            log.info("本次绑定过期时间:{} 早于原绑定过期时间: {}", newExpirationDate, oldExpirationDate);
            return Result.fail(-1, "当前延长时间早于过期时间");
        }
        PrivateSupplierInfo supplierInfo = privateSupplierInfoService.getSupplierInfo(supplierId);
        String updateExpirationUrl = supplierInfo.getUpdateExpirationUrl();
        JSONObject authInfo = JSON.parseObject(supplierInfo.getSupplierAuthInfo());
        // 请求头
        Map<String, String> headers = new HashMap<>();
        headers.put(X_APP_ID, authInfo.getString(X_APP_ID));
        headers.put(X_APP_bizId, authInfo.getString(X_APP_bizId));
        BroadNetAxbUpdateBindDTO broadNetAxbUpdateBindDTO = new BroadNetAxbUpdateBindDTO();
        BroadNetAxbUpdateBindDTO.ModInfo modInfo = new BroadNetAxbUpdateBindDTO.ModInfo();
        // 固定为修改绑定有效期
        broadNetAxbUpdateBindDTO.setRequestType(1);
        broadNetAxbUpdateBindDTO.setModInfo(modInfo);
        modInfo.setBindId(thirdBindId);
        modInfo.setNewExpiration(expiration);

        // 请求广电延长绑定有效期
        String respJson = request(updateExpirationUrl, JSON.toJSONString(broadNetAxbUpdateBindDTO), vccId, Method.POST, supplierId, headers);
        BaseBroadNetVO<BroadNetAxbUpdateBindVO> broadNetAxbUpdateBindVO = JSON.parseObject(respJson, new TypeReference<BaseBroadNetVO<BroadNetAxbUpdateBindVO>>() {
        });
        // 广电接口响应异常
        if (!SUCCESS.equals(broadNetAxbUpdateBindVO.getCode())) {
            return Result.fail(broadNetAxbUpdateBindVO.getCode(), broadNetAxbUpdateBindVO.getMessage());
        }
        saveExecutor.execute(() -> {
            try (HintManager hintManager = HintManager.getInstance()) {
                hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
                // 根据本平台的绑定id查询出绑定记录
                PrivateBindInfoAxb bindInfoAxb = privateBindInfoAxbMapper.selectById(cqtBindId);
                bindInfoAxb.setUpdateTime(now);
                bindInfoAxb.setExpiration(expiration);
                bindInfoAxb.setExpireTime(newExpirationDate);
                int update = privateBindInfoAxbMapper.updateById(bindInfoAxb);
                // 修改redis
                String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, BusinessTypeEnum.AXB.name(), bindInfoAxb.getTelA(), bindInfoAxb.getTelX());
                String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, BusinessTypeEnum.AXB.name(), bindInfoAxb.getTelB(), bindInfoAxb.getTelX());
                String newBindInfoJson = JSON.toJSONString(bindInfoAxb);
                redissonUtil.setObject(axBindInfoKey, newBindInfoJson, expiration, TimeUnit.SECONDS);
                redissonUtil.setObject(bxBindInfoKey, newBindInfoJson, expiration, TimeUnit.SECONDS);
                // 映射关系 过期时间更新
                String bindMapperKey = PrivateCacheUtil.getThirdBindMapperKey(vccId, cqtBindId);
                bindInfo.put("expiration", DateUtil.formatDateTime(newExpirationDate));
                String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, BusinessTypeEnum.AXB.name(), requestId);
                redissonUtil.setObject(bindMapperKey, bindInfo.toJSONString(), expiration, TimeUnit.SECONDS);

                String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, BusinessTypeEnum.AXB.name(), cqtBindId);

                // 第三方与本地映射, 重置缓存过期时间
                String binMapperKey = PrivateCacheUtil.getBindMapperKey(vccId, cqtBindId);
                redissonUtil.setTTL(bindIdKey, expiration);
                redissonUtil.setTTL(binMapperKey, expiration);
                redissonUtil.setTTL(requestIdKey, expiration);

                // 发送mq
//                uploadMq(bindInfoAxb);
                log.info("vccId: {}, update axb : {}", vccId, update);
            } catch (Exception e) {
                log.error("vccId: {}, update axb error: {}", vccId, e);
            }
        });
        return Result.ok();
    }


    private Result resolveResult(String supplierId, String resultJson, AxbBindingDTO axbBindingDTO) {
        Date now = new Date();
        String bindTime = DateUtil.formatDateTime(now);
        log.info("当前绑定时间 : {}", bindTime);
        BaseBroadNetVO<BroadNetAxbBindVO> broadNetAxbBindVO = JSON.parseObject(resultJson, new TypeReference<BaseBroadNetVO<BroadNetAxbBindVO>>() {
        });
        if (SUCCESS.equals(broadNetAxbBindVO.getCode())) {
            // 因为广电返回的X号码是带 86 前缀的, 需要进行处理
            String telX = ThirdUtils.getNumberUn86(broadNetAxbBindVO.getData().getTelX());
            String vccId = axbBindingDTO.getVccId();
            String bindId = broadNetAxbBindVO.getData().getBindId();
            long expiration = axbBindingDTO.getExpiration();
            String cqtBindId = BindIdUtil.getBindId(BusinessTypeEnum.AXB, axbBindingDTO.getAreaCode(), supplierId, telX);
            log.info("当前 {} 平台 绑定id: {}, 本平台id: {}", supplierId, bindId, cqtBindId);

            // 存绑定关系
            Map<String, String> bindMap = new HashMap<>();
            bindMap.put("supplierId", supplierId);
            bindMap.put("bindId", bindId);
            bindMap.put("requestId", axbBindingDTO.getRequestId());

            Map<String, String> cdrBindMap = new HashMap<>();
            cdrBindMap.put("bindTime", bindTime);
            cdrBindMap.put("cqtBindId", cqtBindId);
            cdrBindMap.put("supplierId", supplierId);
            cdrBindMap.put("vccId", vccId);
            cdrBindMap.put("requestId", axbBindingDTO.getRequestId());

            //存ax bx
            axbBindingDTO.setTelX(telX);
            axbBindingDTO.setBindId(cqtBindId);
            PrivateBindInfoAxb bindInfoAxb = AxbBindConverter.INSTANCE.axbBindingDTO2BindInfoAxb(axbBindingDTO);
            // 地址编码 用区号
            bindInfoAxb.setCityCode(axbBindingDTO.getAreaCode());
            // 对部分参数设置缺省值
            JavaUtils.INSTANCE
                    .acceptIfCondition(bindInfoAxb.getWholeArea() == null, 0, bindInfoAxb::setWholeArea)
                    .acceptIfCondition(bindInfoAxb.getType() == null, 0, bindInfoAxb::setType)
                    .acceptIfCondition(bindInfoAxb.getRecordFileFormat() == null, "wav", bindInfoAxb::setRecordFileFormat)
                    .acceptIfCondition(bindInfoAxb.getModel() == null, 2, bindInfoAxb::setModel)
                    .acceptIfCondition(bindInfoAxb.getRecordMode() == null, 1, bindInfoAxb::setRecordMode)
                    .acceptIfCondition(bindInfoAxb.getDualRecordMode() == null, 0, bindInfoAxb::setDualRecordMode)
                    .acceptIfCondition(bindInfoAxb.getMaxDuration() == null, 0, bindInfoAxb::setMaxDuration);

            String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, BusinessTypeEnum.AXB.name(), axbBindingDTO.getTelA(), telX);
            String bxBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, BusinessTypeEnum.AXB.name(), axbBindingDTO.getTelB(), telX);

            String bindInfoJson = JSON.toJSONString(bindInfoAxb);

            redissonUtil.setObject(axBindInfoKey, bindInfoJson, expiration, TimeUnit.SECONDS);
            redissonUtil.setObject(bxBindInfoKey, bindInfoJson, expiration, TimeUnit.SECONDS);
            // 第三方与本地映射
            String bindMapper = PrivateCacheUtil.getBindMapperKey(bindId);
            redissonUtil.setObject(bindMapper, JSON.toJSONString(cdrBindMap), expiration, TimeUnit.SECONDS);

            // 存requestId
            Map<String, String> requestMap = new HashMap<>();
            requestMap.put("tel_x", telX);
            requestMap.put("bind_id", cqtBindId);
            String requestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, BusinessTypeEnum.AXB.name(), axbBindingDTO.getRequestId());

            redissonUtil.setObject(requestIdKey, JSON.toJSONString(requestMap), expiration, TimeUnit.SECONDS);
            // 保存数据库
            saveExecutor.execute(() -> {
                try (HintManager hintManager = HintManager.getInstance()) {
                    String date = DateUtil.format(now, DatePattern.PURE_DATE_PATTERN);
                    hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
                    hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB_HIS, date);

                    bindInfoAxb.setCreateTime(now);
                    bindInfoAxb.setUpdateTime(now);
                    Date expireTime = DateUtil.offsetSecond(now, Convert.toInt(expiration));
                    bindInfoAxb.setExpireTime(expireTime);
                    bindMap.put("expiration", DateUtil.formatDateTime(expireTime));
                    String bindMapJson = JSON.toJSONString(bindMap);
                    redissonUtil.setObject(PrivateCacheUtil.getThirdBindMapperKey(vccId, cqtBindId), bindMapJson, expiration, TimeUnit.SECONDS);
                    String bindIdKey = PrivateCacheUtil.getBindIdKey(vccId, BusinessTypeEnum.AXB.name(), cqtBindId);
                    redissonUtil.setObject(bindIdKey, bindMapJson, expiration, TimeUnit.SECONDS);

                    bindInfoAxb.setSupplierId(supplierId);
                    bindInfoAxb.setSourceBindId(bindId);
                    PrivateBindInfoAxbHis bindInfoAxbHis = AxbBindConverter.INSTANCE.bindInfoAxb2BindInfoAxbHis(bindInfoAxb);
                    int insert = privateBindInfoAxbMapper.insert(bindInfoAxb);
                    int insertHis = privateBindInfoAxbHisMapper.insert(bindInfoAxbHis);

                    log.info("vccId: {}, insert axb : {}, insert axb  his: {}", vccId, insert, insertHis);

                } catch (Exception e) {
                    log.error("vccId: {}, insert axb table error: {}", vccId, e);
                }
            });
            // 上传mq
//            uploadMq(bindInfoAxb);
            // 返回AXB绑定的结果
            AxbBindingVO axbBindingVO = AxbBindConverter.INSTANCE.bindInfoAxb2AxbBindingVO(bindInfoAxb);

            return Result.ok(axbBindingVO);
        }
        return Result.fail(broadNetAxbBindVO.getCode(), broadNetAxbBindVO.getMessage());
    }

    /**
     * (广电) 上传mq
     */
    private void uploadMq(PrivateBindInfoAxb bindInfoAxb) {
        // 上传mq
        long expiration = bindInfoAxb.getExpiration();
        BindRecycleDTO bindRecycleDTO = AxbBindConverter.INSTANCE.bindInfoAxb2BindRecycleDTO(bindInfoAxb);
        bindRecycleDTO.setNumType(BusinessTypeEnum.AXB.name());
        privateMqProducer.sendLazy(Optional.of(bindRecycleDTO), (int) expiration);
    }

    /**
     * AXB 请求广电接口
     */
    private String request(String url, String json, String vccId, Method method, String supplierId, Map<String, String> headers) {
        String errorMsg;
//        String reqUrl = String.format("%s?sign_method=hmac&sign=%s", url, SignUtil.createSignHmacSha256(json, privateNumberBindProperties.getSecretKey()));
        log.info("vccId {}, 请求广电接口: {}, url: {}, 请求header: {}, 请求参数: {}", vccId, supplierId, url, headers, json);
        try (HttpResponse response = HttpRequest.of(url)
                .method(method)
                .headerMap(headers, false)
                .body(json)
                .timeout(10000)
                .execute()) {
            log.info("vccId {}, 请求广电接口: {}, url: {}, response: {}", vccId, supplierId, url, response.body());
            if (response.isOk()) {
                return response.body();
            }
            errorMsg = String.format("非2xx响应, %s: %s", response.getStatus(), response.body());
        } catch (Exception e) {
            log.error("vccId {}, 请求广电接口: {}, 异常: ", vccId, url, e);
            errorMsg = "请求广电接口异常: " + e.getMessage();
        }
        // 这里进行钉钉告警
        warnDingDing(url, supplierId, errorMsg);
        throw new BizException("广电接口请求失败.");
    }

    /**
     * 失败钉钉告警
     */
    private void warnDingDing(String url, String supplierId, String warnMsg) {
        saveExecutor.execute(() -> {
            try {
                String countKey = PrivateCacheUtil.getThirdSupplierExceptionCountKey(supplierId);
                // 先自增
                redissonUtil.increment(countKey);
                int count = Convert.toInt(redissonUtil.getString(countKey), 0);
                // 如果失败的次数超过20次, 则进行告警
                if (count > 20) {
                    // 钉钉告警
                    String message = String.format("【广电平台接口异常告警】\n告警时间: %s\n当前设备: %s\n接口地址: %s\n告警内容: %s\n",
                            DateUtil.now(),
                            nacosDiscoveryProperties.getIp(),
                            url,
                            warnMsg);
                    MessageDTO messageDTO = MessageDTO.builder()
                            .type(ThirdConstant.WARN_TYPE)
                            .content(message)
                            .operateType(ThirdConstant.OPERATE_TYPE)
                            .build();
                    Result result = baseSettingFeignClient.sendMessage(messageDTO);
                    log.info("调用钉钉告警结果 : result: {}", JSONObject.toJSONString(result));
                    redissonUtil.delKey(countKey);
                }
            } catch (Exception ex) {
                log.error("调用钉钉告警失败: ", ex);
            }
        });

    }
}
