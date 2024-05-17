package com.cqt.hmyc.web.bind.service.axbn;

import com.cqt.model.bind.axbn.dto.AxbnBindIdKeyInfoDTO;
import com.cqt.model.bind.axbn.dto.AxbnBindingDTO;
import com.cqt.model.bind.axbn.entity.PrivateBindInfoAxbn;
import com.cqt.model.bind.axbn.entity.PrivateBindInfoAxbnHis;
import com.cqt.model.bind.axbn.vo.AxbnBindInfoVO;
import com.cqt.model.bind.axbn.vo.AxbnBindingVO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.vo.BindInfoVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

/**
 * @author linshiqiang
 * @date 2022/3/22 14:47
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AxbnBindConverter {

    PrivateBindInfoAxbn bindingDto2BindInfoAxbn(AxbnBindingDTO bindingDTO);

    AxbnBindingVO bindInfoAxbn2AxbnBindingVO(PrivateBindInfoAxbn bindInfoAxbn);

    AxbnBindIdKeyInfoDTO bindInfoAxbn2AxbnBindIdKeyInfoDTO(PrivateBindInfoAxbn bindInfoAxbn);

    BindRecycleDTO bindInfoAxbn2BindRecycleDTO(PrivateBindInfoAxbn bindInfoAxbn);

    PrivateBindInfoAxbnHis bindInfoAxbn2BindInfoAxbnHis(PrivateBindInfoAxbn privateBindInfoAxbn);

    PrivateBindInfoAxbn bindRecycleDTO2bindInfoAxbn(BindRecycleDTO bindRecycleDTO);

    PrivateBindInfoAxbn bindIdKeyInfoDTO2BindInfoAxbn(AxbnBindIdKeyInfoDTO bindIdKeyInfoDTO);

    AxbnBindInfoVO bindIdKeyInfoDTO2AxbnBindInfoVO(AxbnBindIdKeyInfoDTO bindIdKeyInfoDTO);

    BindInfoVO bindInfoAxbn2BindInfoVO(PrivateBindInfoAxbn infoAxbn);

}
