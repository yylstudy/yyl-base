package com.cqt.model.client.vo;

import com.cqt.model.client.base.ClientResponseBaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 坐席互顶下线通知
 * @author Xienx
 * @date 2023-08-04 11:13:11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ClientKickOffVO extends ClientResponseBaseVO {

    /**
     * 操作系统
     * */
    private String os;

    /**
     * 坐席ip
     * */
    private String agentIp;
}
