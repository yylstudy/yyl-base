package com.cqt.hmyc.web.bind.service.axebn;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.cache.NumberPoolAxebnCache;
import com.cqt.model.bind.axebn.dto.AxebnAppendTelDTO;
import com.cqt.model.bind.axebn.dto.AxebnBindIdKeyInfoDTO;
import com.cqt.model.bind.axebn.dto.AxebnBindQueryDTO;
import com.cqt.model.bind.axebn.dto.AxebnBindingDTO;
import com.cqt.model.bind.axebn.entity.PrivateBindInfoAxebn;
import com.cqt.model.bind.axebn.vo.AxebnBindQueryVO;
import com.cqt.model.bind.axebn.vo.AxebnBindingVO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author linshiqiang
 * @date 2022/3/7 14:15
 */
@Slf4j
@Service
public class AxebnBindService {

    private final static String TYPE = BusinessTypeEnum.AXEBN.name();

    private final AxebnBindCacheService axebnBindCacheService;

    private final AxebnAsyncService axebnAsyncService;

    private final AxebnBindConverter axebnBindConverter;

    public AxebnBindService(AxebnBindCacheService axebnBindCacheService, AxebnAsyncService axebnAsyncService, AxebnBindConverter axebnBindConverter) {
        this.axebnBindCacheService = axebnBindCacheService;
        this.axebnAsyncService = axebnAsyncService;
        this.axebnBindConverter = axebnBindConverter;
    }

    public Result binding(AxebnBindingDTO axebnBindingDTO) {

        String vccId = axebnBindingDTO.getVccId();
        String requestId = axebnBindingDTO.getRequestId();
        String cityCode = SystemConstant.NUMBER_ONE.equals(axebnBindingDTO.getWholeArea()) ? SystemConstant.COUNTRY_CODE : axebnBindingDTO.getAreaCode();
        axebnBindingDTO.setCityCode(cityCode);

        // 地市号码池
        Optional<ArrayList<String>> poolOptional = NumberPoolAxebnCache.getPool(vccId, cityCode);
        if (!poolOptional.isPresent()) {
            log.warn("{} requestId: {}, 号码池不足", TYPE, requestId);
            return Result.fail(ErrorCodeEnum.POOL_LACK.getCode(), ErrorCodeEnum.POOL_LACK.getMessage());
        }

        Optional<AxebnBindingVO> bindingVoOptional = axebnBindCacheService.getAxebnBindInfoByRequestId(vccId, requestId, TYPE);
        if (bindingVoOptional.isPresent()) {
            // request_id记录已存在
            return Result.okRepeat(bindingVoOptional.get());
        }
        ArrayList<String> initAreaPool = poolOptional.get();
        /*
         * 1. 先根据tel_b, 确定是否初始化, 初始化过程加锁
         * 2. 取交集, 不为空, 获取一个X
         * 3. 获取X的一个分机号
         */
        String telB = axebnBindingDTO.getTelB();
        List<String> telbList = StrUtil.split(telB, ",", true, true);
        int telSize = telbList.size();
        if (telSize > 5) {
            return Result.fail(ErrorCodeEnum.TEL_TOO_MUCH.getCode(), ErrorCodeEnum.TEL_TOO_MUCH.getMessage());
        }

        for (String tel : telbList) {
            axebnBindCacheService.initPoolTelB(vccId, TYPE, cityCode, tel, initAreaPool);
        }
        Optional<String> stringOptional = axebnBindCacheService.getInter(telbList, vccId, TYPE, cityCode);
        if (!stringOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.TEL_B_POOL_LACK.getCode(), ErrorCodeEnum.TEL_B_POOL_LACK.getMessage());
        }
        String telX = stringOptional.get();
        Set<Object> extNumSet = axebnBindCacheService.getExtNum(vccId, TYPE, telX, telSize);

