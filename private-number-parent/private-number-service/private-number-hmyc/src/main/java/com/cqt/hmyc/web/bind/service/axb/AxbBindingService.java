package com.cqt.hmyc.web.bind.service.axb;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.*;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.CollectionUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.config.exception.PoolLackException;
import com.cqt.hmyc.web.cache.NumberPoolAxbCache;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axb.dto.InitTelResultDTO;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.vo.AxbBindingVO;
import com.cqt.model.common.Result;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2022/4/6 9:24
 * AXB 绑定接口
 */
@Service
@AllArgsConstructor
@Slf4j
public class AxbBindingService {

    private static final String BUSINESS_TYPE = NumberTypeEnum.AXB.name();

    private final AxbBindCacheService bindCacheService;

    private final RedissonUtil redissonUtil;

    private final RedissonClient redissonClient;

    private final HideProperties hideProperties;

    private final AxbBindConverter axbBindConverter;

    /**
     * 绑定入口
     */
    public Result binding(AxbBindingDTO axbBindingDTO) {
        // AB号码不能一样
        if (axbBindingDTO.getTelA().equals(axbBindingDTO.getTelB())) {
            return Result.fail(ErrorCodeEnum.DUPLICATE_A_B.getCode(), ErrorCodeEnum.DUPLICATE_A_B.getMessage());
        }

        Optional<AxbBindingVO> bindAxbVoOptional = bindCacheService.getAxbBindInfoByRequestId(axbBindingDTO.getVccId(), axbBindingDTO.getRequestId());
        if (bindAxbVoOptional.isPresent()) {
            // request_id记录已存在
            return Result.ok(bindAxbVoOptional.get());
        }
        String cityCode = SystemConstant.NUMBER_ONE.equals(axbBindingDTO.getWholeArea()) ? SystemConstant.COUNTRY_CODE : axbBindingDTO.getAreaCode();
        axbBindingDTO.setCityCode(cityCode);

        // tel_x参数不为空, 则直接使用, 先判断是否存在绑定关系
        if (StrUtil.isNotEmpty(axbBindingDTO.getTelX())) {
            // X号码是否存在
            boolean existAllPool = NumberPoolAxbCache.isExistAllPool(axbBindingDTO.getVccId(), cityCode, axbBindingDTO.getTelX());
            if (!existAllPool) {
                return Result.fail(ErrorCodeEnum.TEL_X_NOT_EXIST.getCode(), ErrorCodeEnum.TEL_X_NOT_EXIST.getMessage());
            }

            // 指定X号码  1 不生成号码池   0 生成号码池
            if (BindIdUtil.isDirectTelX(axbBindingDTO.getDirectTelX())) {
                return Result.ok(getPrivateBindInfoAxb(axbBindingDTO, axbBindingDTO.getTelX()));
            }

            /*
             * 查询AX BX 是否存在绑定关系
             * 不存在  从AX BX可用号码池移除X号码
             */
            boolean checkTelX = checkTelX(axbBindingDTO.getRequestId(), axbBindingDTO.getVccId(), cityCode,
                    axbBindingDTO.getTelA(), axbBindingDTO.getTelB(), axbBindingDTO.getTelX());
            if (checkTelX) {

                return Result.ok(getPrivateBindInfoAxb(axbBindingDTO, axbBindingDTO.getTelX()));
            }
            return Result.fail(ErrorCodeEnum.TEL_X_IS_USED.getCode(), ErrorCodeEnum.TEL_X_IS_USED.getMessage());
        }


        // 106开头
        if (isIndustrySmsNumber(axbBindingDTO.getTelA())) {
            Optional<String> industrySmsTelX = getIndustrySmsTelX(axbBindingDTO, axbBindingDTO.getTelA());
            if (!industrySmsTelX.isPresent()) {
                throw new PoolLackException();
            }
            return Result.ok(getPrivateBindInfoAxb(axbBindingDTO, industrySmsTelX.get()));
        }

        String telX = getTelX(axbBindingDTO);

        return Result.ok(getPrivateBindInfoAxb(axbBindingDTO, telX));
    }

