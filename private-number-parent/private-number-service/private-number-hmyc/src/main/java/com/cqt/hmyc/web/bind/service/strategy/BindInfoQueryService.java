package com.cqt.hmyc.web.bind.service.strategy;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.mapper.ax.PrivateBindInfoAxMapper;
import com.cqt.hmyc.web.bind.mapper.axb.PrivateBindInfoAxbMapper;
import com.cqt.hmyc.web.bind.mapper.axbn.PrivateBindAxbnRealTelMapper;
import com.cqt.hmyc.web.bind.mapper.axbn.PrivateBindInfoAxbnMapper;
import com.cqt.hmyc.web.bind.mapper.axe.PrivateBindInfoAxeMapper;
import com.cqt.hmyc.web.bind.service.ax.AxBindService;
import com.cqt.hmyc.web.bind.service.axb.AxbBindConverter;
import com.cqt.hmyc.web.bind.service.axb.AxbBindService;
import com.cqt.hmyc.web.cache.NumberTypeCache;
import com.cqt.hmyc.web.numpool.mapper.PrivateNumberInfoMapper;
import com.cqt.model.bind.ax.dto.SetUpTelDTO;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAx;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.vo.AxbBindingVO;
import com.cqt.model.bind.axbn.entity.PrivateBindAxbnRealTel;
import com.cqt.model.bind.axbn.entity.PrivateBindInfoAxbn;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.axebn.dto.AxebnExtBindInfoDTO;
import com.cqt.model.bind.axebn.entity.PrivateBindInfoAxebn;
import com.cqt.model.bind.query.BindInfoQuery;
import com.cqt.model.common.Result;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.redis.util.RedissonUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/2/14 14:39
 * 查询绑定关系
 */
@Slf4j
@Service
@AllArgsConstructor
public class BindInfoQueryService {

    private final RedissonUtil redissonUtil;

    private final PrivateBindInfoAxbMapper privateBindInfoAxbMapper;

    private final PrivateBindInfoAxeMapper privateBindInfoAxeMapper;

    private final PrivateBindInfoAxMapper privateBindInfoAxMapper;

    private final PrivateBindAxbnRealTelMapper privateBindAxbnRealTelMapper;

    private final PrivateBindInfoAxbnMapper privateBindInfoAxbnMapper;

    private final AxbBindService axbBindService;

    private final AxbBindConverter axbBindConverter;

    private final HideProperties hideProperties;

    private final AxBindService axBindService;

    private final PrivateNumberInfoMapper privateNumberInfoMapper;

