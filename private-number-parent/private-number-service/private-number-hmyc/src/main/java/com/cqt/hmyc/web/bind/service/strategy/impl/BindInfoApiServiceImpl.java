package com.cqt.hmyc.web.bind.service.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.ControlOperateEnum;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.enums.NumberTypeEnum;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.bind.converter.BindConverter;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoApiService;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryStrategyManager;
import com.cqt.hmyc.web.numpool.mapper.PrivateNumberInfoMapper;
import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.bind.query.BindInfoQuery;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.bind.vo.BindInfoVO;
import com.cqt.model.common.ResultVO;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.redis.util.RedissonUtil;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * @since 2022-11-16 10:10
 * 对外接口 查询绑定关系
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BindInfoApiServiceImpl implements BindInfoApiService {

    private final BindInfoQueryStrategyManager bindInfoQueryStrategyManager;

    private final BindConverter bindConverter;

    private final RedissonUtil redissonUtil;

    private final PrivateNumberInfoMapper privateNumberInfoMapper;

    @Override
    public ResultVO<BindInfoApiVO> getBindInfo(BindInfoApiQuery bindInfoApiQuery) {
        BindInfoQuery bindInfoQuery = bindConverter.bindInfoApiQuery2BindInfoQuery(bindInfoApiQuery);
        // 要根据X号码查询vccId, 接口vccId只用于鉴权
        Optional<String> vccIdOptional = getVccIdByNumber(bindInfoApiQuery.getCalled());
        if (!vccIdOptional.isPresent()) {
            BindInfoApiVO fail = BindInfoApiVO.fail(ControlOperateEnum.REJECT.name(), "");
            return ResultVO.ok(ErrorCodeEnum.NONE_NUMBER.getMessage(), fail);
        }
        bindInfoQuery.setVccId(vccIdOptional.get());
        BindInfoVO bindInfoVO = bindInfoQueryStrategyManager.query(bindInfoQuery);

        // 接口没传vccId, 设置到vo
        if (StrUtil.isEmpty(bindInfoApiQuery.getVccId())) {
            bindInfoVO.setVccId(vccIdOptional.get());
        }

        if (!Optional.ofNullable(bindInfoVO).isPresent()) {
            BindInfoApiVO fail = BindInfoApiVO.fail(ControlOperateEnum.REJECT.name(), "");
            return ResultVO.ok(ErrorCodeEnum.NOT_BIND.getMessage(), fail);
        }
        BindInfoApiVO bindInfoApiVO = bindConverter.bindInfoVo2BindInfoApiVO(bindInfoVO);
        controlOperate(bindInfoApiVO);
        if (0 != bindInfoVO.getCode()) {
            return ResultVO.ok(bindInfoVO.getMessage(), bindInfoApiVO);
        }
        return ResultVO.ok(bindInfoApiVO);
    }

    private void controlOperate(BindInfoApiVO bindInfoApiVO) {
        String numType = bindInfoApiVO.getNumType();
        bindInfoApiVO.setControlOperate(ControlOperateEnum.CONTINUE.name());
        if (StrUtil.isEmpty(numType)) {
            bindInfoApiVO.setControlOperate(ControlOperateEnum.REJECT.name());
            return;
        }
        if (NumberTypeEnum.XE.name().equals(numType)) {
            bindInfoApiVO.setControlOperate(ControlOperateEnum.IVR.name());
        }
    }

    /**
     * 根据x号码查询所属vccId
     *
     * @param number x号码
     * @return vccId
     */
    @CircuitBreaker(name = "redisBreaker", fallbackMethod = "getVccIdByNumberFallBack")
    private Optional<String> getVccIdByNumber(String number) {
        String vccId = redissonUtil.getString(SystemConstant.BIZ_REDIS, PrivateCacheUtil.getVccIdByNumberKey(number));
        if (StrUtil.isNotEmpty(vccId)) {
            return Optional.of(vccId);
        }
        // 查db
        return getVccIdByNumberByDb(number);
    }

    /**
     * 根据x号码查询所属vccId 查询db
     */
    private Optional<String> getVccIdByNumberByDb(String number) {
        LambdaQueryWrapper<PrivateNumberInfo> queryWrapper = new LambdaQueryWrapper<PrivateNumberInfo>()
                .eq(PrivateNumberInfo::getNumber, number)
                .select(PrivateNumberInfo::getVccId)
                .last("limit 1");
        PrivateNumberInfo numberInfo = privateNumberInfoMapper.selectOne(queryWrapper);
        if (Optional.ofNullable(numberInfo).isPresent()) {
            return Optional.of(numberInfo.getVccId());
        }
        return Optional.empty();
    }

    private Optional<String> getVccIdByNumberFallBack(String number, Throwable e) {
        if (e != null) {
            log.error("getVccIdByNumberFallBack, number: {}, 异常信息: {}", number, e.getMessage());
        }
        return getVccIdByNumberByDb(number);
    }
}
