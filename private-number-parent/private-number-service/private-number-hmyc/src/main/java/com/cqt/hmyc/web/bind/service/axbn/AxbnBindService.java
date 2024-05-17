package com.cqt.hmyc.web.bind.service.axbn;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.*;
import com.cqt.common.util.BindIdUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.config.exception.PoolLackException;
import com.cqt.hmyc.web.cache.NumberPoolAxbnCache;
import com.cqt.hmyc.web.corpinfo.service.CorpBusinessService;
import com.cqt.model.bind.axbn.dto.AxbnBindIdKeyInfoDTO;
import com.cqt.model.bind.axbn.dto.AxbnBindingDTO;
import com.cqt.model.bind.axbn.entity.PrivateBindInfoAxbn;
import com.cqt.model.bind.axbn.query.AxbnBindInfoQuery;
import com.cqt.model.bind.axbn.vo.AxbnBindInfoVO;
import com.cqt.model.bind.axbn.vo.AxbnBindingVO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.bind.dto.UpdateTelBindDTO;
import com.cqt.model.common.Result;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author linshiqiang
 * @date 2022/3/22 14:46
 */
@Service
@Slf4j
@AllArgsConstructor
public class AxbnBindService {

    private static final String TYPE = BusinessTypeEnum.AXBN.name();

    private final AxbnBindCacheService bindCacheService;

    private final AxbnAsyncService asyncService;

    private final AxbnBindConverter bindConverter;

    private final HideProperties hideProperties;

    private final CorpBusinessService corpBusinessService;

    public Result binding(AxbnBindingDTO bindingDTO) {
        String vccId = bindingDTO.getVccId();
        String requestId = bindingDTO.getRequestId();
        String telA = bindingDTO.getTelA();
        String telB = bindingDTO.getTelB();
        String otherTelB = bindingDTO.getOtherTelB();
        String cityCode = SystemConstant.NUMBER_ONE.equals(bindingDTO.getWholeArea()) ? SystemConstant.COUNTRY_CODE : bindingDTO.getAreaCode();
        bindingDTO.setCityCode(cityCode);

        // 查询requestId 是否已绑定过
        Optional<AxbnBindingVO> bindingVoOptional = bindCacheService.getBindInfoByRequestId(vccId, requestId, TYPE);
        if (bindingVoOptional.isPresent()) {
            // request_id记录已存在
            return Result.okRepeat(bindingVoOptional.get());
        }

        Optional<HashSet<String>> poolOptional = NumberPoolAxbnCache.getPool(vccId, cityCode);
        if (!poolOptional.isPresent()) {
            log.error("vccId: {}, requestId: {}, areaCode: {}, 本地号码池不足.", vccId, requestId, cityCode);
            throw new PoolLackException();
        }
        HashSet<String> pool = poolOptional.get();

        // 获取X号码和Y号码
        List<Object> keyList = new ArrayList<>();
        int index = pool.size();
        if (pool.size() > 1) {

            index = RandomUtil.randomInt(1, pool.size());
        }

        keyList.add(PrivateCacheUtil.getPoolSlotKey(vccId, TYPE, cityCode));
        keyList.add(PrivateCacheUtil.getUsedPoolSlotKey(vccId, TYPE, cityCode, telA));
        keyList.add(PrivateCacheUtil.getUsedPoolSlotKey(vccId, TYPE, cityCode, telB));
        Optional<List<String>> otherTelListOptional = Optional.empty();
        if (StrUtil.isNotEmpty(otherTelB)) {
            List<String> otherTelList = StrUtil.split(otherTelB, ",");
            for (String tel : otherTelList) {
                keyList.add(PrivateCacheUtil.getUsedPoolSlotKey(vccId, TYPE, cityCode, tel));
            }
            otherTelListOptional = Optional.of(otherTelList);
        }
        Optional<List<String>> optional = bindCacheService.getTelX(keyList, index, TYPE);
        if (!optional.isPresent()) {
            log.error("vccId: {}, requestId: {}, areaCode: {}, 分配号码池不足.", vccId, requestId, cityCode);
            throw new PoolLackException();
        }
        List<String> xList = optional.get();
        String telX = xList.get(0);
        xList.remove(telX);

        Map<String, String> otherMap = new HashMap<>(8);
        if (otherTelListOptional.isPresent()) {
            List<String> otherTelList = otherTelListOptional.get();
            int size = otherTelList.size();
            for (int i = 0; i < size; i++) {
                otherMap.put(otherTelList.get(i), xList.get(i));
            }
        }

        PrivateBindInfoAxbn bindInfoAxbn = getPrivateBindInfoAxbn(bindingDTO, vccId, cityCode, xList, telX, otherMap);
        // 保存绑定关系
        asyncService.saveBinding(bindInfoAxbn, TYPE, otherMap);
        AxbnBindingVO bindingVO = bindConverter.bindInfoAxbn2AxbnBindingVO(bindInfoAxbn);
        return Result.ok(bindingVO);
    }

