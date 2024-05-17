package com.cqt.hmyc.web.bind.service.axyb;

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
import com.cqt.hmyc.web.bind.mapper.axyb.PrivateBindInfoAxybMapper;
import com.cqt.model.bind.axyb.dto.AxybBindingDTO;
import com.cqt.model.bind.axyb.entity.PrivateBindInfoAxyb;
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
public class AxybBindService {

    private final static String TYPE = BusinessTypeEnum.AXYB.name();

    private PrivateBindInfoAxybMapper privateBindInfoAxybMapper;

    public Result binding(AxybBindingDTO bindingDTO) {

        PrivateBindInfoAxyb bindInfoAxyb = new PrivateBindInfoAxyb();
        BeanUtil.copyProperties(bindingDTO, bindInfoAxyb);
        bindInfoAxyb.setBindId(BindIdUtil.getBindId(BusinessTypeEnum.AXYB, bindingDTO.getAreaCode(), GatewayConstant.LOCAL));
        bindInfoAxyb.setTelX(RandomUtil.randomNumbers(11));
        bindInfoAxyb.setTelY(RandomUtil.randomNumbers(11));
        bindInfoAxyb.setCityCode(bindingDTO.getAreaCode());
        bindInfoAxyb.setWholeArea(1);
        DateTime expireTime = DateUtil.offset(DateUtil.date(), DateField.SECOND, Convert.toInt(bindingDTO.getExpiration()));
        bindInfoAxyb.setExpireTime(expireTime);
        bindInfoAxyb.setRecordMode(1);
        bindInfoAxyb.setModel(2);
        bindInfoAxyb.setRecordFileFormat("mp3");
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue(TableNameConstant.PRIVATE_BIND_INFO_AXYB, bindingDTO.getVccId());
            privateBindInfoAxybMapper.insert(bindInfoAxyb);
        }
        AxybBindingVO axybBindingVO = new AxybBindingVO();
        BeanUtil.copyProperties(bindInfoAxyb, axybBindingVO);
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
