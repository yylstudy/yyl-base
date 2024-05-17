package com.cqt.call.event.incalls;

import com.cqt.base.enums.OperateTypeEnum;
import lombok.Getter;

/**
 * @author linshiqiang
 * date:  2023-08-21 18:27
 */
@Getter
public class InCallsNumberEvent {

    private final String companyCode;

    private final String mainCallId;

    private final String uuid;

    private final String number;

    private final OperateTypeEnum operateTypeEnum;

    public InCallsNumberEvent(String companyCode,
                              String mainCallId,
                              String uuid,
                              String number,
                              OperateTypeEnum operateTypeEnum) {
        this.companyCode = companyCode;
        this.mainCallId = mainCallId;
        this.uuid = uuid;
        this.number = number;
        this.operateTypeEnum = operateTypeEnum;
    }
}
