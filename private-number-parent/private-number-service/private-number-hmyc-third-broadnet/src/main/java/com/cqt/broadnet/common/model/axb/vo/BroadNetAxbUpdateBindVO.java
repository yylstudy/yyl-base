package com.cqt.broadnet.common.model.axb.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author Xienx
 * @date 2023-05-26 11:29:11:29
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BroadNetAxbUpdateBindVO extends BaseBroadNetVO implements Serializable {

    private static final long serialVersionUID = -7886024785352320376L;

    /**
     * 新号码X. 请求类型为2时（服务状态检查）
     */
    private String newTelX;
}
