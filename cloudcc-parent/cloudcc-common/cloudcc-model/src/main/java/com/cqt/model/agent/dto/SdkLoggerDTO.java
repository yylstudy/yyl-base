package com.cqt.model.agent.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-11-24 10:00
 */
@Data
public class SdkLoggerDTO implements Serializable {

    private static final long serialVersionUID = -5136281054498158839L;
    
    private String json;

    private String empAccId;
}
