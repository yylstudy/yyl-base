package com.cqt.model.bind.axebn.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * @since 2021-09-09 14:42:34
 * AXEBN bindId 的key的信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AxebnBindIdKeyInfoDTO implements Serializable {

    private static final long serialVersionUID = 4582154713971282693L;

    /**
     * 请求ID,request_id相同的请求，须返回相同的结果。
     * request_id不同的请求，代表不同的绑定申请，即使tel_a、tel_b相同，也应返回不同的tel_x。
     */
    private String requestId;

    private String bindId;

    /**
     * 虚拟号码
     */
    private String telX;

    private String telXExt;

    private String telA;

    private String telB;

    private String areaCode;

    private String cityCode;

}
