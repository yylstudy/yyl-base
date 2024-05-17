package com.cqt.broadnet.common.model.axb.converter;

import com.cqt.model.bind.axb.dto.AxbBindingDTO;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxbHis;
import com.cqt.model.bind.axb.vo.AxbBindingVO;
import com.cqt.model.bind.dto.BindRecycleDTO;

/**
 * @author Xienx
 * @date 2023-05-25 19:08:19:08
 */
public final class AxbBindConverter {

    /**
     * The singleton instance of this utility class.
     */
    public static final AxbBindConverter INSTANCE = new AxbBindConverter();

    private AxbBindConverter() {

    }

    /**
     * AxbBindingDTO convert to PrivateBindInfoAxb
     *
     * @param axbBindingDTO axbBindingDTO
     * @return PrivateBindInfoAxb
     */
    public PrivateBindInfoAxb axbBindingDTO2BindInfoAxb(AxbBindingDTO axbBindingDTO) {
        PrivateBindInfoAxb bindInfoAxb = new PrivateBindInfoAxb();

        bindInfoAxb.setAppkey(axbBindingDTO.getAppkey());
        bindInfoAxb.setTs(axbBindingDTO.getTs());
        bindInfoAxb.setSign(axbBindingDTO.getSign());
        bindInfoAxb.setBindId(axbBindingDTO.getBindId());
        bindInfoAxb.setRequestId(axbBindingDTO.getRequestId());
        bindInfoAxb.setVccId(axbBindingDTO.getVccId());
        bindInfoAxb.setTelA(axbBindingDTO.getTelA());
        bindInfoAxb.setTelB(axbBindingDTO.getTelB());
        bindInfoAxb.setTelX(axbBindingDTO.getTelX());
        bindInfoAxb.setAreaCode(axbBindingDTO.getAreaCode());
        bindInfoAxb.setCityCode(axbBindingDTO.getCityCode());
        bindInfoAxb.setWholeArea(axbBindingDTO.getWholeArea());
        bindInfoAxb.setExpiration(axbBindingDTO.getExpiration());
        bindInfoAxb.setType(axbBindingDTO.getType());
        bindInfoAxb.setAudioACallX(axbBindingDTO.getAudioACallX());
        bindInfoAxb.setAudioBCallX(axbBindingDTO.getAudioBCallX());
        bindInfoAxb.setAudioOtherCallX(axbBindingDTO.getAudioOtherCallX());
        bindInfoAxb.setAudioACalledX(axbBindingDTO.getAudioACalledX());
        bindInfoAxb.setAudioBCalledX(axbBindingDTO.getAudioBCalledX());
        bindInfoAxb.setAudioACallXBefore(axbBindingDTO.getAudioACallXBefore());
        bindInfoAxb.setAudioBCallXBefore(axbBindingDTO.getAudioBCallXBefore());
        bindInfoAxb.setEnableRecord(axbBindingDTO.getEnableRecord());
        bindInfoAxb.setExpireTime(axbBindingDTO.getExpireTime());
        bindInfoAxb.setUserData(axbBindingDTO.getUserData());
        bindInfoAxb.setMaxDuration(axbBindingDTO.getMaxDuration());
        bindInfoAxb.setAybOtherShow(axbBindingDTO.getAybOtherShow());
        bindInfoAxb.setAxeybTelX(axbBindingDTO.getAxeybTelX());
        bindInfoAxb.setSourceBindId(axbBindingDTO.getSourceBindId());
        bindInfoAxb.setSourceRequestId(axbBindingDTO.getSourceRequestId());
        bindInfoAxb.setSourceBindTime(axbBindingDTO.getSourceBindTime());
        bindInfoAxb.setSourceAreaCode(axbBindingDTO.getSourceAreaCode());
        bindInfoAxb.setSourceExtNum(axbBindingDTO.getSourceExtNum());
        bindInfoAxb.setRecordFileFormat(axbBindingDTO.getRecordFileFormat());
        bindInfoAxb.setModel(axbBindingDTO.getModel());
        bindInfoAxb.setRecordMode(axbBindingDTO.getRecordMode());
        bindInfoAxb.setDualRecordMode(axbBindingDTO.getDualRecordMode());
        bindInfoAxb.setLastMinVoice(axbBindingDTO.getLastMinVoice());
        bindInfoAxb.setDirectTelX(axbBindingDTO.getDirectTelX());

        return bindInfoAxb;
    }

