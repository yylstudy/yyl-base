package com.cqt.model.queue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-19 15:24
 * 进入空闲坐席队列的空闲坐席信息
 * <p>
 * 坐席离线的队列-坐席签出离线
 * 在签出时判断下有配置手机号加入队列
 * 在迁入时移除队列
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferAgentQueueDTO implements Serializable {

    private static final long serialVersionUID = -943913389831205489L;

    /**
     * 权值信息
     */
    @JsonProperty("aw")
    private AgentWeightInfoDTO agentWeightInfoDTO;

    /**
     * 进入空闲时间
     */

    @JsonProperty("ts")
    private Long timestamp;

    /**
     * 坐席id
     */
    @JsonProperty("id")
    private String agentId;

    /**
     * 离线坐席接续的手机
     */
    @JsonProperty("pn")
    private String phoneNumber;
}