        if (CollUtil.isEmpty(extNumSet)) {
            // TODO tel_b 可用池要回滚
            return Result.fail(ErrorCodeEnum.POOL_LACK.getCode(), ErrorCodeEnum.POOL_LACK.getMessage());
        }
        if (extNumSet.size() != telSize) {
            // TODO 分机号不足, tel_b 可用池要回滚
            return Result.fail(ErrorCodeEnum.POOL_LACK.getCode(), ErrorCodeEnum.POOL_LACK.getMessage());
        }
        List<Object> extNumList = new ArrayList<>(extNumSet);
        // 保存axebn绑定关系
        PrivateBindInfoAxebn bindInfoAxebn = axebnBindConverter.axebnBindingDTO2bindInfoAxebn(axebnBindingDTO);
        bindInfoAxebn.setBindId(BindIdUtil.getBindId(BusinessTypeEnum.AXEBN, cityCode, GatewayConstant.LOCAL));
        bindInfoAxebn.setTelX(telX);
        bindInfoAxebn.setTelXExt(StrUtil.join(",", extNumList));
        DateTime expireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(axebnBindingDTO.getExpiration()));
        bindInfoAxebn.setExpireTime(expireTime);

        return Result.ok(axebnAsyncService.saveBindInfo(bindInfoAxebn, TYPE, telbList, extNumList));
    }

    public Result unbind(UnBindDTO unbindDTO) {
        String vccId = unbindDTO.getVccId();
        String bindId = unbindDTO.getBindId();
        Optional<AxebnBindIdKeyInfoDTO> bindIdKeyInfoDtoOptional = axebnBindCacheService.getAxebnBindInfoByBindId(vccId, TYPE, bindId);
        if (!bindIdKeyInfoDtoOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        AxebnBindIdKeyInfoDTO bindIdKeyInfoDTO = bindIdKeyInfoDtoOptional.get();

        axebnAsyncService.unbind(bindIdKeyInfoDTO, unbindDTO, TYPE);
        return Result.ok();
    }

    public Result updateExpirationBind(UpdateExpirationDTO updateExpirationDTO) {
        String vccId = updateExpirationDTO.getVccId();
        String bindId = updateExpirationDTO.getBindId();
        Optional<AxebnBindIdKeyInfoDTO> bindIdKeyInfoDtoOptional = axebnBindCacheService.getAxebnBindInfoByBindId(vccId, TYPE, bindId);
        if (!bindIdKeyInfoDtoOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        AxebnBindIdKeyInfoDTO bindIdKeyInfoDTO = bindIdKeyInfoDtoOptional.get();

        return axebnAsyncService.updateExpirationBind(bindIdKeyInfoDTO, updateExpirationDTO, TYPE);
    }

    public Result query(AxebnBindQueryDTO bindQueryDTO) {
        String bindId = bindQueryDTO.getBindId();
        String requestId = bindQueryDTO.getRequestId();
        String vccId = bindQueryDTO.getVccId();

        if (StrUtil.isEmpty(bindId)) {
            if (StrUtil.isEmpty(requestId)) {
                return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
            }
            Optional<AxebnBindingVO> bindingVoOptional = axebnBindCacheService.getAxebnBindInfoByRequestId(vccId, requestId, TYPE);
            if (!bindingVoOptional.isPresent()) {
                return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
            }
            bindId = bindingVoOptional.get().getBindId();
        }
        Optional<AxebnBindIdKeyInfoDTO> bindIdKeyInfoDtoOptional = axebnBindCacheService.getAxebnBindInfoByBindId(vccId, TYPE, bindId);
        if (!bindIdKeyInfoDtoOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        AxebnBindIdKeyInfoDTO bindIdKeyInfoDTO = bindIdKeyInfoDtoOptional.get();
        AxebnBindQueryVO bindQueryVO = axebnBindConverter.bindIdKeyInfoDTO2AxebnBindQueryVO(bindIdKeyInfoDTO);
        long ttl = axebnBindCacheService.getTtl(PrivateCacheUtil.getBindIdKey(vccId, TYPE, bindId));
        bindQueryVO.setExpiration(ttl > 0 ? ttl / 1000 : ttl);
        return Result.ok(bindQueryVO);
    }

    public Result appendTel(AxebnAppendTelDTO appendTelDTO) {

        return Result.ok();
    }
}
