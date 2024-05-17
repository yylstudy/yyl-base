package com.cqt.broadnet.common.model.x.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author linshiqiang
 * date:  2023-02-16 13:56
 * 1.2.2.2.6 异常号码状态同步接口入参
 * &secret_no=15100000000&status=0&vendor_key=CMCC
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExceptionNoSyncDTO extends BaseAuthDTO {

    /**
     * 异常的中间号码。
     */
    @JsonProperty("secret_no")
    private String secretNo;

    /**
     * 异常的原因
     */
    @JsonProperty("exception_msg")
    private String exceptionMsg;

    /**
     * 0-异常状态
     * 1-可恢复正常使用
     */
    @JsonProperty("status")
    private Integer status;

    /**
     * 供应商合作KEY
     * 取值样例：CMCC
     */
    @JsonProperty("vendor_key")
    private String vendorKey;
}
