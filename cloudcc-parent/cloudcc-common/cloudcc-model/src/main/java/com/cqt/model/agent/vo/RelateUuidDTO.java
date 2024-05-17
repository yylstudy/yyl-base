package com.cqt.model.agent.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-31 16:35
 * 关联的通话id
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RelateUuidDTO implements Serializable {

    private static final long serialVersionUID = 3724824762488373138L;

    /**
     * 正常外呼或呼入桥接的uuid
     */
    private String bridgeUUID;

    /**
     * 咨询人员的uuid
     */
    private String consultUUID;

    /**
     * 咨询-转接人员的uuid
     */
    private String consulTransUUID;

    /**
     * 盲转-转接人员的uuid
     */
    private String blindTransUUID;

    /**
     * 三方通话人员
     */
    private String threeWayUUID;

    /**
     * 当前坐席被 管理员发起代接uuid
     */
    private String substituteUUID;

    /**
     * 呼入客户uuid
     */
    private String clientUUID;
}
