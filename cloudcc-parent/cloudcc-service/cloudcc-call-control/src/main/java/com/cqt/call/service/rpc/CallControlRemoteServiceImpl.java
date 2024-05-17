package com.cqt.call.service.rpc;

import com.cqt.base.util.TraceIdUtil;
import com.cqt.call.service.DataQueryService;
import com.cqt.call.strategy.client.ClientRequestStrategyFactory;
import com.cqt.call.strategy.client.impl.AbstractClientChecker;
import com.cqt.cloudcc.manager.context.AgentInfoContext;
import com.cqt.model.client.base.ClientBase;
import com.cqt.model.client.base.ClientRequestBaseDTO;
import com.cqt.rpc.call.CallControlRemoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-06-29 10:32
 * 话务控制rpc
 */
@Slf4j
@DubboService
@Service
@RequiredArgsConstructor
public class CallControlRemoteServiceImpl extends AbstractClientChecker implements CallControlRemoteService {

    private final ClientRequestStrategyFactory clientRequestStrategyFactory;

    private final DataQueryService dataQueryService;

    @Override
    public ClientBase request(String requestBody) throws Exception {
        // 修改坐席id和分机id  企业id_坐席id, 企业id_分机id
        ClientRequestBaseDTO requestBaseDTO = convert(requestBody, ClientRequestBaseDTO.class);
        TraceIdUtil.setTraceId(getTraceId(requestBaseDTO));
        AgentInfoContext.set(dataQueryService.getAgentInfo(requestBaseDTO.getCompanyCode(), requestBaseDTO.getAgentId()));
        try {
            return clientRequestStrategyFactory.dealClientRequest(requestBaseDTO, requestBody);
        } finally {
            TraceIdUtil.remove();
        }
    }

    /**
     * 构造traceId
     */
    private String getTraceId(ClientRequestBaseDTO requestBaseDTO) {
        return TraceIdUtil.buildTraceId(requestBaseDTO.getReqId(),
                requestBaseDTO.getMsgType(),
                requestBaseDTO.getCompanyCode(),
                requestBaseDTO.getAgentId(),
                requestBaseDTO.getExtId());
    }

}
