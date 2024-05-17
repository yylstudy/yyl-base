package com.cqt.client.service;

import com.cqt.base.enums.SdkErrCode;
import com.cqt.model.agent.dto.SdkLoggerDTO;
import com.cqt.model.client.vo.ClientRequestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-11-24 9:58
 */
@Slf4j
@Service
public class SdkLoggerService {

    public ClientRequestVO<Void> logger(SdkLoggerDTO skillAgentDTO) {
        log.info("坐席: {}, 日志: {}", skillAgentDTO.getEmpAccId(), skillAgentDTO.getJson());
        return ClientRequestVO.response(SdkErrCode.OK);
    }
}
