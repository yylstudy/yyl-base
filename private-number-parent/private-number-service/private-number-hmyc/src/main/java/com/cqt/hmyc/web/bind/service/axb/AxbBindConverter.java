package com.cqt.hmyc.web.bind.service.axb;

import com.cqt.model.bind.axb.dto.AxbBindIdKeyInfoDTO;
import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxbHis;
import com.cqt.model.bind.axb.vo.AxbBindingVO;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.entity.PrivateRecyclePushFail;
import com.cqt.model.bind.vo.BindInfoVO;
import com.cqt.model.push.dto.AybBindPushDTO;
import com.cqt.model.push.dto.UnbindPushDTO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

/**
 * @author linshiqiang
 * @date 2022/2/16 14:02
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AxbBindConverter {

    PrivateRecyclePushFail bindRecycleDto2PrivateRecyclePushFail(BindRecycleDTO bindRecycleDTO);

    PrivateBindInfoAxb axbBindingDto2BindInfoAxb(AxbBindingDTO axbBindingDTO);

    PrivateBindInfoAxbHis bindInfoAxb2BindInfoAxbHis(PrivateBindInfoAxb bindInfoAxb);

    AxbBindingVO bindInfoAxb2AxbBindingVO(PrivateBindInfoAxb bindInfoAxb);

    AxbBindIdKeyInfoDTO bindInfoAxb2AxbBindIdKeyInfoDTO(PrivateBindInfoAxb bindInfoAxb);

    BindRecycleDTO bindInfoAxb2BindRecycleDTO(PrivateBindInfoAxb bindInfoAxb);


    BindInfoVO bindInfoAxb2BindInfoVo(PrivateBindInfoAxb bindInfoAxb);

    @Mappings({
            @Mapping(source = "sourceBindId", target = "sourceBindId"),
            @Mapping(target = "type", source = "type", ignore = true),
    })
    AybBindPushDTO bindInfoAxb2AybBindPushDTO(PrivateBindInfoAxb privateBindInfoAxb);

    PrivateBindInfoAxb bindIdKeyInfoDTO2axbBindIdKeyInfoDTO(AxbBindIdKeyInfoDTO axbBindIdKeyInfoDTO);

    @Mappings({
            @Mapping(source = "aybExpiration", target = "expiration"),
            @Mapping(source = "aybAreaCode", target = "areaCode"),
            @Mapping(source = "aybAudioACallX", target = "audioACallX"),
            @Mapping(source = "aybAudioBCallX", target = "audioBCallX"),
            @Mapping(source = "aybAudioACalledX", target = "audioACalledX"),
            @Mapping(source = "aybAudioBCalledX", target = "audioBCalledX"),
            @Mapping(source = "aybAudioACallXBefore", target = "audioACallXBefore"),
            @Mapping(source = "aybAudioBCallXBefore", target = "audioBCallXBefore"),
            @Mapping(source = "bindId", target = "sourceBindId"),
            @Mapping(source = "requestId", target = "sourceRequestId"),
            @Mapping(source = "areaCode", target = "sourceAreaCode"),
            @Mapping(source = "telXExt", target = "sourceExtNum"),
            @Mapping(source = "createTime", target = "sourceBindTime", dateFormat = "yyyy-MM-dd HH:mm:ss"),
    })
    AxbBindingDTO bindInfoAxe2AxbBindingDto(PrivateBindInfoAxe bindInfoAxe);

    UnbindPushDTO bindInfoAxb2UnbindPushDTO(PrivateBindInfoAxb bindInfoAxb);

    PrivateBindInfoAxb bindRecycleDTO2bindInfoAxb(BindRecycleDTO bindRecycleDTO);

    BindInfoVO bindInfoAxb2BindInfoVO(PrivateBindInfoAxb infoAxb);

    BindRecycleDTO privateRecyclePushFail2BindRecycleDTO(PrivateRecyclePushFail pushFail);

}
