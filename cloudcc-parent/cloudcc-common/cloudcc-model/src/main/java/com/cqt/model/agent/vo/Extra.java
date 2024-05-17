package com.cqt.model.agent.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author linshiqiang
 * date:  2023-07-14 11:10
 * 扩展字段
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Extra {

    /**
     * 工单id
     * 外呼时传入, 回调返回
     */
    private String workOrderId;
}
