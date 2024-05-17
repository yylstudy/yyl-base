package com.cqt.hmyc.web.bind.service.axb;

import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.model.bind.axb.dto.AxbBindIdKeyInfoDTO;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.vo.AxbBindingVO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.bind.dto.UpdateTelBindDTO;
import com.cqt.model.common.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/2/16 11:39
 */
@Slf4j
@Service
@AllArgsConstructor
public class AxbBindService {

    private final static String TYPE = BusinessTypeEnum.AXB.name();

    private final AxbBindCacheService axbBindCacheService;

    private final AxbAsyncService axbAsyncService;

    private final AxbBindingService axbBindingService;

    public Result bindingAxb(AxbBindingDTO axbBindingDTO) {
        Result result = axbBindingService.binding(axbBindingDTO);
        if (result.getCode() != 0) {
            return result;
        }
        Object data = result.getData();
        if (!(data instanceof PrivateBindInfoAxb)) {
            return result;
        }
        PrivateBindInfoAxb privateBindInfoAxb = (PrivateBindInfoAxb) data;
        AxbBindingVO axbBindingVO = axbAsyncService.saveAxbBindInfo(privateBindInfoAxb, TYPE);
        return Result.ok(axbBindingVO);
    }

    public Result bindingAyb(AxbBindingDTO axbBindingDTO) {
        Result result = axbBindingService.binding(axbBindingDTO);
        if (result.getCode() != 0) {
            return result;
        }
        Object data = result.getData();
        if (!(data instanceof PrivateBindInfoAxb)) {
            return result;
        }
        PrivateBindInfoAxb privateBindInfoAxb = (PrivateBindInfoAxb) data;
        axbAsyncService.saveAxbBindInfo(privateBindInfoAxb, TYPE);
        return Result.ok(privateBindInfoAxb);
    }

    public Result unbind(UnBindDTO unBindDTO) {
        String bindId = unBindDTO.getBindId();
        String vccId = unBindDTO.getVccId();
        Optional<AxbBindIdKeyInfoDTO> optional = axbBindCacheService.getAxbBindInfoByBindId(vccId, bindId);
        if (!optional.isPresent()) {
            // 绑定关系不存在
            log.warn("企业id: {}, bindId: {}, 绑定关系不存在", vccId, bindId);
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }

        // 异步删除
        axbAsyncService.unBind(optional.get(), TYPE);

        return Result.ok();
    }

    public Result updateExpirationBind(UpdateExpirationDTO updateExpirationDTO) {
        String bindId = updateExpirationDTO.getBindId();
        String vccId = updateExpirationDTO.getVccId();
        Optional<AxbBindIdKeyInfoDTO> optional = axbBindCacheService.getAxbBindInfoByBindId(vccId, bindId);
        if (!optional.isPresent()) {
            // 绑定关系不存在
            log.warn("bindId: {}, 绑定关系不存在", bindId);
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }

        // 异步修改
        axbAsyncService.updateAxbExpiration(optional.get(), updateExpirationDTO, TYPE);
        return Result.ok();
    }

    public Result updateTelBind(UpdateTelBindDTO updateTelBindDTO) {
        String bindId = updateTelBindDTO.getBindId();
        String vccId = updateTelBindDTO.getVccId();
        Optional<AxbBindIdKeyInfoDTO> optional = axbBindCacheService.getAxbBindInfoByBindId(vccId, bindId);
        if (!optional.isPresent()) {
            // 绑定关系不存在
            return Result.fail(ErrorCodeEnum.NOT_BIND.getCode(), ErrorCodeEnum.NOT_BIND.getMessage());
        }

        return axbAsyncService.updateAxbTelBind(optional.get(), updateTelBindDTO, TYPE);
    }
}
