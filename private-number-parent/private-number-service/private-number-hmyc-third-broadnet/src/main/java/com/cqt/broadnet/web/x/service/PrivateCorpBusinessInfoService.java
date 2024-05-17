package com.cqt.broadnet.web.x.service;

import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2023-02-20 16:41
 * 企业业务配置
 */
public interface PrivateCorpBusinessInfoService {

    /**
     * 根据x号码查询所属企业业务配置
     *
     * @param secretNo x号码
     * @return 企业业务配置
     */
    PrivateCorpBusinessInfoDTO getPrivateCorpBusinessInfoDTO(String secretNo);

    /**
     * 根据呼叫id获取绑定信息
     *
     * @param callId 呼叫id
     * @return 绑定信息
     */
    Optional<BindInfoApiVO> getBindInfoVO(String callId) throws JsonProcessingException;

    /**
     * 获取x号码归属地
     *
     * @param xNum x号码
     * @return 区号
     */
    String getAreaCode(String xNum);

    /**
     * 获取内部绑定id
     *
     * @param broadBindId 广电绑定id
     * @return 内部绑定id
     */
    String getCqtBindId(String broadBindId) throws JsonProcessingException;
}
