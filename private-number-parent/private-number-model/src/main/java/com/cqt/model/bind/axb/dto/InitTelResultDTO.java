package com.cqt.model.bind.axb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2022/4/6 11:28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InitTelResultDTO {

    /**
     * 是否第一次初始化
     */
    private Boolean firstInit;

    /**
     * 初始化标识
     */
    private String initFlag;

}