    /**
     * 查询axb绑定关系 redis
     */
    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "redisBreakerAxbFallBack")
    public Optional<PrivateBindInfoAxb> getAxbBindInfo(String vccId, String numType, String telX, String telA) {
        String axbBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, telX);
        String info = redissonUtil.getString(axbBindInfoKey);

        // 为空是否要查询db
        if (StrUtil.isEmpty(info)) {

            return getAxbBindInfoVoByDb(vccId, numType, telX, telA);
        }

        PrivateBindInfoAxb infoAxb = JSON.parseObject(info, PrivateBindInfoAxb.class);

        return Optional.of(infoAxb);
    }

    /**
     * 查询axb绑定关系 fallback
     */
    public Optional<PrivateBindInfoAxb> redisBreakerAxbFallBack(String vccId, String numType, String telX, String telA, Throwable e) {
        if (e != null) {
            log.error("axb, vccId: {}, telX:{}, telB:{}, 查询绑定关系进入熔断, 异常信息: ", vccId, telX, telA, e);
        }
        return getAxbBindInfoVoByDb(vccId, numType, telX, telA);
    }

    /**
     * 查询axb绑定关系 db
     */
    private Optional<PrivateBindInfoAxb> getAxbBindInfoVoByDb(String vccId, String numType, String telX, String telA) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
            LambdaQueryWrapper<PrivateBindInfoAxb> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PrivateBindInfoAxb::getTelX, telX);
            queryWrapper.eq(PrivateBindInfoAxb::getTelA, telA);
            queryWrapper.gt(PrivateBindInfoAxb::getExpireTime, DateUtil.date());
            queryWrapper.last("limit 1");
            PrivateBindInfoAxb bindInfoAxb = privateBindInfoAxbMapper.selectOne(queryWrapper);
            if (ObjectUtil.isNull(bindInfoAxb)) {
                queryWrapper.clear();
                queryWrapper.eq(PrivateBindInfoAxb::getTelX, telX);
                queryWrapper.eq(PrivateBindInfoAxb::getTelB, telA);
                queryWrapper.gt(PrivateBindInfoAxb::getExpireTime, DateUtil.date());
                queryWrapper.last("limit 1");
                bindInfoAxb = privateBindInfoAxbMapper.selectOne(queryWrapper);
                return Optional.ofNullable(bindInfoAxb);
            }
            return Optional.of(bindInfoAxb);
        } catch (Exception e) {
            log.error("axb 查询db异常: ", e);
        }
        return Optional.empty();
    }

    /**
     * 查询axe xe绑定关系 redis
     */
    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "getAxeBindInfoOfXeFallBack")
    public Optional<PrivateBindInfoAxe> getAxeBindInfoOfXe(String vccId, String businessType, String telX, String extNum) {
        String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, businessType, telX, extNum);
        String axeInfo = redissonUtil.getString(extBindInfoKey);

        // 为空是否要查询db
        if (StrUtil.isEmpty(axeInfo)) {
            return getAxeBindInfoOfXeByDb(vccId, telX, extNum);
        }

        PrivateBindInfoAxe bindInfoAxe = JSON.parseObject(axeInfo, PrivateBindInfoAxe.class);
        return Optional.of(bindInfoAxe);
    }

    /**
     * 查询axe xe绑定关系 fallback
     */
    public Optional<PrivateBindInfoAxe> getAxeBindInfoOfXeFallBack(String vccId, String businessType, String telX, String extNum, Throwable e) {
        if (e != null) {
            log.error("axe, vccId: {}, telX: {}, extNum: {},  getAxeBindInfoOfXe查询熔断, 异常信息: ", vccId, telX, extNum, e);
        }
        return getAxeBindInfoOfXeByDb(vccId, telX, extNum);
    }

    /**
     * 查询axe xe绑定关系 db
     * 改为AXE表  private_bind_info_axe_{vccId}_{index}
     */
    private Optional<PrivateBindInfoAxe> getAxeBindInfoOfXeByDb(String vccId, String telX, String extNum) {

        try (HintManager hintManager = HintManager.getInstance()) {
            String numberHash = BindIdUtil.getHash(telX);
            String sharingKey = vccId + StrUtil.AT + numberHash;
            hintManager.addDatabaseShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, numberHash);
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, sharingKey);
            LambdaQueryWrapper<PrivateBindInfoAxe> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PrivateBindInfoAxe::getTelX, telX);
            queryWrapper.eq(PrivateBindInfoAxe::getTelXExt, extNum);
            queryWrapper.gt(PrivateBindInfoAxe::getExpireTime, DateUtil.date());
            queryWrapper.last("limit 1");
            PrivateBindInfoAxe bindInfoAxe = privateBindInfoAxeMapper.selectOne(queryWrapper);
            return Optional.ofNullable(bindInfoAxe);
        } catch (Exception e) {
            log.error("axe getAxeBindInfoOfXeByDb 查询db异常: ", e);
        }
        return Optional.empty();
    }

    /**
     * 查询axe ax绑定关系 redis
     */
    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "getAxeBindInfoOfAxFallBack")
    public Optional<PrivateBindInfoAxe> getAxeBindInfoOfAx(String vccId, String businessType, String telA, String telX) {
        String axBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, businessType, telA, telX);
        String axBindInfo = redissonUtil.getString(axBindInfoKey);

        if (StrUtil.isEmpty(axBindInfo)) {
            // 查db
            return getAxeBindInfoOfAxByDb(vccId, telA, telX);
        }

        PrivateBindInfoAxe privateBindInfoAxe = JSON.parseObject(axBindInfo, PrivateBindInfoAxe.class);
        return Optional.ofNullable(privateBindInfoAxe);
    }

    /**
     * 查询axe xe绑定关系 fallback
     */
    public Optional<PrivateBindInfoAxe> getAxeBindInfoOfAxFallBack(String vccId, String businessType, String telA, String telX, Throwable e) {
        if (e != null) {
            log.error("axe, vccId: {}, telX: {}, telA: {},  getAxeBindInfoOfAx查询进入熔断, 异常信息: ", vccId, telX, telA, e);
        }
        return getAxeBindInfoOfAxByDb(vccId, telA, telX);
    }

    /**
     * 查询axe ax绑定关系 db
     * 改为AXE表  private_bind_info_axe_{vccId}_{index}
     */
    private Optional<PrivateBindInfoAxe> getAxeBindInfoOfAxByDb(String vccId, String telA, String telX) {
        try (HintManager hintManager = HintManager.getInstance()) {
            String numberHash = BindIdUtil.getHash(telX);
            String sharingKey = vccId + StrUtil.AT + numberHash;
            hintManager.addDatabaseShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, numberHash);
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXE, sharingKey);
            LambdaQueryWrapper<PrivateBindInfoAxe> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PrivateBindInfoAxe::getTel, telA);
            queryWrapper.eq(PrivateBindInfoAxe::getTelX, telX);
            queryWrapper.gt(PrivateBindInfoAxe::getExpireTime, DateUtil.date());
            queryWrapper.last("limit 1");
            PrivateBindInfoAxe bindInfoAxe = privateBindInfoAxeMapper.selectOne(queryWrapper);
            return Optional.ofNullable(bindInfoAxe);
        } catch (Exception e) {
            log.error("axe getAxeBindInfoOfAxByDb 查询db异常: ", e);
        }
        return Optional.empty();
    }

    /**
     * 生成AYB绑定关系
     */
    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "redisBreakerAybFallBack")
    public Result bindAyb(AxbBindingDTO axbBindingDTO) {

        return axbBindService.bindingAyb(axbBindingDTO);
    }

    /**
     * 生成AYB绑定关系 fallback
     */
    public Result redisBreakerAybFallBack(AxbBindingDTO axbBindingDTO, Throwable e) {
        if (e != null) {
            log.error("ayb 绑定关系生成: {}, 进入熔断, 异常信息: ", axbBindingDTO, e);
        }

        return Result.fail(500, "生成AYB绑定关系失败");
    }

    /**
     * AYB时, 根据requestId查询绑定关系
     */
    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "redisBreakerAxbByRequestFallBack")
    public Optional<AxbBindingVO> getAxbBindInfoByRequestId(String vccId, String numType, String requestId) {

        String axbRequestIdKey = PrivateCacheUtil.getRequestIdKey(vccId, numType, requestId);
        String bindInfoStr = redissonUtil.getString(axbRequestIdKey);
        if (StrUtil.isEmpty(bindInfoStr)) {
            return Optional.empty();
        }
        return Optional.of(JSON.parseObject(bindInfoStr, AxbBindingVO.class));
    }

    /**
     * AYB时, 根据requestId查询绑定关系 fallback
     */
    public Optional<AxbBindingVO> redisBreakerAxbByRequestFallBack(String vccId, String numType, String requestId, Throwable e) {
        if (e != null) {
            log.error("ayb vccId: {}, 根据requestId: {}, 查询, 进入熔断, 异常信息: ", vccId, requestId, e);
        }
        PrivateBindInfoAxb bindInfoAxb = null;
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXB, vccId);
            LambdaQueryWrapper<PrivateBindInfoAxb> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(PrivateBindInfoAxb::getBindId, PrivateBindInfoAxb::getTelX);
            queryWrapper.eq(PrivateBindInfoAxb::getRequestId, requestId);
            bindInfoAxb = privateBindInfoAxbMapper.selectOne(queryWrapper);
        } catch (Exception ex) {
            log.error("axb selectOne error: ", ex);
        }

        if (bindInfoAxb == null) {

            return Optional.empty();
        }
        return Optional.of(axbBindConverter.bindInfoAxb2AxbBindingVO(bindInfoAxb));
    }

    /**
     * 查询号码类型
     * 业务模式
     *
     * @param telX X号码
     * @return 号码类型
     */
    public Optional<String> getNumType(String telX) {
        String type = NumberTypeCache.getNumType(telX);
        if (StrUtil.isEmpty(type)) {
            PrivateNumberInfo numberInfo = null;
            // 查库
            try {
                // TODO 企业表改了
                LambdaQueryWrapper<PrivateNumberInfo> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.select(PrivateNumberInfo::getBusinessType);
                queryWrapper.eq(PrivateNumberInfo::getNumber, telX);
                queryWrapper.last("limit 1");
                numberInfo = privateNumberInfoMapper.selectOne(queryWrapper);
                if (Optional.ofNullable(numberInfo).isPresent()) {
                    NumberTypeCache.put(telX, numberInfo.getBusinessType());
                }
            } catch (Exception e) {
                log.error("number pool: {} selectOne error: ", telX, e);
            }

            if (numberInfo == null) {
                return Optional.empty();
            }
            return Optional.of(numberInfo.getBusinessType());
        }

        return Optional.of(type);
    }

    /**
     * 获取企业的 无绑定关系提示语
     */
    public String getNotBindIvr(Optional<PrivateCorpBusinessInfoDTO> corpBusinessInfoOptional) {
        String notBindIvr = hideProperties.getNotBindIvr();
        if (corpBusinessInfoOptional.isPresent()) {
            PrivateCorpBusinessInfoDTO businessInfoDTO = corpBusinessInfoOptional.get();
            if (StrUtil.isNotEmpty(businessInfoDTO.getNotBindIvr())) {
                notBindIvr = businessInfoDTO.getNotBindIvr();
            }
        }
        return notBindIvr;
    }

    /**
     * 获取企业 请输入提示音
     */
    public String getDigitsIvr(Optional<PrivateCorpBusinessInfoDTO> corpBusinessInfoOptional) {
        String notBindIvr = hideProperties.getDigitsIvr();
        if (corpBusinessInfoOptional.isPresent()) {
            PrivateCorpBusinessInfoDTO businessInfoDTO = corpBusinessInfoOptional.get();
            if (StrUtil.isNotEmpty(businessInfoDTO.getDigitsIvr())) {
                notBindIvr = businessInfoDTO.getDigitsIvr();
            }
        }
        return notBindIvr;
    }

    public PrivateBindInfoAxb removeAudio(PrivateBindInfoAxb privateBindInfoAxb) {

//        privateBindInfoAxb.setAudioACalledX(null);
//        privateBindInfoAxb.setAudioBCalledX(null);
//        privateBindInfoAxb.setAudioACallXBefore(null);
//        privateBindInfoAxb.setAudioACallX(null);
//        privateBindInfoAxb.setAudioBCallX(null);
//        privateBindInfoAxb.setAudioBCallXBefore(null);
//        privateBindInfoAxb.setAudioACallXBefore(null);

        return privateBindInfoAxb;
    }

    public PrivateBindInfoAx removeAudio(PrivateBindInfoAx privateBindInfoAx) {

//        privateBindInfoAx.setAudioACalledX(null);
//        privateBindInfoAx.setAudioBCalledX(null);
//        privateBindInfoAx.setAudioACallXBefore(null);
//        privateBindInfoAx.setAudioACallX(null);
//        privateBindInfoAx.setAudioBCallX(null);
//        privateBindInfoAx.setAudioBCallXBefore(null);
//        privateBindInfoAx.setAudioACallXBefore(null);

        return privateBindInfoAx;
    }

    public PrivateBindInfoAxe removeAudio(PrivateBindInfoAxe privateBindInfoAxe) {


        return privateBindInfoAxe;
    }


    /**
     * 查询axebn绑定关系 redis
     */
    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "redisBreakerAxebnFallBack")
    public Optional<PrivateBindInfoAxebn> getAxebnBindInfo(String vccId, String numType, String telA, String telX) {
        String bindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, telX);
        String info = redissonUtil.getString(bindInfoKey);

        // 为空是否要查询db
        if (StrUtil.isEmpty(info)) {
            // TODO 查db
            return Optional.empty();
        }

        PrivateBindInfoAxebn bindInfoAxebn = JSON.parseObject(info, PrivateBindInfoAxebn.class);

        return Optional.of(bindInfoAxebn);
    }

    /**
     * 查询axb绑定关系 fallback
     */
    public Optional<PrivateBindInfoAxebn> redisBreakerAxebnFallBack(String vccId, String numType, String telX, String telA, Throwable e) {
        if (e != null) {
            log.error("{}, vccId: {}, telX:{}, telB:{}, 查询绑定关系进入熔断, 异常信息: ", numType, vccId, telX, telA, e);
        }
        // TODO 查db
        return Optional.empty();
    }


    /**
     * axebn 查询X与分机号绑定关系
     */
    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "redisBreakerAxebnExtFallBack")
    public Optional<AxebnExtBindInfoDTO> getAxebnExtBindInfo(String vccId, String numType, String telX, String extNum) {
        String extBindInfoKey = PrivateCacheUtil.getExtBindInfoKey(vccId, numType, telX, extNum);

        String bindInfo = redissonUtil.getString(extBindInfoKey);

        // 为空是否要查询db
        if (StrUtil.isEmpty(bindInfo)) {
            // TODO 查db
            return Optional.empty();
        }

        AxebnExtBindInfoDTO axebnExtBindInfoDTO = JSON.parseObject(bindInfo, AxebnExtBindInfoDTO.class);

        return Optional.of(axebnExtBindInfoDTO);
    }

    public Optional<PrivateBindInfoAxebn> redisBreakerAxebnExtFallBack(String vccId, String numType, String telX, String extNum, Throwable e) {
        if (e != null) {
            log.error("{}, vccId: {}, telX:{}, extNum:{}, 查询绑定关系进入熔断, 异常信息: ", numType, vccId, telX, extNum, e);
        }
        // TODO 查db
        return Optional.empty();
    }

    /**
     * 查询AX, X号码绑定关系
     */
    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "redisBreakerAxFallBack")
    public Optional<PrivateBindInfoAx> getAxBindInfo(String vccId, String numType, String called) {

        String telxBindInfoKey = PrivateCacheUtil.getTelxBindInfoKey(vccId, numType, called);
        String bindInfo = redissonUtil.getString(telxBindInfoKey);
        // 为空是否要查询db
        if (StrUtil.isEmpty(bindInfo)) {
            // 查db
            return getPrivateBindInfoAxByDb(called, numType, vccId);
        }

        PrivateBindInfoAx bindInfoAx = JSON.parseObject(bindInfo, PrivateBindInfoAx.class);

        return Optional.ofNullable(bindInfoAx);
    }

    public Optional<PrivateBindInfoAx> redisBreakerAxFallBack(String vccId, String numType, String called, Throwable e) {
        if (e != null) {
            log.error("ax, vccId: {}, telX:{} 查询绑定关系进入熔断, 异常信息: ", vccId, called, e);
        }
        return getPrivateBindInfoAxByDb(called, numType, vccId);
    }

    private Optional<PrivateBindInfoAx> getPrivateBindInfoAxByDb(String called, String numType, String vccId) {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AX, vccId);
            LambdaQueryWrapper<PrivateBindInfoAx> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PrivateBindInfoAx::getTelX, called);
            queryWrapper.gt(PrivateBindInfoAx::getExpireTime, DateUtil.date());
            queryWrapper.last("limit 1");
            PrivateBindInfoAx privateBindInfoAx = privateBindInfoAxMapper.selectOne(queryWrapper);
            return Optional.ofNullable(privateBindInfoAx);
        } catch (Exception e) {
            log.error("getPrivateBindInfoAxByDb error: ", e);
        }
        return Optional.empty();
    }

    /**
     * AX B为主叫时, 设置B号码
     */
    public void setupTelB(BindInfoQuery bindInfoQuery, PrivateBindInfoAx bindInfoAx) {
        SetUpTelDTO setUpTelDTO = SetUpTelDTO.builder()
                .bindId(bindInfoAx.getBindId())
                .telB(bindInfoQuery.getCaller())
                .vccId(bindInfoAx.getVccId())
                .build();
        axBindService.setupTelB(setUpTelDTO);
    }

    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "redisBreakerAxbnFallBack")
    public Optional<PrivateBindInfoAxbn> getAxbnBindInfo(String vccId, String numType, String telX, String telA) {
        String axbBindInfoKey = PrivateCacheUtil.getBindInfoKey(vccId, numType, telA, telX);
        String info = redissonUtil.getString(axbBindInfoKey);

        // 为空是否要查询db
        if (StrUtil.isEmpty(info)) {

            return getAxbnBindInfoVoByDb(vccId, numType, telX, telA);
        }

        PrivateBindInfoAxbn infoAxbn = JSON.parseObject(info, PrivateBindInfoAxbn.class);

        return Optional.of(infoAxbn);
    }

    /**
     * 查询axb绑定关系 fallback
     */
    public Optional<PrivateBindInfoAxbn> redisBreakerAxbnFallBack(String vccId, String numType, String telX, String telA, Throwable e) {
        if (e != null) {
            log.error("axbn, vccId: {}, telX:{}, telB:{}, 查询绑定关系进入熔断, 异常信息: ", vccId, telX, telA, e);
        }
        return getAxbnBindInfoVoByDb(vccId, numType, telX, telA);
    }

    /**
     * 查询axb绑定关系 db
     */
    private Optional<PrivateBindInfoAxbn> getAxbnBindInfoVoByDb(String vccId, String numType, String telX, String telA) {

        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXBN, vccId);
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_AXBN_REAL_TEL, vccId);

            String realTelId = BindIdUtil.getAxbnRealTelId(vccId, telA, telX);
            LambdaQueryWrapper<PrivateBindAxbnRealTel> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(PrivateBindAxbnRealTel::getBindId);
            queryWrapper.eq(PrivateBindAxbnRealTel::getId, realTelId);
            queryWrapper.last("limit 1");
            PrivateBindAxbnRealTel privateBindAxbnRealTel = privateBindAxbnRealTelMapper.selectOne(queryWrapper);
            Optional<PrivateBindAxbnRealTel> realTelOptional = Optional.ofNullable(privateBindAxbnRealTel);
            if (realTelOptional.isPresent()) {
                PrivateBindInfoAxbn bindInfoAxbn = privateBindInfoAxbnMapper.selectById(realTelOptional.get().getBindId());
                // 设置redis, 不知area_code
                return Optional.ofNullable(bindInfoAxbn);
            }
        } catch (Exception e) {
            log.error("getAxbnBindInfoVoByDb error: ", e);
        }
        return Optional.empty();
    }
}