    /**
     * 接口指定X号码, 判断X号码是否被AB绑定过
     */
    private Boolean checkTelX(String requestId, String vccId, String cityCode, String telA, String telB, String telX) {
        String axUsablePoolKey = PrivateCacheUtil.getUsablePoolKey(vccId, BUSINESS_TYPE, cityCode, telA);
        String bxUsablePoolKey = PrivateCacheUtil.getUsablePoolKey(vccId, BUSINESS_TYPE, cityCode, telB);
        // 查询使用已经初始化
        String axInitFlagKey = PrivateCacheUtil.getInitFlagKey(vccId, BUSINESS_TYPE, cityCode, telA);
        String bxInitFlagKey = PrivateCacheUtil.getInitFlagKey(vccId, BUSINESS_TYPE, cityCode, telB);
        String axPoolKey = PrivateCacheUtil.getUsablePoolKey(vccId, BUSINESS_TYPE, cityCode, telA);
        String bxPoolKey = PrivateCacheUtil.getUsablePoolKey(vccId, BUSINESS_TYPE, cityCode, telB);
        Optional<HashSet<String>> masterPoolOptional = NumberPoolAxbCache.getPool(AxbPoolTypeEnum.MASTER, vccId, cityCode);
        if (!masterPoolOptional.isPresent()) {
            throw new PoolLackException();
        }
        Set<String> masterPool = new HashSet<>(masterPoolOptional.get());

        // 先初始化可用号码池
        initTelA(requestId, axPoolKey, axInitFlagKey, telA, cityCode, vccId, masterPool);
        initTelA(requestId, bxPoolKey, bxInitFlagKey, telB, cityCode, vccId, masterPool);
        // AX BX 可用池 是否移除X号码成功
        boolean removeA = redissonUtil.removeSet(axUsablePoolKey, telX);
        boolean removeB = redissonUtil.removeSet(bxUsablePoolKey, telX);

        log.info("requestId:{}, vccId:{}, businessType:{}, cityCode:{}, telA:{}, telB:{}, telX:{}, removeA:{}, removeB:{}",
                requestId, vccId, BUSINESS_TYPE, cityCode, telA, telB, telX, removeA, removeB);
        if (removeA && removeB) {
            return true;
        }
        // TODO 主池是否使用完  如果使用完, 则从备池判断.  指定X号码, 应都是主池, 备池不设置号码

        if (removeA) {
            redissonUtil.addSet(axUsablePoolKey, telX);
        }
        if (removeB) {
            redissonUtil.addSet(bxUsablePoolKey, telX);
        }

        return false;
    }

