package com.cqt.hmyc.web.bind.service.axg;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.common.constants.TableNameConstant;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.common.util.BindIdUtil;
import com.cqt.hmyc.web.bind.mapper.axg.PrivateBindInfoAxgMapper;
import com.cqt.model.bind.axg.dto.AxgBindingDTO;
import com.cqt.model.bind.axg.entity.PrivateBindInfoAxg;
import com.cqt.model.bind.axyb.vo.AxybBindingVO;
import com.cqt.model.bind.dto.UnBindDTO;
import com.cqt.model.bind.dto.UpdateExpirationDTO;
import com.cqt.model.bind.dto.UpdateTelBindDTO;
import com.cqt.model.common.Result;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.hint.HintManager;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * @date 2022/2/16 11:39
 */
@Slf4j
@Service
@AllArgsConstructor
public class AxgBindService {

    private final static String TYPE = BusinessTypeEnum.AXG.name();

    private PrivateBindInfoAxgMapper privateBindInfoAxgMapper;

    public Result binding(AxgBindingDTO bindingDTO) {

        PrivateBindInfoAxg bindInfo = new PrivateBindInfoAxg();
        BeanUtil.copyProperties(bindingDTO, bindInfo);
        bindInfo.setBindId(BindIdUtil.getBindId(BusinessTypeEnum.AXYB, bindingDTO.getAreaCode(), GatewayConstant.LOCAL));
        bindInfo.setTelX(RandomUtil.randomNumbers(11));
        bindInfo.setCityCode(bindingDTO.getAreaCode());
        bindInfo.setWholeArea(1);
        DateTime expireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(bindingDTO.getExpiration()));
        bindInfo.setExpireTime(expireTime);
        bindInfo.setRecordMode(1);
        bindInfo.setModel(2);
        bindInfo.setRecordFileFormat("mp3");
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXG, bindingDTO.getVccId());
            privateBindInfoAxgMapper.insert(bindInfo);
        }
        AxybBindingVO axybBindingVO = new AxybBindingVO();
        BeanUtil.copyProperties(bindInfo, axybBindingVO);
        return Result.ok(axybBindingVO);
    }


    public Result unbind(UnBindDTO unBindDTO) {
        String bindId = unBindDTO.getBindId();
        String vccId = unBindDTO.getVccId();

        return Result.ok();
    }

    public Result updateExpirationBind(UpdateExpirationDTO updateExpirationDTO) {
        String bindId = updateExpirationDTO.getBindId();
        String vccId = updateExpirationDTO.getVccId();
        return Result.ok();
    }

    public Result updateTelBind(UpdateTelBindDTO updateTelBindDTO) {
        String bindId = updateTelBindDTO.getBindId();
        String vccId = updateTelBindDTO.getVccId();
        return Result.ok();
    }
}
