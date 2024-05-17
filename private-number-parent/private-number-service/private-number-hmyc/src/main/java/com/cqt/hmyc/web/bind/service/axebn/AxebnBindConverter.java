package com.cqt.hmyc.web.bind.service.axebn;

import com.cqt.model.bind.axebn.dto.AxebnBindIdKeyInfoDTO;
import com.cqt.model.bind.axebn.dto.AxebnBindingDTO;
import com.cqt.model.bind.axebn.dto.AxebnExtBindInfoDTO;
import com.cqt.model.bind.axebn.entity.PrivateBindInfoAxebn;
import com.cqt.model.bind.axebn.entity.PrivateBindInfoAxebnHis;
import com.cqt.model.bind.axebn.vo.AxebnBindQueryVO;
import com.cqt.model.bind.axebn.vo.AxebnBindingVO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.vo.BindInfoVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author linshiqiang
 * @date 2022/3/7 16:10
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AxebnBindConverter {

    PrivateBindInfoAxebn axebnBindingDTO2bindInfoAxebn(AxebnBindingDTO axebnBindingDTO);

    AxebnBindingVO bindInfoAxebn2bindingVO(PrivateBindInfoAxebn bindInfoAxebn);

    AxebnBindIdKeyInfoDTO bindInfoAxebn2bindIdKeyInfoDTO(PrivateBindInfoAxebn bindInfoAxebn);

    @Mapping(source = "telXExt", target = "extNum")
    BindRecycleDTO bindInfoAxebn2BindRecycleDTO(PrivateBindInfoAxebn bindInfoAxebn);

    AxebnExtBindInfoDTO bindInfoAxebn2AxebnExtBindInfoDTO(PrivateBindInfoAxebn bindInfoAxebn);

    BindInfoVO axebnExtBindInfoDTO2bindInfoVO(AxebnExtBindInfoDTO axebnExtBindInfoDTO);

    BindInfoVO bindInfoAxebn2bindInfoVO(PrivateBindInfoAxebn bindInfoAxebn);

    PrivateBindInfoAxebnHis bindInfoAxebn2bindInfoAxebnHis(PrivateBindInfoAxebn privateBindInfoAxebn);

    PrivateBindInfoAxebn bindRecycleDTO2bindInfoAxebn(BindRecycleDTO bindRecycleDTO);

    PrivateBindInfoAxebn bindIdKeyInfoDTO2BindInfoAxebn(AxebnBindIdKeyInfoDTO bindIdKeyInfoDTO);

    AxebnBindQueryVO bindIdKeyInfoDTO2AxebnBindQueryVO(AxebnBindIdKeyInfoDTO bindIdKeyInfoDTO);

}
