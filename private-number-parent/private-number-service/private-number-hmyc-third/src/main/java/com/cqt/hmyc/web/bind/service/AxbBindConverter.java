package com.cqt.hmyc.web.bind.service;

import com.cqt.hmyc.web.model.hdh.axb.HdhAxbBindDTO;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxbHis;
import com.cqt.model.bind.axb.vo.AxbBindingVO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author linshiqiang
 * date 2022/2/16 14:02
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AxbBindConverter {

    PrivateBindInfoAxb axbBindingDto2BindInfoAxb(AxbBindingDTO axbBindingDTO);

    PrivateBindInfoAxbHis bindInfoAxb2BindInfoAxbHis(PrivateBindInfoAxb bindInfoAxb);

    AxbBindingVO bindInfoAxb2AxbBindingVO(PrivateBindInfoAxb bindInfoAxb);

    BindRecycleDTO bindInfoAxb2BindRecycleDTO(PrivateBindInfoAxb bindInfoAxb);

    @Mappings({
            @Mapping(source = "enableRecord", target = "record"),
            @Mapping(target = "GNFlag",expression ="java(com.cqt.common.util.ThirdUtils.modelExchange(bindingDTO.getModel()))"),
    })
    HdhAxbBindDTO axbBindingDTO2HdhAxbBindDto(AxbBindingDTO bindingDTO);

}
