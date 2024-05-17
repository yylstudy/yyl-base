package com.cqt.model.client.dto;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-05 14:28
 * SDK 挂断参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientHangupDTO extends ClientRequestBaseDTO implements Serializable {

    /**
     * 是 需要挂断的通话uuid
     */
    @NotEmpty(message = "[uuid]不能为空!")
    private String uuid;

}
