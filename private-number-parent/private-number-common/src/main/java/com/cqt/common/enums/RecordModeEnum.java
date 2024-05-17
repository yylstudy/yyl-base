package com.cqt.common.enums;

import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2021/10/27 9:46
 * 录音方式。
 *  0：混音，即通话双方的声音混合在一个声道中。
 *  1：双声道，即通话双方的声音分别录制在左、右两个声道中。
 * 如果不携带该参数，参数值默认为0。
 */
@Getter
public enum RecordModeEnum {

    /**
     *
     */
    MIX(0, "混音，即通话双方的声音混合在一个声道中。"),

    BINAURAL(1, "双声道，即通话双方的声音分别录制在左、右两个声道中。");

    private final Integer code;
    private final String message;

    RecordModeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
