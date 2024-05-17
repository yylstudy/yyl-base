package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2021/11/3 13:51
 * 1:只需要联系telB（只能使用x虚拟号接通被叫B，使用y1到y5拨打失败，放统一提示音）
 * 2:需要能联系到所有telB（可以使用x,y1到y5虚拟号联系B，并成功通话）
 * 以上两种模式中所有B号码呼叫tel_x都能转接到tel_a号码, tel_a呼叫tel_x转接到tel_b号码；
 */
@Getter
public enum ModeEnum {

    /**
     * 只需要联系telB（只能使用x虚拟号接通被叫B，使用y1到y5拨打失败，放统一提示音）
     */
    ONLY_B(1),

    /**
     * 需要能联系到所有telB（可以使用x,y1到y5虚拟号联系B，并成功通话）
     */
    ALL_B(2);

    private final Integer code;


    ModeEnum(Integer code) {
        this.code = code;
    }
}
