package com.cqt.model.client.vo;

import com.cqt.model.client.base.ClientResponseBaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 静音/取消静音响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientMuteVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    /**
     * 是(0否1是) 静音/取消静音
     */
    private String mute;

}
