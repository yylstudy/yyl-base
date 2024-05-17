package com.cqt.hmyc.web.bind.service.axe;

import com.cqt.model.bind.axe.dto.AxeBindIdKeyInfoDTO;
import com.cqt.model.bind.axe.dto.AxeBindingDTO;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxeHis;
import com.cqt.model.bind.axe.vo.AxeBindingVO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.vo.BindInfoVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author linshiqiang
 * @date 2022/2/16 14:02
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AxeBindConverter {

    PrivateBindInfoAxe bindingDto2BindInfo(AxeBindingDTO axeBindingDTO);

    /**
     * 全国0000
     * city_code包含全国0000
     *
     * @param bindInfoAxe
     * @return
     */
    @Mapping(source = "cityCode", target = "areaCode")
    AxeBindingVO bindInfo2BindingVO(PrivateBindInfoAxe bindInfoAxe);

    AxeBindIdKeyInfoDTO bindInfo2BindIdKeyInfoDTO(PrivateBindInfoAxe bindInfoAxe);

    @Mapping(source = "telXExt", target = "extNum")
    BindRecycleDTO bindInfo2BindRecycleDTO(PrivateBindInfoAxe bindInfoAxe);

    PrivateBindInfoAxeHis bindInfoAxe2BindInfoAxeHis(PrivateBindInfoAxe privateBindInfoAxe);

    PrivateBindInfoAxe bindIdKeyInfoDTO2bindInfo(AxeBindIdKeyInfoDTO bindIdKeyInfoDTO);

    PrivateBindInfoAxe bindRecycleDTO2bindInfo(BindRecycleDTO bindRecycleDTO);

    BindInfoVO bindInfo2BindInfoVO(PrivateBindInfoAxe bindInfoAxe);

}