    /**
     * 分配X号码入口
     */
    private String getTelX(AxbBindingDTO axbBindingDTO) {
        String vccId = axbBindingDTO.getVccId();
        String requestId = axbBindingDTO.getRequestId();
        String telA = axbBindingDTO.getTelA();
        String telB = axbBindingDTO.getTelB();
        String cityCode = axbBindingDTO.getCityCode();

        // 开始绑定
        // 查询A可用号码池/B可用号码池, 判断是否已经初始化A/B
        String axPoolKey = PrivateCacheUtil.getUsablePoolKey(vccId, BUSINESS_TYPE, cityCode, telA);
        String bxPoolKey = PrivateCacheUtil.getUsablePoolKey(vccId, BUSINESS_TYPE, cityCode, telB);

        String axInitFlagKey = PrivateCacheUtil.getInitFlagKey(vccId, BUSINESS_TYPE, cityCode, telA);
        String bxInitFlagKey = PrivateCacheUtil.getInitFlagKey(vccId, BUSINESS_TYPE, cityCode, telB);

        // 地市池子
        Optional<HashSet<String>> masterPoolOptional = NumberPoolAxbCache.getPool(AxbPoolTypeEnum.MASTER, vccId, cityCode);

        if (!masterPoolOptional.isPresent()) {
            log.warn("requestId: {}, cityCode: {}, 本地内存主号码池不足", requestId, cityCode);
            throw new PoolLackException();
        }
        // 创建新对象
        Set<String> masterPool = new HashSet<>(masterPoolOptional.get());

        InitTelResultDTO initTelA = initTelA(requestId, axPoolKey, axInitFlagKey, telA, cityCode, vccId, masterPool);
        String axInitFlag = initTelA.getInitFlag();
        String bxInitFlag = redissonUtil.getString(bxInitFlagKey);
        //AX 第一次初始化, || A池已初始化,且只使用主池
        if (initTelA.getFirstInit() || InitFlagEnum.FIRST_INIT.getCode().equals(axInitFlag) || StrUtil.isEmpty(bxInitFlag)) {
            if (StrUtil.isEmpty(bxInitFlag)) {
                Optional<String> optionalTelX = initTelB(requestId, axPoolKey, bxPoolKey, bxInitFlagKey, telB, cityCode, vccId, masterPool, axInitFlag);

                if (optionalTelX.isPresent()) {
                    // 得到X
                    return optionalTelX.get();
                }

            }
            // 3. A池未初始化(第一次初始化)
            //B池已初始化,且只使用主池
            if (initTelA.getFirstInit() && InitFlagEnum.FIRST_INIT.getCode().equals(bxInitFlag)) {

                Optional<String> removeFromBxPoolOptional = removeFromAxPool(axPoolKey, bxPoolKey);
                if (removeFromBxPoolOptional.isPresent()) {
                    return removeFromBxPoolOptional.get();
                }

            }
        }
        // 退出, 进行第4步, 使用备池
        Optional<String> intersectionOptional = intersection(requestId, cityCode, telA, telB, axPoolKey, bxPoolKey, axInitFlagKey, bxInitFlagKey, vccId);
        if (intersectionOptional.isPresent()) {
            return intersectionOptional.get();
        }

        throw new PoolLackException();
    }

