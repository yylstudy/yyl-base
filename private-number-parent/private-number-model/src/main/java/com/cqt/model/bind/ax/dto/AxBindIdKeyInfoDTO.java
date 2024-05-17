package com.cqt.model.bind.ax.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * @since 2021-09-09 14:42:34
 *  bindId 的key的信息ax
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AxBindIdKeyInfoDTO implements Serializable {

    private static final long serialVersionUID = 4582154713971282693L;

    /**
     * 请求ID,request_id相同的请求，须返回相同的结果。
     * request_id不同的请求，代表不同的绑定申请，即使tel_a、tel_b相同，也应返回不同的tel_x。
     */
    @JsonProperty("request_id")
    private String requestId;

    /**
     * 号码A, 可以接受手机号码、固定电话。固话有区号且为全数字。
     */
    @JsonProperty("tel_a")
    private String telA;

    /**
     * 号码B, 可以接受手机号码、固定电话。固话有区号且为全数字。
     */
    @JsonProperty("tel_b")
    private String telB;

    /**
     * 虚拟号码
     */
    @JsonProperty("tel_x")
    private String telX;


    private String bindId;

    private String areaCode;

    private String cityCode;

    /**
     * 供应商id
     */
    private String supplierId;

}
