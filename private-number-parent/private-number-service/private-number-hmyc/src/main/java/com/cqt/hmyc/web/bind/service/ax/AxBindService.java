package com.cqt.hmyc.web.bind.service.ax;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.*;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.cache.NumberPoolAxCache;
import com.cqt.model.bind.ax.dto.AxBindIdKeyInfoDTO;
import com.cqt.model.bind.ax.dto.AxBindingDTO;
import com.cqt.model.bind.ax.dto.SetUpTelDTO;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAx;
import com.cqt.model.bind.ax.vo.AxBindingVO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.common.Result;
import com.cqt.model.common.properties.HideProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/3/15 11:34
 */
@Service
@Slf4j
public class AxBindService {

    private static final String TYPE = BusinessTypeEnum.AX.name();

    private final AxBindCacheService bindCacheService;

    private final AxAsyncService asyncService;

    private final AxBindConverter bindConverter;

    private final HideProperties hideProperties;

    public AxBindService(AxBindCacheService bindCacheService, AxAsyncService asyncService,
                         AxBindConverter bindConverter, HideProperties hideProperties) {
        this.bindCacheService = bindCacheService;
        this.asyncService = asyncService;
        this.bindConverter = bindConverter;
        this.hideProperties = hideProperties;
    }

    public Result bindingAx(AxBindingDTO axBindingDTO) {
        String telX = axBindingDTO.getTelX();
        String vccId = axBindingDTO.getVccId();
        String requestId = axBindingDTO.getRequestId();
        String cityCode = SystemConstant.NUMBER_ONE.equals(axBindingDTO.getWholeArea()) ? SystemConstant.COUNTRY_CODE : axBindingDTO.getAreaCode();
        axBindingDTO.setCityCode(cityCode);

        Optional<AxBindingVO> bindingVoOptional = bindCacheService.getAxBindInfoByRequestId(vccId, requestId, TYPE);
        if (bindingVoOptional.isPresent()) {
            // request_id记录已存在
            return Result.okRepeat(bindingVoOptional.get());
        }

        Optional<List<String>> poolOptional = NumberPoolAxCache.getPool(vccId, cityCode);
        if (!poolOptional.isPresent()) {
            log.warn("AX 内存无{}-{}号池", vccId, cityCode);
            return Result.fail(ErrorCodeEnum.POOL_LACK.getCode(), ErrorCodeEnum.POOL_LACK.getMessage());
        }

        // 指定tel_x
        if (StrUtil.isNotEmpty(telX)) {
            List<String> pool = poolOptional.get();
            if (!pool.contains(telX)) {
                return Result.fail(ErrorCodeEnum.TEL_X_NOT_EXIST.getCode(), ErrorCodeEnum.TEL_X_NOT_EXIST.getMessage());
            }

            // 查询X号码是否被使用了
            boolean exist = bindCacheService.isExistX(vccId, TYPE, cityCode, telX);
            if (!exist) {
                return Result.fail(ErrorCodeEnum.TEL_X_IS_USED.getCode(), ErrorCodeEnum.TEL_X_IS_USED.getMessage());
            }
            PrivateBindInfoAx bindInfoAx = getPrivateBindInfoAx(axBindingDTO, cityCode, telX);

            return Result.ok(asyncService.saveBindInfo(bindInfoAx, TYPE));
        }

        // 随机分配X号码
        String findTelX = bindCacheService.getTelX(vccId, TYPE, cityCode);
        if (StrUtil.isEmpty(findTelX)) {

            return Result.fail(ErrorCodeEnum.POOL_LACK.getCode(), ErrorCodeEnum.POOL_LACK.getMessage());
        }
        // 保存ax绑定关系
        PrivateBindInfoAx bindInfoAx = getPrivateBindInfoAx(axBindingDTO, cityCode, findTelX);
        log.info("{}, vccId: {}, requestId: {}, bindId: {}, 分配X号码: {}", TYPE, vccId, requestId, bindInfoAx.getBindId(), findTelX);

        return Result.ok(asyncService.saveBindInfo(bindInfoAx, TYPE));
    }

