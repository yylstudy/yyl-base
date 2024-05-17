package com.cqt.broadnet.common.model.axb.dto;

import lombok.Data;

/**
 * @author linshiqiang
 * date:  2023-06-06 13:58
 */
@Data
public class BindIdMapperDTO {

    private String bindTime;

    private String cqtBindId;

    private String requestId;

    private String supplierId;
    
    private String vccId;
}
