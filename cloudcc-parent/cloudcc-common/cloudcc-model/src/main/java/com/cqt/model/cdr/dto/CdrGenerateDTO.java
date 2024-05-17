package com.cqt.model.cdr.dto;

import com.cqt.model.agent.vo.CallUuidContext;
import com.cqt.model.freeswitch.dto.event.CallStatusEventDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-07-13 19:11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CdrGenerateDTO implements Serializable {

    private static final long serialVersionUID = 5602247807380373752L;

    /**
     * 挂断事件消息
     */
    private CallStatusEventDTO callStatusEventDTO;

    /**
     * 当前挂断的uuid上下文
     */
    private CallUuidContext callUuidContext;
}
