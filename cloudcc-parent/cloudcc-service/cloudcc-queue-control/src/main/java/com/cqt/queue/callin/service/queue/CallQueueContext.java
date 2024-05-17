package com.cqt.queue.callin.service.queue;

import com.cqt.model.queue.dto.TransferAgentQueueDTO;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * date:  2023-10-13 15:10
 */
@Getter
@Setter
public class CallQueueContext implements Serializable {

    private static final long serialVersionUID = -1630404878455834564L;
    
    /**
     * 企业id
     */
    private String companyCode;

    /**
     * 空闲的坐席
     */
    private List<TransferAgentQueueDTO> freeAgents;

    /**
     * 排队的客户
     */
    private List<UserQueueUpDTO> userQueueUpList;

    /**
     * 根据排队策略分组
     */
    private Map<Integer, List<UserQueueUpDTO>> queueStrategyMap;

    /**
     * 模型创建出错时的错误信息
     */
    private String errorMsg;

    public CallQueueContext(String companyCode, List<TransferAgentQueueDTO> freeAgents, List<UserQueueUpDTO> userQueueUpList) {
        this.companyCode = companyCode;
        this.freeAgents = freeAgents;
        this.userQueueUpList = userQueueUpList;
    }

    public void removeQueueUser(UserQueueUpDTO userQueueUpDTO) {
        userQueueUpList.remove(userQueueUpDTO);
    }
}