    private PrivateBindInfoAxbn getPrivateBindInfoAxbn(AxbnBindingDTO bindingDTO, String vccId, String cityCode, List<String> xList,
                                                       String telX, Map<String, String> otherMap) {
        PrivateBindInfoAxbn bindInfoAxbn = bindConverter.bindingDto2BindInfoAxbn(bindingDTO);
        bindInfoAxbn.setTelX(telX);
        bindInfoAxbn.setTelY(StrUtil.join(",", xList));
        bindInfoAxbn.setOtherBy(JSON.toJSONString(otherMap));
        String bindId = BindIdUtil.getBindId(BusinessTypeEnum.AXBN, cityCode, GatewayConstant.LOCAL);
        bindInfoAxbn.setBindId(bindId);
        bindInfoAxbn.setSourceBindId(bindId);
        bindInfoAxbn.setMaxDuration(ObjectUtil.isEmpty(bindingDTO.getMaxDuration()) ? hideProperties.getMaxDuration() : bindingDTO.getMaxDuration());
        bindInfoAxbn.setCreateTime(DateUtil.date());
        DateTime expireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(bindInfoAxbn.getExpiration()));
        bindInfoAxbn.setExpireTime(expireTime);
        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = corpBusinessService.getCorpBusinessInfo(vccId);
        if (businessInfoOptional.isPresent()) {
            PrivateCorpBusinessInfoDTO businessInfoDTO = businessInfoOptional.get();
            bindInfoAxbn.setType(ObjectUtil.isNull(bindInfoAxbn.getType()) ? businessInfoDTO.getSmsFlag() : bindInfoAxbn.getType());
            bindInfoAxbn.setEnableRecord(ObjectUtil.isNull(bindInfoAxbn.getEnableRecord()) ? businessInfoDTO.getRecordFlag() : bindInfoAxbn.getEnableRecord());
        }
        bindInfoAxbn.setSupplierId(GatewayConstant.LOCAL);
        bindInfoAxbn.setModel(ObjectUtil.isEmpty(bindingDTO.getModel()) ? ModelEnum.TEL_X.getCode() : bindingDTO.getModel());
        bindInfoAxbn.setRecordFileFormat(ObjectUtil.isEmpty(bindingDTO.getRecordFileFormat()) ? RecordFileFormatEnum.wav.name() : bindingDTO.getRecordFileFormat());
        bindInfoAxbn.setRecordMode(ObjectUtil.isEmpty(bindingDTO.getRecordMode()) ? RecordModeEnum.MIX.getCode() : bindingDTO.getRecordMode());
        bindInfoAxbn.setDualRecordMode(ObjectUtil.isEmpty(bindingDTO.getDualRecordMode()) ? DualRecordModeEnum.CALLER_LEFT.getCode() : bindingDTO.getDualRecordMode());
        return bindInfoAxbn;
    }

    public Result unbind(UnBindDTO unBindDTO) {
        String vccId = unBindDTO.getVccId();
        String bindId = unBindDTO.getBindId();
        Optional<AxbnBindIdKeyInfoDTO> bindIdKeyInfoDtoOptional = bindCacheService.getBindInfoByBindId(vccId, TYPE, bindId);
        if (!bindIdKeyInfoDtoOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        AxbnBindIdKeyInfoDTO bindIdKeyInfoDTO = bindIdKeyInfoDtoOptional.get();

        asyncService.unbind(bindIdKeyInfoDTO, unBindDTO, TYPE);
        return Result.ok();
    }

    public Result updateExpiration(UpdateExpirationDTO updateExpirationDTO) {
        String vccId = updateExpirationDTO.getVccId();
        String bindId = updateExpirationDTO.getBindId();
        Optional<AxbnBindIdKeyInfoDTO> bindIdKeyInfoDtoOptional = bindCacheService.getBindInfoByBindId(vccId, TYPE, bindId);
        if (!bindIdKeyInfoDtoOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        AxbnBindIdKeyInfoDTO bindIdKeyInfoDTO = bindIdKeyInfoDtoOptional.get();

        return asyncService.updateExpiration(bindIdKeyInfoDTO, updateExpirationDTO, TYPE);
    }

    @SuppressWarnings("all")
    public Result query(AxbnBindInfoQuery bindInfoQuery) {

        String bindId = bindInfoQuery.getBindId();
        if (StrUtil.isEmpty(bindId)) {
            String requestId = bindInfoQuery.getRequestId();
            if (StrUtil.isEmpty(requestId)) {
                return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
            }
            Optional<AxbnBindingVO> bindingVoOptional = bindCacheService.getBindInfoByRequestId(bindInfoQuery.getVccId(), requestId, TYPE);
            if (bindingVoOptional.isPresent()) {
                AxbnBindingVO bindingVO = bindingVoOptional.get();
                bindId = bindingVO.getBindId();
            }
        }
        Optional<AxbnBindIdKeyInfoDTO> bindIdKeyInfoDtoOptional = bindCacheService.getBindInfoByBindId(bindInfoQuery.getVccId(), TYPE, bindId);
        if (!bindIdKeyInfoDtoOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        AxbnBindIdKeyInfoDTO bindIdKeyInfoDTO = bindIdKeyInfoDtoOptional.get();
        AxbnBindInfoVO axbnBindInfoVO = bindConverter.bindIdKeyInfoDTO2AxbnBindInfoVO(bindIdKeyInfoDTO);
        long ttl = bindCacheService.getTtl(PrivateCacheUtil.getBindIdKey(bindInfoQuery.getVccId(), TYPE, bindId));
        axbnBindInfoVO.setExpiration(ttl > 0 ? ttl / 1000 : ttl);
        String otherBy = bindIdKeyInfoDTO.getOtherBy();
        if (StrUtil.isNotBlank(otherBy)) {
            // key: b号码, value: y号码
            Map<String, String> otherByMap = JSON.parseObject(otherBy, Map.class);
            // 转为list
            List<Map<String, String>> otherByList = new ArrayList<>();
            for (Map.Entry<String, String> entry : otherByMap.entrySet()) {
                HashMap<String, String> tempMap = new HashMap<>();
                tempMap.put(entry.getKey(), entry.getValue());
                otherByList.add(tempMap);
            }
            axbnBindInfoVO.setOtherByList(otherByList);
        }
        return Result.ok(axbnBindInfoVO);
    }

    public Result updateTel(UpdateTelBindDTO updateTelBindDTO) {
        String vccId = updateTelBindDTO.getVccId();
        String bindId = updateTelBindDTO.getBindId();
        Optional<AxbnBindIdKeyInfoDTO> bindIdKeyInfoDtoOptional = bindCacheService.getBindInfoByBindId(vccId, TYPE, bindId);
        if (!bindIdKeyInfoDtoOptional.isPresent()) {
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }
        AxbnBindIdKeyInfoDTO bindIdKeyInfoDTO = bindIdKeyInfoDtoOptional.get();

        asyncService.updateTel(bindIdKeyInfoDTO, updateTelBindDTO, TYPE);
        return Result.ok();
    }
}
