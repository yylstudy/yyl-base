package com.cqt.broadnet.web.x.service;

import com.cqt.broadnet.common.model.x.dto.SmsCheckDTO;
import com.cqt.broadnet.common.model.x.vo.SmsCheckVO;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author linshiqiang
 * date:  2023-04-26 14:57
 */
public interface SmsCheckService {

    /**
     * 短信校验
     *
     * @param smsCheckDTO 参数
     * @return 响应
     */
    SmsCheckVO check(String smsCheckDTO) throws JsonProcessingException;
}
