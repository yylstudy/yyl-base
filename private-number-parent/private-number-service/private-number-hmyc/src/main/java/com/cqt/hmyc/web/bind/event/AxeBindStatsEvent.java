package com.cqt.hmyc.web.bind.event;

import com.cqt.common.enums.OperateTypeEnum;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * date:  2023-08-29 10:37
 */
@Getter
public class AxeBindStatsEvent extends ApplicationEvent {

    private static final long serialVersionUID = 9110770548322846794L;
    private final String vccId;

    private final String areaCode;

    private final OperateTypeEnum operateTypeEnum;

    public AxeBindStatsEvent(Object source, String vccId, String areaCode, OperateTypeEnum operateTypeEnum) {
        super(source);
        this.vccId = vccId;
        this.areaCode = areaCode;
        this.operateTypeEnum = operateTypeEnum;
    }
}
