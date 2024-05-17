package com.cqt.model.numpool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * date:  2023-02-20 17:36
 * 通用异常号码状态推送接口入参
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommonExceptionNoSyncDTO {

    /**
     * 中间号码
     */
    private String number;

    /**
     * 号码状态：
     * 0：可用
     * 1：不可用
     * 2：可恢复正常使用
     */
    private Integer state;

    /**
     * 异常的原因
     */
    private String reason;
}
