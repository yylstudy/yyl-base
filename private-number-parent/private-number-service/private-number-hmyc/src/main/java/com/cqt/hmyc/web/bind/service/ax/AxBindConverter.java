package com.cqt.hmyc.web.bind.service.ax;

import com.cqt.model.bind.ax.dto.AxBindIdKeyInfoDTO;
import com.cqt.model.bind.ax.dto.AxBindingDTO;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAx;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAxHis;
import com.cqt.model.bind.ax.vo.AxBindingVO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.vo.BindInfoVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

/**
 * @author linshiqiang
 * @date 2022/3/15 11:37
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AxBindConverter {

    PrivateBindInfoAx axBindingDTO2bindInfoAx(AxBindingDTO axBindingDTO);

    AxBindingVO bindInfoAx2AxBindingVO(PrivateBindInfoAx bindInfoAx);

    AxBindIdKeyInfoDTO bindInfoAx2AxBindIdKeyInfoDTO(PrivateBindInfoAx bindInfoAx);

    BindRecycleDTO bindInfoAx2BindRecycleDTO(PrivateBindInfoAx bindInfoAx);

    PrivateBindInfoAxHis bindInfoAx2BindInfoAxHis(PrivateBindInfoAx privateBindInfoAx);

    PrivateBindInfoAx bindRecycleDTO2bindInfoAx(BindRecycleDTO bindRecycleDTO);

    PrivateBindInfoAx bindIdKeyInfoDTO2BindInfoAx(AxBindIdKeyInfoDTO bindIdKeyInfoDTO);

    BindInfoVO bindInfoAx2BindInfoVO(PrivateBindInfoAx bindInfoAx);

}