    /**
     * 行业短信号码 分配X号码
     */
    private Optional<String> getIndustrySmsTelX(AxbBindingDTO axbBindingDTO, String tel) {

        String industrySmsTelUsablePoolKey = PrivateCacheUtil.getIndustrySmsTelUsablePoolKey(axbBindingDTO.getVccId(), axbBindingDTO.getCityCode(), tel);
        String industrySmsTelInitKey = PrivateCacheUtil.getIndustrySmsTelInitKey(axbBindingDTO.getVccId(), axbBindingDTO.getCityCode(), tel);
        String initFlag = redissonUtil.getString(industrySmsTelInitKey);
        if (StrUtil.isEmpty(initFlag)) {
            // ax不存在, 初始化ax, 从地市号码池
            RLock lock = redissonClient.getLock(PrivateCacheUtil.getIndustryInitLockKey(tel));
            try {
                lock.lock(5, TimeUnit.SECONDS);
                if (StrUtil.isEmpty(redissonUtil.getString(industrySmsTelInitKey))) {
                    Optional<HashSet<String>> allPoolOptional = NumberPoolAxbCache.getPool(AxbPoolTypeEnum.ALL, axbBindingDTO.getVccId(), axbBindingDTO.getCityCode());
                    if (!allPoolOptional.isPresent()) {
                        return Optional.empty();
                    }
                    HashSet<String> pool = allPoolOptional.get();
                    String randomX = CollectionUtil.getRandomSet(pool);
                    pool.remove(randomX);
                    bindCacheService.initPool(axbBindingDTO.getVccId(), industrySmsTelInitKey, industrySmsTelUsablePoolKey, pool,
                            tel, axbBindingDTO.getCityCode(), InitFlagEnum.THREE_INIT.getCode(), false);
                    return Optional.of(randomX);
                }
                String randomX = redissonUtil.removeSetRandom(industrySmsTelUsablePoolKey);
                return Optional.ofNullable(randomX);
            } catch (Exception e) {
                log.error("axb, requestId: {}, 初始化106号码池异常: ", axbBindingDTO.getRequestId(), e);
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.error("解锁异常, 锁已失效,{}", e.getMessage());
                }
            }
        }
        String randomX = redissonUtil.removeSetRandom(industrySmsTelUsablePoolKey);
        if (StrUtil.isEmpty(randomX)) {
            return Optional.empty();
        }
        return Optional.of(randomX);
    }

    /**
     * 号码 是否为行业短信号码
     */
    private Boolean isIndustrySmsNumber(String tel) {

        return ReUtil.isMatch(hideProperties.getIndustrySmsNumberRegex(), tel);
    }

    private PrivateBindInfoAxb getPrivateBindInfoAxb(AxbBindingDTO axbBindingDTO, String telX) {

        axbBindingDTO.setBindTime(DateUtil.date());
        DateTime expireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(axbBindingDTO.getExpiration()));
        axbBindingDTO.setExpireTime(expireTime);
        PrivateBindInfoAxb bindInfoAxb = axbBindConverter.axbBindingDto2BindInfoAxb(axbBindingDTO);
        bindInfoAxb.setTelX(telX);
        bindInfoAxb.setModel(ObjectUtil.isEmpty(axbBindingDTO.getModel()) ? ModelEnum.TEL_X.getCode() : axbBindingDTO.getModel());
        bindInfoAxb.setRecordFileFormat(ObjectUtil.isEmpty(axbBindingDTO.getRecordFileFormat()) ? RecordFileFormatEnum.wav.name() : axbBindingDTO.getRecordFileFormat());
        bindInfoAxb.setRecordMode(ObjectUtil.isEmpty(axbBindingDTO.getRecordMode()) ? RecordModeEnum.MIX.getCode() : axbBindingDTO.getRecordMode());
        bindInfoAxb.setDualRecordMode(ObjectUtil.isEmpty(axbBindingDTO.getDualRecordMode()) ? DualRecordModeEnum.CALLER_LEFT.getCode() : axbBindingDTO.getDualRecordMode());
        bindInfoAxb.setCreateTime(DateUtil.date());
        // 是否全国池
        bindInfoAxb.setWholeArea(ObjectUtil.isEmpty(axbBindingDTO.getWholeArea()) ? 0 : axbBindingDTO.getWholeArea());
        bindInfoAxb.setMaxDuration(ObjectUtil.isEmpty(axbBindingDTO.getMaxDuration()) ? hideProperties.getMaxDuration() : axbBindingDTO.getMaxDuration());
        bindInfoAxb.setSupplierId(GatewayConstant.LOCAL);
        // 生成bindId
        String bindId;
        if (NumberTypeEnum.AYB.name().equals(axbBindingDTO.getNumType())) {
            bindId = BindIdUtil.getBindId(BusinessTypeEnum.AYB, axbBindingDTO.getCityCode(), GatewayConstant.LOCAL, telX);
        } else {
            bindId = BindIdUtil.getBindId(BusinessTypeEnum.AXB, axbBindingDTO.getCityCode(), GatewayConstant.LOCAL, telX);
        }
        log.info("axb binding success, request_id: {}, bind_id: {}, tel_x: {}", axbBindingDTO.getRequestId(), bindId, telX);
        bindInfoAxb.setBindId(bindId);
        return bindInfoAxb;
    }

    /**
     * 初始化A号码可用池
     */
    private InitTelResultDTO initTelA(String requestId, String axPoolKey, String axInitFlagKey, String telA, String cityCode,
                                      String vccId, Set<String> masterPool) {
        InitTelResultDTO initTelResultDTO = new InitTelResultDTO();
        String axInitFlag = redissonUtil.getString(axInitFlagKey);
        if (StrUtil.isEmpty(axInitFlag)) {
            // ax不存在, 初始化ax, 从地市号码池
            RLock lock = redissonClient.getLock(PrivateCacheUtil.getLockInitAxKey(vccId, BUSINESS_TYPE, telA));
            try {
                lock.lock(5, TimeUnit.SECONDS);
                axInitFlag = redissonUtil.getString(axInitFlagKey);
                if (StrUtil.isEmpty(axInitFlag)) {
                    bindCacheService.initPool(vccId, axInitFlagKey, axPoolKey, masterPool, telA, cityCode, InitFlagEnum.FIRST_INIT.getCode(), false);
                    initTelResultDTO.setInitFlag(InitFlagEnum.FIRST_INIT.getCode());
                    initTelResultDTO.setFirstInit(true);
                    return initTelResultDTO;
                }
            } catch (Exception e) {
                log.error("axb, requestId: {}, 初始化Ax主池异常: ", requestId, e);
            } finally {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.error("解锁异常, 锁已失效,{}", e.getMessage());
                }
            }
        }
        initTelResultDTO.setInitFlag(axInitFlag);
        initTelResultDTO.setFirstInit(false);
        return initTelResultDTO;
    }

    /**
     * 初始化B
     */
    private Optional<String> initTelB(String requestId, String axPoolKey, String bxPoolKey, String bxInitFlagKey,
                                      String telB, String cityCode, String vccId, Set<String> masterPool, String axInitFlag) {

        // Bx未初始化,  异步初始化, 再去掉x
        // Bx未初始化, Ax已初始化
        // Bx未初始化, Ax未初始化
        // 初始化bx池  从A号码池中取出一个X号码 tel_x
        RLock lock = redissonClient.getLock(PrivateCacheUtil.getLockInitBxKey(vccId, BUSINESS_TYPE, telB));
        try {
            lock.lock(5, TimeUnit.SECONDS);
            // Ax池中取出一个x号码, bx池初始化时, 去掉X号码
            String bxInitFlag = redissonUtil.getString(bxInitFlagKey);
            if (StrUtil.isEmpty(bxInitFlag) && InitFlagEnum.FIRST_INIT.getCode().equals(axInitFlag)) {
                String randomX = redissonUtil.removeSetRandom(axPoolKey);
                boolean axPoolEmpty = StrUtil.isEmpty(randomX);
                if (!axPoolEmpty) {
                    masterPool.remove(randomX);
                }
                bindCacheService.initPool(vccId, bxInitFlagKey, bxPoolKey, masterPool, telB, cityCode, InitFlagEnum.FIRST_INIT.getCode(), false);
                if (axPoolEmpty) {
                    return Optional.empty();
                }
                return Optional.of(randomX);
            }
            bindCacheService.initPool(vccId, bxInitFlagKey, bxPoolKey, masterPool, telB, cityCode, InitFlagEnum.FIRST_INIT.getCode(), false);
        } catch (Exception e) {
            log.error("requestId: {}, axb, 初始化Bx池异常: ", requestId, e);
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {
                log.error("解锁异常, 锁已失效,{}", e.getMessage());
            }
        }
        return Optional.empty();
    }

    /**
     * 从Ax池移除x号码
     *
     * @param axPoolKey a号码池键
     * @param bxPoolKey b号码池键
     * @return x号码
     */
    private Optional<String> removeFromAxPool(String axPoolKey, String bxPoolKey) {

        /*
         * A池未初始化(第一次初始化)
         * B池已初始化,且只使用主池
         * 从B号码池中取出一个X号码tel_x
         * A号码池移除tel_x
         */
        String randomX = redissonUtil.removeSetRandom(bxPoolKey);
        if (StrUtil.isNotEmpty(randomX)) {
            boolean removeSet = redissonUtil.removeSet(axPoolKey, randomX);
            if (removeSet) {
                return Optional.of(randomX);
            }
            // ax移除失败, 遍历重试
            List<String> removeList = new ArrayList<>();
            removeList.add(randomX);
            Integer bxPoolCount = redissonUtil.getSetCount(bxPoolKey);
            for (int i = 0; i < bxPoolCount; i++) {
                String random = redissonUtil.removeSetRandom(bxPoolKey);
                if (StrUtil.isEmpty(randomX)) {
                    return Optional.empty();
                }
                boolean remove = redissonUtil.removeSet(axPoolKey, random);
                if (remove) {
                    redissonUtil.addAllSet(bxPoolKey, removeList);
                    removeList.clear();
                    return Optional.of(random);
                }
                removeList.add(random);
            }
            // A移除失败的x号码, 添加到B号码池中(B是移除成功的)
            if (CollUtil.isNotEmpty(removeList)) {
                redissonUtil.addAllSet(bxPoolKey, removeList);
            }
        }
        // Bx spop 为空
        return Optional.empty();
    }

    /**
     * 取交集
     */
    private Optional<String> intersection(String requestId, String cityCode, String telA, String telB, String axPoolKey, String bxPoolKey,
                                          String axInitFlagKey, String bxInitFlagKey, String vccId) {

        RLock lock = redissonClient.getLock(PrivateCacheUtil.getLockInitAxKey(vccId, BUSINESS_TYPE, telA));
        try {
            lock.lock(5, TimeUnit.SECONDS);
            Set<String> axPool = redissonUtil.getSet(axPoolKey);
            Set<String> bxPool = redissonUtil.getSet(bxPoolKey);
            Set<String> distinct = CollUtil.intersectionDistinct(axPool, bxPool);
            if (CollUtil.isNotEmpty(distinct)) {
                String randomX = CollectionUtil.getRandomSet(distinct);
                boolean removeA = redissonUtil.removeSet(axPoolKey, randomX);
                boolean removeB = redissonUtil.removeSet(bxPoolKey, randomX);
                log.info("requestId: {}, AB第一次取交集, tel_x: {}, axPoolKey： {}， removeA: {}, bxPoolKey： {}， removeB: {}",
                        requestId, randomX, axPoolKey, removeA, bxPoolKey, removeB);
                // 需要判断都移除成功?
                return Optional.of(randomX);
            }
            // 主池交集为空, 进行备池判断
            boolean axInitFlag = isBackPool(axInitFlagKey);
            boolean bxInitFlag = isBackPool(bxInitFlagKey);

            if (!axInitFlag) {
                // 初始化A的备池
                initBackPool(requestId, cityCode, telA, axPoolKey, axInitFlagKey, false, vccId);
            }

            if (!bxInitFlag) {
                // 初始化B的备池
                initBackPool(requestId, cityCode, telB, bxPoolKey, bxInitFlagKey, true, vccId);
            }

            Set<String> axPoolBack = redissonUtil.getSet(axPoolKey);
            Set<String> bxPoolBack = redissonUtil.getSet(bxPoolKey);
            Set<String> distinctBack = CollUtil.intersectionDistinct(axPoolBack, bxPoolBack);
            if (CollUtil.isEmpty(distinctBack)) {
                // 备池交集也为空 则号码池不足
                log.error("requestId: {}, 备池交集也为空 则号码池不足", requestId);
                throw new PoolLackException();
            }

            String randomX = CollectionUtil.getRandomSet(distinctBack);
            distinctBack.remove(randomX);
            boolean removeBackA = redissonUtil.removeSet(axPoolKey, randomX);
            boolean removeBackB = redissonUtil.removeSet(bxPoolKey, randomX);
            if (removeBackA && removeBackB) {
                log.info("requestId: {}, AB备池取交集, tel_x: {}, axPoolKey： {}， removeA: {}, bxPoolKey： {}， removeB: {}",
                        requestId, randomX, axPoolKey, removeBackA, bxPoolKey, removeBackB);
                return Optional.of(randomX);
            }
            List<String> removeFailB = new ArrayList<>();
            if (!removeBackB) {
                removeFailB.add(randomX);
            }
            int size = distinctBack.size();
            for (int i = 0; i < size; i++) {
                String randomX2 = CollectionUtil.getRandomSet(distinctBack);
                if (StrUtil.isEmpty(randomX2)) {
                    rollbackAxPool(axPoolKey, removeFailB);
                    log.error("requestId: {}, 备池for取交集也为空 则号码池不足", requestId);
                    throw new PoolLackException();
                }
                distinctBack.remove(randomX2);
                boolean removeBackAx = redissonUtil.removeSet(axPoolKey, randomX2);
                boolean removeBackBx = redissonUtil.removeSet(bxPoolKey, randomX2);
                if (removeBackAx && removeBackBx) {
                    // B移除失败的x号码, 添加到A号码池中(A是移除成功的)
                    rollbackAxPool(axPoolKey, removeFailB);
                    log.info("requestId: {}, AB备池取交集重试, tel_x: {}, axPoolKey： {}， removeA: {}, bxPoolKey： {}， removeB: {}",
                            requestId, randomX, axPoolKey, removeBackAx, bxPoolKey, removeBackBx);
                    return Optional.of(randomX2);
                }
                if (!removeBackBx) {
                    removeFailB.add(randomX2);
                }
            }
            rollbackAxPool(axPoolKey, removeFailB);
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {
                log.error("解锁异常, 锁已失效,{}", e.getMessage());
            }
        }
        return Optional.empty();
    }

    /**
     * B移除失败的x号码, 添加到A号码池中(A是移除成功的)
     *
     * @param axPoolKey   ax号码池
     * @param removeFailB B移除失败的x号码
     */
    private void rollbackAxPool(String axPoolKey, List<String> removeFailB) {
        if (CollUtil.isNotEmpty(removeFailB)) {
            redissonUtil.addAllSet(axPoolKey, removeFailB);
        }
    }

    /**
     * 号码池是否使用备池
     */
    public Boolean isBackPool(String key) {

        String initFlag = redissonUtil.getString(key);

        return !InitFlagEnum.FIRST_INIT.getCode().equals(initFlag);
    }

    /**
     * 初始化备池
     *
     * @param requestId   请求id
     * @param cityCode    城市编码
     * @param tel         A/B号码
     * @param poolKey     号码池键
     * @param initFlagKey 初始化键
     * @param needLock    是否需加分布式锁
     */
    private void initBackPool(String requestId, String cityCode, String tel, String poolKey, String initFlagKey, Boolean needLock, String vccId) {
        if (!needLock) {
            boolean backPool = isBackPool(initFlagKey);
            if (backPool) {
                return;
            }
            initBack(vccId, cityCode, tel, poolKey, initFlagKey);
            return;
        }
        RLock lock = redissonClient.getLock(PrivateCacheUtil.getLockInitBxKey(vccId, BUSINESS_TYPE, tel));
        try {
            lock.lock(5, TimeUnit.SECONDS);
            // Ax池中取出一个x号码, bx池初始化时, 去掉X号码
            boolean backPool = isBackPool(initFlagKey);
            if (backPool) {
                return;
            }
            initBack(vccId, cityCode, tel, poolKey, initFlagKey);
        } catch (Exception e) {
            log.error("requestId: {}, axb, tel: {}, 初始化号码池异常: ", requestId, tel, e);
        } finally {
            try {
                lock.unlock();
            } catch (Exception e) {
                log.error("解锁异常, 锁已失效,{}", e.getMessage());
            }
        }
    }

    /**
     * 初始化备池
     */
    private void initBack(String vccId, String cityCode, String tel, String poolKey, String initFlagKey) {
        Optional<HashSet<String>> slavePoolOptional = NumberPoolAxbCache.getPool(AxbPoolTypeEnum.SLAVE, vccId, cityCode);
        if (slavePoolOptional.isPresent()) {
            HashSet<String> slavePool = slavePoolOptional.get();
            bindCacheService.initPool(vccId, initFlagKey, poolKey, slavePool, tel, cityCode, InitFlagEnum.SECOND_INIT.getCode(), false);
        }
    }
}
