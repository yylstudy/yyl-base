package com.cqt.model.queue.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-18 16:17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CallInIvrActionVO implements Serializable {

    /**
     * 分配的坐席id
     */
    @JsonProperty("agent_id")
    private String agentId;
}
