package com.cqt.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author linshiqiang
 * @date 2021/10/27 9:46
 * 指示呼叫转接的录音格式，仅下列值有效。默认是 wav。
 *     mp3
 *     wav
 */
@Getter
@AllArgsConstructor
public enum RecordFileFormatEnum {

    /**
     *
     */
    mp3,

    wav
}
