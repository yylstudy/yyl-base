package com.cqt.model.client.vo;

import com.cqt.model.client.base.ClientResponseBaseVO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-06-29 9:42
 * SDK 接听 响应
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ClientAnswerVO extends ClientResponseBaseVO implements Serializable {

    private static final long serialVersionUID = 3113424155698909917L;

    /**
     * 呼入uuid
     */
    private String uuid;

    private Integer audio;

    private Integer video;

    @JsonProperty("callin_channel")
    private String callinChannel;

}
