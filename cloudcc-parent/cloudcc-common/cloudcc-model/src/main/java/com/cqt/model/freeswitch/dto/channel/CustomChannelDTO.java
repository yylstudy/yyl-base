package com.cqt.model.freeswitch.dto.channel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-06 16:00
 * 自定义通道变量值
 */
@Data
public class CustomChannelDTO implements Serializable {

    private static final long serialVersionUID = 1307574489723655718L;

    /**
     * 关联主通话id
     */
    private String mainCallId;

    /**
     * 构造对象
     */
    public static CustomChannelDTO build(String mainCallId) {
        CustomChannelDTO customChannelDTO = new CustomChannelDTO();
        customChannelDTO.setMainCallId(mainCallId);
        return customChannelDTO;
    }

    public String toJson(ObjectMapper objectMapper) throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }
}
