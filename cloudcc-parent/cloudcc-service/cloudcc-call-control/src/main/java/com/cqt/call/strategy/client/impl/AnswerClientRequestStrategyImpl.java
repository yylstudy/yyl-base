package com.cqt.call.strategy.client.impl;

import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.call.converter.ModelConverter;
import com.cqt.call.strategy.client.ClientRequestStrategy;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.client.base.ClientResponseBaseVO;
import com.cqt.model.client.dto.ClientAnswerDTO;
import com.cqt.model.freeswitch.dto.api.AnswerDTO;
import com.cqt.model.freeswitch.vo.FreeswitchApiVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:42
 * 前端SDK 接听请求
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerClientRequestStrategyImpl extends AbstractClientChecker implements ClientRequestStrategy {

    private final FreeswitchRequestService freeswitchRequestService;

    @Override
    public MsgTypeEnum getMsgType() {
        return MsgTypeEnum.answer;
    }

    @Override
    public ClientResponseBaseVO deal(String requestBody) throws Exception {
        ClientAnswerDTO clientAnswerDTO = convert(requestBody, ClientAnswerDTO.class);
        // 调用底层answer接口
        AnswerDTO answerDTO = ModelConverter.INSTANCE.client2base(clientAnswerDTO);
        FreeswitchApiVO answered = freeswitchRequestService.answer(answerDTO);
        if (answered.getResult()) {
            return ClientResponseBaseVO.response(clientAnswerDTO, "0", "接听成功!");
        }
        return ClientResponseBaseVO.response(clientAnswerDTO, "1", "接听失败!");
    }

}
