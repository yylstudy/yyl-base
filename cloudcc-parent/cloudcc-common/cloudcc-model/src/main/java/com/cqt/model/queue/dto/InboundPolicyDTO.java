package com.cqt.model.queue.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-19 11:46
 * 呼入策略json
 */
@Data
public class InboundPolicyDTO implements Serializable {

    private static final long serialVersionUID = -1858604872433125616L;

    private Integer isEnable;

    private Integer ruleSource;

    private String companyCode;
}
