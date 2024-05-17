package com.cqt.model.client.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-24 14:02
 * 通知前端SDK的消息实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Deprecated
public class ClientNotifyVO implements Serializable {

    private static final long serialVersionUID = -8783894327975425856L;

    /**
     * 坐席状态变化时通知
     */
    private ClientAgentStatusChangeVO clientAgentStatusChangeVO;

    /**
     * 构建对象
     */
    public static ClientNotifyVO build(ClientAgentStatusChangeVO clientAgentStatusChangeVO) {
        return ClientNotifyVO.builder()
                .clientAgentStatusChangeVO(clientAgentStatusChangeVO)
                .build();
    }

}
