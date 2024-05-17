package com.cqt.hmyc.web.bind.service;

import com.cqt.hmyc.web.model.hdh.axb.HdhAxeBindDTO;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxbHis;
import com.cqt.model.bind.axb.vo.AxbBindingVO;
import com.cqt.model.bind.axe.dto.AxeBindingDTO;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxeHis;
import com.cqt.model.bind.axe.vo.AxeBindingVO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author huweizhong
 * date  2023/12/8 13:36
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AxeBindConverter {

    @Mappings({
            @Mapping(target = "audio", ignore = true),
    })
    HdhAxeBindDTO axeBindingDTO2HdhAxeBindDto(AxeBindingDTO bindingDTO);

    @Mappings({
            @Mapping(source = "extNum", target = "telXExt"),
    })
    PrivateBindInfoAxe axeBindingDto2BindInfoAxe(AxeBindingDTO axeBindingDTO);

    PrivateBindInfoAxeHis bindInfoAxe2BindInfoAxebHis(PrivateBindInfoAxe bindInfoAxe);

    @Mappings({
            @Mapping(source = "telXExt", target = "extNum"),
    })
    BindRecycleDTO bindInfoAxe2BindRecycleDTO(PrivateBindInfoAxe bindInfoAxe);

    AxeBindingVO bindInfoAxe2AxeBindingVO(PrivateBindInfoAxe bindInfoAxe);



}