    private PrivateBindInfoAx getPrivateBindInfoAx(AxBindingDTO axBindingDTO, String cityCode, String findTelX) {
        PrivateBindInfoAx bindInfoAx = bindConverter.axBindingDTO2bindInfoAx(axBindingDTO);
        bindInfoAx.setBindId(BindIdUtil.getBindId(BusinessTypeEnum.AX, cityCode, GatewayConstant.LOCAL, findTelX));
        bindInfoAx.setTelX(findTelX);
        DateTime expireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(axBindingDTO.getExpiration()));
        bindInfoAx.setExpireTime(expireTime);
        bindInfoAx.setMaxDuration(ObjectUtil.isEmpty(axBindingDTO.getMaxDuration()) ? hideProperties.getMaxDuration() : axBindingDTO.getMaxDuration());
        bindInfoAx.setModel(ObjectUtil.isEmpty(axBindingDTO.getModel()) ? ModelEnum.TEL_X.getCode() : axBindingDTO.getModel());
        bindInfoAx.setRecordFileFormat(ObjectUtil.isEmpty(axBindingDTO.getRecordFileFormat()) ? RecordFileFormatEnum.wav.name() : axBindingDTO.getRecordFileFormat());
        bindInfoAx.setRecordMode(ObjectUtil.isEmpty(axBindingDTO.getRecordMode()) ? RecordModeEnum.MIX.getCode() : axBindingDTO.getRecordMode());
        bindInfoAx.setDualRecordMode(ObjectUtil.isEmpty(axBindingDTO.getDualRecordMode()) ? DualRecordModeEnum.CALLER_LEFT.getCode() : axBindingDTO.getDualRecordMode());
        bindInfoAx.setSupplierId(GatewayConstant.LOCAL);
        bindInfoAx.setWholeArea(ObjectUtil.isEmpty(axBindingDTO.getWholeArea()) ? 0 : axBindingDTO.getWholeArea());
        return bindInfoAx;
    }

    public Result unbind(UnBindDTO unBindDTO) {
        String vccId = unBindDTO.getVccId();
        String bindId = unBindDTO.getBindId();
        Optional<AxBindIdKeyInfoDTO> bindIdKeyInfoDtoOptional = bindCacheService.getAxBindInfoByBindId(vccId, TYPE, bindId);
        if (!bindIdKeyInfoDtoOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        AxBindIdKeyInfoDTO bindIdKeyInfoDTO = bindIdKeyInfoDtoOptional.get();

        asyncService.unbind(bindIdKeyInfoDTO, unBindDTO, TYPE);
        return Result.ok();
    }

    public Result updateExpiration(UpdateExpirationDTO updateExpirationDTO) {
        String vccId = updateExpirationDTO.getVccId();
        String bindId = updateExpirationDTO.getBindId();
        Optional<AxBindIdKeyInfoDTO> bindIdKeyInfoDtoOptional = bindCacheService.getAxBindInfoByBindId(vccId, TYPE, bindId);
        if (!bindIdKeyInfoDtoOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        AxBindIdKeyInfoDTO bindIdKeyInfoDTO = bindIdKeyInfoDtoOptional.get();

        return asyncService.updateExpiration(bindIdKeyInfoDTO, updateExpirationDTO, TYPE);
    }

    public Result setupTelB(SetUpTelDTO setUpTelB) {
        String vccId = setUpTelB.getVccId();
        String bindId = setUpTelB.getBindId();
        Optional<AxBindIdKeyInfoDTO> bindIdKeyInfoDtoOptional = bindCacheService.getAxBindInfoByBindId(vccId, TYPE, bindId);
        if (!bindIdKeyInfoDtoOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        AxBindIdKeyInfoDTO bindIdKeyInfoDTO = bindIdKeyInfoDtoOptional.get();

        asyncService.setupTelB(bindIdKeyInfoDTO, setUpTelB, TYPE);
        return Result.ok();
    }

    public String randomDistributeY(String vccId, String areaCode) {
        if (StrUtil.isEmpty(areaCode)) {
            areaCode = SystemConstant.COUNTRY_CODE;
        }

        return bindCacheService.randomDistributeY(vccId, areaCode);
    }
}