    /**
     * PrivateBindInfoAxb convert to PrivateBindInfoAxbHis
     *
     * @param bindInfoAxb bindInfoAxb
     * @return PrivateBindInfoAxb
     */
    public PrivateBindInfoAxbHis bindInfoAxb2BindInfoAxbHis(PrivateBindInfoAxb bindInfoAxb) {
        if (bindInfoAxb == null) {
            return null;
        }
        PrivateBindInfoAxbHis privateBindInfoAxbHis = new PrivateBindInfoAxbHis();

        privateBindInfoAxbHis.setBindId(bindInfoAxb.getBindId());
        privateBindInfoAxbHis.setRequestId(bindInfoAxb.getRequestId());
        privateBindInfoAxbHis.setSupplierId(bindInfoAxb.getSupplierId());
        privateBindInfoAxbHis.setSourceBindId(bindInfoAxb.getSourceBindId());
        privateBindInfoAxbHis.setTelA(bindInfoAxb.getTelA());
        privateBindInfoAxbHis.setTelB(bindInfoAxb.getTelB());
        privateBindInfoAxbHis.setTelX(bindInfoAxb.getTelX());
        privateBindInfoAxbHis.setVccId(bindInfoAxb.getVccId());
        privateBindInfoAxbHis.setAreaCode(bindInfoAxb.getAreaCode());
        privateBindInfoAxbHis.setCreateTime(bindInfoAxb.getCreateTime());
        privateBindInfoAxbHis.setExpireTime(bindInfoAxb.getExpireTime());

        return privateBindInfoAxbHis;
    }

    /**
     * PrivateBindInfoAxb convert to AxbBindingVO
     *
     * @param bindInfoAxb bindInfoAxb
     * @return AxbBindingVO
     */
    public AxbBindingVO bindInfoAxb2AxbBindingVO(PrivateBindInfoAxb bindInfoAxb) {
        if (bindInfoAxb == null) {
            return null;
        }

        AxbBindingVO axbBindingVO = new AxbBindingVO();

        axbBindingVO.setTelX(bindInfoAxb.getTelX());
        axbBindingVO.setBindId(bindInfoAxb.getBindId());

        return axbBindingVO;
    }


    public BindRecycleDTO bindInfoAxb2BindRecycleDTO(PrivateBindInfoAxb bindInfoAxb) {
        if (bindInfoAxb == null) {
            return null;
        }

        BindRecycleDTO bindRecycleDTO = new BindRecycleDTO();

        bindRecycleDTO.setRequestId(bindInfoAxb.getRequestId());
        bindRecycleDTO.setTelA(bindInfoAxb.getTelA());
        bindRecycleDTO.setTelB(bindInfoAxb.getTelB());
        bindRecycleDTO.setTelX(bindInfoAxb.getTelX());
        bindRecycleDTO.setExpireTime(bindInfoAxb.getExpireTime());
        bindRecycleDTO.setAreaCode(bindInfoAxb.getAreaCode());
        bindRecycleDTO.setCityCode(bindInfoAxb.getCityCode());
        bindRecycleDTO.setVccId(bindInfoAxb.getVccId());
        bindRecycleDTO.setBindId(bindInfoAxb.getBindId());
        bindRecycleDTO.setSupplierId(bindInfoAxb.getSupplierId());
        bindRecycleDTO.setDirectTelX(bindInfoAxb.getDirectTelX());

        return bindRecycleDTO;
    }

    
}
