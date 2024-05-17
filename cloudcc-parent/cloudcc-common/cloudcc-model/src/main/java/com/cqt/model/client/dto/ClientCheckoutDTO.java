package com.cqt.model.client.dto;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-28 17:50
 * SDK 签出入参
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientCheckoutDTO extends ClientRequestBaseDTO implements Serializable {

    private static final long serialVersionUID = 533601836473292063L;

    /**
     * 是否通知前端
     */
    private Boolean reply;

}
