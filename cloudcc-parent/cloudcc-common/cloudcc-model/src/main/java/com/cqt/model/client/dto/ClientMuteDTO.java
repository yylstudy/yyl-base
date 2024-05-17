package com.cqt.model.client.dto;

import com.cqt.model.client.base.ClientRequestBaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-05 14:28
 * SDK 静音/取消静音参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClientMuteDTO extends ClientRequestBaseDTO implements Serializable {

    /**
     * 是(0否1是) 静音/取消静音
     */
    private String mute;

    /**
     * 是 需要保持的通话uuid
     */
    private String uuid;

}
