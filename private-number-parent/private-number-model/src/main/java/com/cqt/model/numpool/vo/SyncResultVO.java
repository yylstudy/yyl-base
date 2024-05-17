package com.cqt.model.numpool.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2022/5/27 16:02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SyncResultVO {

    private String ip;

    private Boolean success;

    private String message;
}
