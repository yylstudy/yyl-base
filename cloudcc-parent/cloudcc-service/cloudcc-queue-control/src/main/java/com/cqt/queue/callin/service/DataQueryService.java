package com.cqt.queue.callin.service;

import com.cqt.model.queue.dto.UserQueueUpDTO;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-07-18 18:09
 */
public interface DataQueryService {

    /**
     * 查询企业排队的客户列表
     *
     * @param companyCode 企业id
     * @return 企业排队的客户列表
     */
    List<UserQueueUpDTO> getUserQueueUpList(String companyCode);

    /**
     * 检测坐席和分机是否可用
     *
     * @param companyCode 企业id
     * @param agentId     坐席id
     * @return 是否可用
     */
    Boolean checkAgentAndExtStatus(String companyCode, String agentId);

}
