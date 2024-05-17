package com.cqt.hmyc.web.bind.service.strategy;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.hmyc.web.corpinfo.service.CorpBusinessService;
import com.cqt.model.bind.query.BindInfoQuery;
import com.cqt.model.bind.vo.BindInfoVO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author linshiqiang
 * @date 2021/10/25 11:23
 * 查询绑定关系策略执行器
 */
@Component
@Slf4j
public class BindInfoQueryStrategyManager {

    private final Map<String, BindInfoQueryStrategy> STRATEGY_MAP = new ConcurrentHashMap<>();

    @Resource
    private List<BindInfoQueryStrategy> bindInfoQueryStrategyList;

    @Resource
    private BindInfoQueryService bindInfoQueryService;

    @Resource
    private CorpBusinessService corpBusinessService;

    public BindInfoVO query(BindInfoQuery bindInfoQuery) {
        if (log.isInfoEnabled()) {
            log.info("fs绑定关系查询参数: {}", JSON.toJSONString(bindInfoQuery));
        }
        String vccId = bindInfoQuery.getVccId();
        Optional<PrivateCorpBusinessInfoDTO> corpBusinessInfoOptional = corpBusinessService.getCorpBusinessInfo(vccId);
        String notBindIvr = bindInfoQueryService.getNotBindIvr(corpBusinessInfoOptional);

        if (StrUtil.isBlank(bindInfoQuery.getCalled())) {
            return new BindInfoVO(ErrorCodeEnum.NONE_NUMBER.getCode(), ErrorCodeEnum.NONE_NUMBER.getMessage(), notBindIvr);
        }
        // 查询X号码是什么类型
        Optional<String> stringOptional = bindInfoQueryService.getNumType(bindInfoQuery.getCalled());
        if (!stringOptional.isPresent()) {
            return new BindInfoVO(ErrorCodeEnum.NUM_TYPE_NOT_EXIST.getCode(), ErrorCodeEnum.NUM_TYPE_NOT_EXIST.getMessage(), notBindIvr);
        }
        String businessType = stringOptional.get();

        BindInfoQueryStrategy strategy = STRATEGY_MAP.get(businessType);
        Optional<BindInfoQueryStrategy> strategyOptional = Optional.ofNullable(strategy);
        if (strategyOptional.isPresent()) {
            Optional<BindInfoVO> bindInfoVoOptional = strategyOptional.get().query(bindInfoQuery, corpBusinessInfoOptional);
            if (bindInfoVoOptional.isPresent()) {
                BindInfoVO bindInfoVO = bindInfoVoOptional.get();
                if (corpBusinessInfoOptional.isPresent()) {
                    // 设置企业信息默认值
                    PrivateCorpBusinessInfoDTO businessInfoDTO = corpBusinessInfoOptional.get();
                    bindInfoVO.setType(ObjectUtil.isNull(bindInfoVO.getType()) ? businessInfoDTO.getSmsFlag() : bindInfoVO.getType());
                    bindInfoVO.setEnableRecord(ObjectUtil.isNull(bindInfoVO.getEnableRecord()) ? businessInfoDTO.getRecordFlag() : bindInfoVO.getEnableRecord());
                }

                if (log.isInfoEnabled()) {
                    log.info("callId: {}, fs绑定关系查询, 返回结果: {}", bindInfoQuery.getCallId(), JSON.toJSONString(bindInfoVO));
                }
                Object transferData = bindInfoVO.getTransferData();
                if (ObjectUtil.isNotNull(transferData)) {

                    String jsonString = JSON.toJSONString(transferData);
                    bindInfoVO.setTransferData(Base64.encode(jsonString));
                }
                return bindInfoVO;
            }
            return new BindInfoVO(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage(), notBindIvr);
        }
        return new BindInfoVO(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage(), notBindIvr);
    }

    @PostConstruct
    public void initStrategy() {
        for (BindInfoQueryStrategy queryStrategy : bindInfoQueryStrategyList) {
            STRATEGY_MAP.put(queryStrategy.getBusinessType(), queryStrategy);
        }
    }
}
