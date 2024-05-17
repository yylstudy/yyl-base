package com.cqt.hmyc.web.bind.service.axe;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.*;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.config.balancer.LoadBalancer;
import com.cqt.hmyc.config.exception.ParamsException;
import com.cqt.hmyc.config.exception.PoolLackException;
import com.cqt.hmyc.web.bind.mapper.axe.PrivateBindInfoAxeMapper;
import com.cqt.hmyc.web.cache.NumberPoolAxeCache;
import com.cqt.hmyc.web.cache.PrivateFixedPhoneCache;
import com.cqt.hmyc.web.numpool.mapper.PrivateNumberInfoMapper;
import com.cqt.model.bind.axe.dto.AxeBindIdKeyInfoDTO;
import com.cqt.model.bind.axe.dto.AxeBindingDTO;
import com.cqt.model.bind.axe.dto.AxeUtilizationDTO;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.axe.vo.AxeBindingVO;
import com.cqt.model.bind.axe.vo.AxeUtilizationVO;
import com.cqt.model.bind.axe.vo.GetExtNumVO;
import com.cqt.model.bind.axe.vo.UsablePoolVO;
import com.cqt.model.bind.dto.NumberAreaCodeDTO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;
import com.cqt.model.common.ResultVO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.numpool.vo.NumberAreaCodeCountVO;
import com.cqt.redis.util.RedissonUtil;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date 2021/9/9 14:53
 * AXE 绑定service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AxeBindService {

    private final HideProperties hideProperties;

    private final AxeBindCacheService bindCacheService;

    private final AxeBindConverter axeBindConverter;

    private final LoadBalancer<String> roundRobinLoadBalancer;

    private final AxeAsyncService asyncService;

    private final PrivateBindInfoAxeMapper privateBindInfoAxeMapper;

    private final PrivateNumberInfoMapper privateNumberInfoMapper;

    private final RedissonUtil redissonUtil;

    private final AxeBindCacheService axeBindCacheService;

    public ResultVO<AxeBindingVO> binding(AxeBindingDTO bindingDTO, PrivateCorpBusinessInfoDTO businessInfoDTO, String businessType) {
        paramsCheck(bindingDTO);
        String requestId = bindingDTO.getRequestId();
        String vccId = bindingDTO.getVccId();

        // 1. 查询是否绑定过
        Optional<AxeBindingVO> bindingVoOptional = bindCacheService.getBindInfoByRequestId(vccId, requestId, businessType);
        if (bindingVoOptional.isPresent()) {
            AxeBindingVO bindingVO = bindingVoOptional.get();
            distributeFixedPhone(vccId, bindingDTO.getPhoneFlag(), bindingVO);
            return ResultVO.ok(bindingVO);
        }
        initCustomAudio(bindingDTO);

        // 是否是全国池分配
        boolean isWholeArea = SystemConstant.NUMBER_ONE.equals(bindingDTO.getWholeArea());

        // 指定X号码 @since 2.7.1
        if (StrUtil.isNotEmpty(bindingDTO.getTelX())) {
            return specifyTelX(bindingDTO, businessInfoDTO, businessType, isWholeArea);
        }

        // 指定分机号，未指定X号码 @since 2.7.1
        if (StrUtil.isNotEmpty(bindingDTO.getExtNum())) {
            return specifyExtNum(bindingDTO, businessInfoDTO, businessType, isWholeArea);
        }

        // 平台分配X号码和分机号，
        return distributeExtNumAndTelX(bindingDTO, businessInfoDTO, businessType, isWholeArea);
    }

    private void initCustomAudio(AxeBindingDTO bindingDTO) {
        String vccId = bindingDTO.getVccId();
        String axeAudioVccId = hideProperties.getCustomConfig().getAxeAudioVccId();
        if (axeAudioVccId.contains(vccId)) {
            bindingDTO.setAudio(bindingDTO.getAybAudioACallX());
            bindingDTO.setAudioCalled(bindingDTO.getAybAudioBCalledX());
            String aybAudioBCallX = bindingDTO.getAybAudioBCallX();
            String aybAudioACallX = bindingDTO.getAybAudioACallX();
            String aybAudioBCalledX = bindingDTO.getAybAudioBCalledX();
            String aybAudioACalledX = bindingDTO.getAybAudioACalledX();
            bindingDTO.setAybAudioACallX(aybAudioBCallX);
            bindingDTO.setAybAudioBCallX(aybAudioACallX);
            bindingDTO.setAybAudioACalledX(aybAudioBCalledX);
            bindingDTO.setAybAudioBCalledX(aybAudioACalledX);
        }
    }

    /**
     * 平台分配X号码和分机号
     *
     * @since 2.4.1
     */
    private ResultVO<AxeBindingVO> distributeExtNumAndTelX(AxeBindingDTO bindingDTO,
                                                           PrivateCorpBusinessInfoDTO businessInfoDTO,
                                                           String businessType,
                                                           boolean isWholeArea) {
        String requestId = bindingDTO.getRequestId();
        String vccId = bindingDTO.getVccId();
        // 2. 获取可分配X号码池子
        UsablePoolVO usablePoolVO = getUsablePool(bindingDTO, isWholeArea);
        String cityCode = usablePoolVO.getCityCode();
        List<String> poolList = usablePoolVO.getPoolList();

        // 3. 随机取一个X号码,并按顺序取出一个分机号
        GetExtNumVO getExtNumVO = getExtNum(vccId, poolList, cityCode, businessType);

        // 4. 若地市池不够分配, 是否可以使用全国池分配
        if (!getExtNumVO.getSuccess()) {
            if (enableUseWholePool(bindingDTO.getAreaMatchMode(), isWholeArea)) {
                Optional<List<String>> wholePoolOptional = getWholePoolOptional(vccId);
                if (wholePoolOptional.isPresent()) {
                    cityCode = SystemConstant.COUNTRY_CODE;
                    getExtNumVO = getExtNum(vccId, wholePoolOptional.get(), cityCode, businessType);
                }
            }
            if (!getExtNumVO.getSuccess()) {
                log.error("AXE分配分机号, vccId: {}, requestId: {}, 地市: {} 和全国池分机号均不足", vccId, requestId, bindingDTO.getAreaCode());
                throw new PoolLackException(String.format("地市: %s和全国池分机号均不足", bindingDTO.getAreaCode()));
            }
        }
        bindingDTO.setCityCode(cityCode);
        // 5. 保存axe绑定关系X-E
        bindingDTO.setTelX(getExtNumVO.getTelX());
        bindingDTO.setExtNum(getExtNumVO.getExtNum());
        return finishBinding(bindingDTO, businessInfoDTO, businessType, cityCode);
    }

    /**
     * 指定分机号，未指定X号码
     *
     * @since 2.7.1
     */
    private ResultVO<AxeBindingVO> specifyExtNum(AxeBindingDTO bindingDTO,
                                                 PrivateCorpBusinessInfoDTO businessInfoDTO,
                                                 String businessType,
                                                 boolean isWholeArea) {
        String vccId = bindingDTO.getVccId();
        String extNum = bindingDTO.getExtNum();
        /*
         * 判断是否用全国池，
         * 1. 地市池子+全国池子，于分机号匹配是否可分配
         * 2. 1匹配不成功，地市池子随机分配一个分机号
         */
        String cityCode = getCityCode(isWholeArea, bindingDTO.getAreaCode());
        if (isWholeArea) {
            // 设置使用全国池，只使用全国池
            // 全国池子
            List<String> wholePool = randomList(bindingDTO.getVccId(), cityCode);
            for (String number : wholePool) {
                Optional<String> stringOptional = bindCacheService.checkExtNumAndTelX(bindingDTO.getVccId(), businessType,
                        bindingDTO.getCityCode(), number, extNum);
                if (stringOptional.isPresent()) {
                    boolean set = axeBindCacheService.setUniqueExtBind(vccId, cityCode, number, stringOptional.get());
                    if (!set) {
                        continue;
                    }
                    bindingDTO.setTelX(number);
                    bindingDTO.setExtNum(stringOptional.get());
                    log.info("使用全国池whole=1, vccId: {}, type: {}, areaCode: {}, telX: {}, extNum: {}, 分机号找到可分配X号码，分配成功",
                            bindingDTO.getVccId(), businessType, bindingDTO.getCityCode(), stringOptional.get(), extNum);
                    return finishBinding(bindingDTO, businessInfoDTO, businessType, cityCode);
                }
            }
            log.warn("使用全国池whole=1, vccId: {}, type: {}, areaCode: {}, extNum: {}, 分机号未找到可分配X号码，分配失败",
                    bindingDTO.getVccId(), businessType, bindingDTO.getCityCode(), extNum);
            return ResultVO.fail(ErrorCodeEnum.EXT_NUM_ALREADY_USED.getCode(), ErrorCodeEnum.EXT_NUM_ALREADY_USED.getMessage());
        }
        // 地市号码池
        List<NumberAreaCodeDTO> areaPool = NumberPoolAxeCache.getNumberPoolReplica(bindingDTO.getVccId(), bindingDTO.getAreaCode());
        if (enableUseWholePool(bindingDTO.getAreaMatchMode(), false)) {
            List<NumberAreaCodeDTO> wholePool = NumberPoolAxeCache.getNumberPoolReplica(bindingDTO.getVccId(), SystemConstant.COUNTRY_CODE);
            areaPool.addAll(wholePool);
        }
        areaPool = randomList(areaPool);
        for (NumberAreaCodeDTO dto : areaPool) {
            String number = dto.getNumber();
            String areaCode = dto.getAreaCode();
            Optional<String> stringOptional = bindCacheService.checkExtNumAndTelX(bindingDTO.getVccId(), businessType,
                    areaCode, number, extNum);
            if (stringOptional.isPresent()) {
                String distributeExtNum = stringOptional.get();
                boolean set = axeBindCacheService.setUniqueExtBind(vccId, areaCode, number, distributeExtNum);
                if (!set) {
                    continue;
                }
                bindingDTO.setTelX(number);
                bindingDTO.setExtNum(distributeExtNum);
                bindingDTO.setCityCode(areaCode);
                log.info("vccId: {}, type: {}, areaCode: {}, telX: {}, extNum: {}, 分机号找到可分配X号码，分配成功",
                        bindingDTO.getVccId(), businessType, areaCode, number, distributeExtNum);
                return finishBinding(bindingDTO, businessInfoDTO, businessType, areaCode);
            }
        }
        // 没有找到匹配的，手动指定的分机号失效，由平台分配X号码和分机号
        return distributeExtNumAndTelX(bindingDTO, businessInfoDTO, businessType, false);
    }

    private List<String> randomList(String vccId, String cityCode) {
        List<String> poolList = NumberPoolAxeCache.getPoolReplica(vccId, cityCode);
        return randomList(poolList);
    }

    private <T> List<T> randomList(List<T> poolList) {
        if (CollUtil.isEmpty(poolList)) {
            return Lists.newArrayList();
        }
        int size = poolList.size();
        Integer randomPoolSize = hideProperties.getRandomPoolSize();
        if (size <= randomPoolSize) {
            return poolList;
        }
        return RandomUtil.randomEles(poolList, randomPoolSize);
    }

    /**
     * 指定X号码，若未指定分机号，则随机分配一个分机号
     *
     * @since 2.7.1
     */
    private ResultVO<AxeBindingVO> specifyTelX(AxeBindingDTO bindingDTO,
                                               PrivateCorpBusinessInfoDTO businessInfoDTO,
                                               String businessType,
                                               boolean isWholeArea) {
        String cityCode = getCityCode(isWholeArea, bindingDTO.getAreaCode());
        // 判断x号码是否存在于号码池内,
        Boolean isIn = NumberPoolAxeCache.isInAxePool(bindingDTO.getVccId(), cityCode, bindingDTO.getTelX());
        if (!isIn) {
            return ResultVO.fail(ErrorCodeEnum.TEL_X_NOT_EXIST.getCode(), ErrorCodeEnum.TEL_X_NOT_EXIST.getMessage());
        }
        bindingDTO.setCityCode(cityCode);
        // 查询cityCode-X-E是否存在绑定 lock
        Optional<String> stringOptional = bindCacheService.checkExtNumAndTelX(bindingDTO.getVccId(), businessType,
                bindingDTO.getCityCode(), bindingDTO.getTelX(), bindingDTO.getExtNum());
        if (!stringOptional.isPresent()) {
            // 分机号已被使用, 或分机号不足
            log.info("vccId: {}, type: {}, areaCode: {}, telX: {}, extNum: {}, 分机号已被使用或分机号不足",
                    bindingDTO.getVccId(), businessType, bindingDTO.getCityCode(), bindingDTO.getTelX(), bindingDTO.getExtNum());
            return ResultVO.fail(ErrorCodeEnum.EXT_NUM_ALREADY_USED.getCode(), ErrorCodeEnum.EXT_NUM_ALREADY_USED.getMessage());
        }
        // 完成绑定
        bindingDTO.setExtNum(stringOptional.get());
        return finishBinding(bindingDTO, businessInfoDTO, businessType, cityCode);
    }

    private ResultVO<AxeBindingVO> finishBinding(AxeBindingDTO bindingDTO,
                                                 PrivateCorpBusinessInfoDTO businessInfoDTO,
                                                 String businessType,
                                                 String cityCode) {
        PrivateBindInfoAxe bindInfo = getPrivateBindInfo(bindingDTO, cityCode);
        log.info("AXE设置绑定完成, vccId: {}, requestId: {}, bindId: {}, telX: {}, ext: {}",
                bindInfo.getVccId(), bindInfo.getRequestId(), bindInfo.getBindId(), bindInfo.getTelX(), bindInfo.getTelXExt());
        AxeBindingVO bindingVO = asyncService.saveBindInfo(bindInfo, businessInfoDTO, businessType);
        distributeFixedPhone(bindInfo.getVccId(), bindingDTO.getPhoneFlag(), bindingVO);
        return ResultVO.ok(bindingVO);
    }

    /**
     * 轮训 分配返回一个固话号码
     */
    private void distributeFixedPhone(String vccId, Integer phoneFlag, AxeBindingVO bindingVO) {
        if (ObjectUtil.isNotEmpty(phoneFlag) && phoneFlag == 1) {
            String key = vccId + StrUtil.AT + "phone";
            List<String> list = PrivateFixedPhoneCache.get(vccId);
            String phone = roundRobinLoadBalancer.get(key, list);
            bindingVO.setPhone(phone);
        }
    }

    /**
     * 特殊参数校验
     */
    private void paramsCheck(AxeBindingDTO bindingDTO) {
        // callback_expiration 要小于等于 expiration
        Long callbackExpiration = bindingDTO.getCallbackExpiration();
        if (ObjectUtil.isNotEmpty(callbackExpiration) && callbackExpiration > bindingDTO.getExpiration()) {
            throw new ParamsException("callback_expiration必须要小于等于expiration");
        }
    }

    /**
     * 分配分机号
     */
    private GetExtNumVO getExtNum(String vccId, List<String> poolList, String cityCode, String businessType) {
        String key = vccId + StrUtil.COLON + cityCode;
        String findX = roundRobinLoadBalancer.get(key, poolList);
        // 找分机号, X号码的未使用分机号池set:
        Optional<String> extNumOptional = bindCacheService.getExtNum(vccId, businessType, cityCode, findX);
        if (extNumOptional.isPresent()) {
            boolean set = bindCacheService.setUniqueExtBind(vccId, cityCode, findX, extNumOptional.get());
            if (set) {
                return GetExtNumVO.builder().telX(findX).extNum(extNumOptional.get()).success(true).build();
            }
        }
        log.info("vccId: {}, cityCode: {}, 分配分机号, 遍历池子集合: {}个, getExtNum", vccId, cityCode, poolList.size());
        // 分机号set为空, 再查找其他X号码, 遍历全部X号码
        ArrayList<String> list = new ArrayList<>(poolList);
        list.remove(findX);
        Collections.shuffle(list);
        for (String telX : list) {
            Optional<String> extOptional = bindCacheService.getExtNum(vccId, businessType, cityCode, telX);
            if (extOptional.isPresent()) {
                // 判断XE 绑定 是否绑定过
                boolean set = bindCacheService.setUniqueExtBind(vccId, cityCode, telX, extOptional.get());
                if (!set) {
                    continue;
                }
                return GetExtNumVO.builder().telX(telX).extNum(extOptional.get()).success(true).build();
            }
        }
        return GetExtNumVO.builder().success(false).build();
    }

    /**
     * 获取可用X号码池子
     */
    private UsablePoolVO getUsablePool(AxeBindingDTO bindingDTO, boolean isWholeArea) {
        String vccId = bindingDTO.getVccId();
        String areaCode = bindingDTO.getAreaCode();
        String requestId = bindingDTO.getRequestId();
        String cityCode = getCityCode(isWholeArea, areaCode);
        // 地市号码池
        Optional<List<String>> poolOptional = NumberPoolAxeCache.getPool(vccId, cityCode);
        if (!poolOptional.isPresent()) {
            // area_match_mode=1 && whole_area!=1 使用全国池,
            if (enableUseWholePool(bindingDTO.getAreaMatchMode(), isWholeArea)) {
                Optional<List<String>> wholePoolOptional = getWholePoolOptional(vccId);
                if (!wholePoolOptional.isPresent()) {
                    log.error("AXE获取可用X号码池子, vccId: {}, requestId: {},号码池: {}和全国池X号码均不足", vccId, requestId, areaCode);
                    throw new PoolLackException(String.format("地市: %s和全国池X号码均不足", areaCode));
                }
                cityCode = SystemConstant.COUNTRY_CODE;
                poolOptional = wholePoolOptional;
            } else {
                log.error("AXE获取可用X号码池子, vccId: {}, requestId: {},不使用全国池,地市号码池: {}, X号码不足", vccId, requestId, areaCode);
                throw new PoolLackException(String.format("地市: %s, X号码不足", areaCode));
            }
        }
        return UsablePoolVO.builder().poolList(poolOptional.get()).cityCode(cityCode).build();
    }

    private static String getCityCode(boolean isWholeArea, String areaCode) {
        return isWholeArea ? SystemConstant.COUNTRY_CODE : areaCode;
    }

    /**
     * 当地市池号码不足时, area_match_mode=1 && whole_area!=1 可使用全国池
     */
    private boolean enableUseWholePool(Integer areaMatchMode, boolean isWholeArea) {
        return AreaMatchModeEnum.USE_WHOLE_POOl.getCode().equals(areaMatchMode) && !isWholeArea;
    }

    /**
     * 获取全国池号码
     */
    private Optional<List<String>> getWholePoolOptional(String vccId) {
        return NumberPoolAxeCache.getPool(vccId, SystemConstant.COUNTRY_CODE);
    }

    private PrivateBindInfoAxe getPrivateBindInfo(AxeBindingDTO axeBindingDTO, String cityCode) {
        PrivateBindInfoAxe bindInfo = axeBindConverter.bindingDto2BindInfo(axeBindingDTO);
        bindInfo.setBindId(BindIdUtil.getBindId(BusinessTypeEnum.AXE, cityCode, GatewayConstant.LOCAL, axeBindingDTO.getTelX()));
        bindInfo.setTelX(axeBindingDTO.getTelX());
        bindInfo.setTelXExt(axeBindingDTO.getExtNum());
        DateTime expireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(axeBindingDTO.getExpiration()));
        bindInfo.setExpireTime(expireTime);
        bindInfo.setMaxDuration(ObjectUtil.isEmpty(axeBindingDTO.getMaxDuration()) ? hideProperties.getMaxDuration() : axeBindingDTO.getMaxDuration());
        bindInfo.setAybOtherShow(ObjectUtil.isEmpty(bindInfo.getAybOtherShow()) ? 1 : bindInfo.getAybOtherShow());
        bindInfo.setAybAreaCode(StrUtil.isBlank(bindInfo.getAybAreaCode()) ? bindInfo.getAreaCode() : bindInfo.getAybAreaCode());
        bindInfo.setAybExpiration(ObjectUtil.isEmpty(bindInfo.getAybExpiration()) ? hideProperties.getDefaultExpiration() : bindInfo.getAybExpiration());
        bindInfo.setCityCode(cityCode);
        bindInfo.setWholeArea(ObjectUtil.isEmpty(bindInfo.getWholeArea()) ? 0 : bindInfo.getWholeArea());
        bindInfo.setAybFlag(ObjectUtil.isEmpty(axeBindingDTO.getAybFlag()) ? 0 : axeBindingDTO.getAybFlag());
        bindInfo.setSupplierId(GatewayConstant.LOCAL);
        bindInfo.setModel(ObjectUtil.isEmpty(axeBindingDTO.getModel()) ? ModelEnum.TEL_X.getCode() : axeBindingDTO.getModel());
        bindInfo.setRecordFileFormat(ObjectUtil.isEmpty(axeBindingDTO.getRecordFileFormat()) ? RecordFileFormatEnum.wav.name() : axeBindingDTO.getRecordFileFormat());
        bindInfo.setRecordMode(ObjectUtil.isEmpty(axeBindingDTO.getRecordMode()) ? RecordModeEnum.MIX.getCode() : axeBindingDTO.getRecordMode());
        bindInfo.setDualRecordMode(ObjectUtil.isEmpty(axeBindingDTO.getDualRecordMode()) ? DualRecordModeEnum.CALLER_LEFT.getCode() : axeBindingDTO.getDualRecordMode());

        // 2.4.0新增字段
        bindInfo.setCallbackFlag(ObjectUtil.isEmpty(axeBindingDTO.getCallbackFlag()) ? 0 : axeBindingDTO.getCallbackFlag());
        bindInfo.setCallbackExpiration(ObjectUtil.isEmpty(axeBindingDTO.getCallbackExpiration()) ? bindInfo.getExpiration() : axeBindingDTO.getCallbackExpiration());
        // 具体回呼到期时间
        DateTime callbackExpireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(bindInfo.getCallbackExpiration()));
        bindInfo.setCallbackExpireTime(callbackExpireTime);
        bindInfo.setAreaMatchMode(ObjectUtil.isEmpty(axeBindingDTO.getAreaMatchMode()) ? AreaMatchModeEnum.NOT_USE_WHOLE_POOl.getCode() : axeBindingDTO.getAreaMatchMode());

        return bindInfo;
    }

    public Result unbind(UnBindDTO unBindDTO, String numType) {
        String vccId = unBindDTO.getVccId();
        Optional<AxeBindIdKeyInfoDTO> bindIdKeyInfoDtoOptional = bindCacheService.getBindInfoByBindId(vccId, unBindDTO.getBindId(), numType);
        if (!bindIdKeyInfoDtoOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        AxeBindIdKeyInfoDTO bindIdKeyInfoDTO = bindIdKeyInfoDtoOptional.get();

        asyncService.unbind(bindIdKeyInfoDTO, unBindDTO, numType);
        return Result.ok();
    }

    public Result updateExpirationBind(UpdateExpirationDTO updateExpirationDTO, String numType) {
        Optional<AxeBindIdKeyInfoDTO> bindIdKeyInfoDtoOptional = bindCacheService.getBindInfoByBindId(updateExpirationDTO.getVccId(),
                updateExpirationDTO.getBindId(), numType);
        if (!bindIdKeyInfoDtoOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        AxeBindIdKeyInfoDTO bindIdKeyInfoDTO = bindIdKeyInfoDtoOptional.get();

        return asyncService.updateExpirationBind(bindIdKeyInfoDTO, updateExpirationDTO, numType);
    }

    public ResultVO<List<AxeUtilizationVO>> utilization(AxeUtilizationDTO utilizationDTO, String vccId) {
        if (Boolean.TRUE.equals(hideProperties.getSwitchs().getAxeBindStatsFromCache())) {
            return ResultVO.ok(utilizationFromCache(utilizationDTO, vccId));
        }
        return ResultVO.ok(utilizationFromDb(utilizationDTO, vccId));
    }

    public List<AxeUtilizationVO> utilizationFromDb(AxeUtilizationDTO utilizationDTO, String vccId) {
        List<AxeUtilizationVO> list = new ArrayList<>();
        List<NumberAreaCodeCountVO> countList = privateNumberInfoMapper.getNumberCountGroupByAreaCode(vccId,
                utilizationDTO.getAreaCode(), PoolTypeEnum.AXE.name(), 1);
        if (CollUtil.isEmpty(countList)) {
            return null;
        }
        Map<String, Long> countMap = countList.stream()
                .collect(Collectors.toMap(NumberAreaCodeCountVO::getAreaCode, NumberAreaCodeCountVO::getCount));

        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, vccId);
            List<AxeUtilizationVO> utilizationList = privateBindInfoAxeMapper.queryAxeUtilizationVO(utilizationDTO);
            Map<String, Long> usedCountMap = utilizationList.stream()
                    .collect(Collectors.toMap(AxeUtilizationVO::getAreaCode, AxeUtilizationVO::getUsedCount));

            for (Map.Entry<String, Long> entry : countMap.entrySet()) {
                String areaCode = entry.getKey();
                Long totalCount = entry.getValue() * 10000;
                Long usedCount = Convert.toLong(usedCountMap.get(areaCode), 0L);
                BigDecimal rate = NumberUtil.div(usedCount, totalCount, 4);
                AxeUtilizationVO vo = AxeUtilizationVO.builder()
                        .usedCount(usedCount)
                        .totalCount(totalCount)
                        .utilizationRate(rate.doubleValue())
                        .areaCode(areaCode)
                        .build();
                list.add(vo);
            }
        }
        return list;
    }

    private List<AxeUtilizationVO> utilizationFromCache(AxeUtilizationDTO utilizationDTO, String vccId) {
        List<AxeUtilizationVO> list = new ArrayList<>();
        List<NumberAreaCodeCountVO> countList = privateNumberInfoMapper.getNumberCountGroupByAreaCode(vccId,
                utilizationDTO.getAreaCode(), PoolTypeEnum.AXE.name(), 1);
        if (CollUtil.isEmpty(countList)) {
            return null;
        }
        Map<String, Long> countMap = countList.stream()
                .collect(Collectors.toMap(NumberAreaCodeCountVO::getAreaCode, NumberAreaCodeCountVO::getCount));
        for (Map.Entry<String, Long> entry : countMap.entrySet()) {
            String areaCode = entry.getKey();
            Long totalCount = entry.getValue() * 10000;
            String axeBindStatsKey = PrivateCacheUtil.getAxeBindStatsKey(vccId, areaCode);
            Long usedCount = Convert.toLong(redissonUtil.getString(axeBindStatsKey), 0L);
            BigDecimal rate = NumberUtil.div(usedCount, totalCount, 4);
            AxeUtilizationVO vo = AxeUtilizationVO.builder()
                    .usedCount(usedCount)
                    .totalCount(totalCount)
                    .utilizationRate(rate.doubleValue())
                    .areaCode(areaCode)
                    .build();
            list.add(vo);
        }
        return list;
    }

}
