package com.cqt.model.call.dto;

import cn.hutool.core.convert.Convert;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 淘宝异常号码状态同步接口请求参数
 *
 * @author Xienx
 * @date 2023年02月06日 9:57
 */
@Data
public class AlibabaAliqinAxbVendorExceptionNoSyncRequest implements Serializable {

    private static final long serialVersionUID = -506780637927468926L;

    /**
     * 0-异常状态
     */
    public static final Integer ABNORMAL_STATUS = 0;

    /**
     * 1-可恢复正常使用
     */
    public static final Integer NORMAL_STATUS = 1;
    /**
     * 异常的中间号码
     */
    @JsonAlias("secret_no")
    @ApiModelProperty(value = "异常的中间号码", example = "17011111")
    private String secretNo;

    /**
     * 异常的原因
     */
    @JsonAlias("exception_msg")
    @ApiModelProperty(value = "异常的原因", example = "位置丢失")
    private String exceptionMsg;

    /**
     * 0-异常状态 1-可恢复正常使用
     */
    @JsonAlias("status")
    @ApiModelProperty(value = "0-异常状态 1-可恢复正常使用", example = "0")
    private Integer status;

    /**
     * 供应商KEY
     */
    @JsonAlias("vendor_key")
    @ApiModelProperty(value = "供应商KEY", example = "VENDOR_KEY")
    private String vendorKey;

    public String getApiMethodName() {
        return "alibaba.aliqin.axb.vendor.exception.no.sync";
    }

    public Map<String, String> getTextParams() {
        Map<String, String> txtParams = new HashMap<>(4);
        txtParams.put("secret_no", this.secretNo);
        txtParams.put("exception_msg", this.exceptionMsg);
        txtParams.put("status", Convert.toStr(this.status));
        txtParams.put("vendor_key", this.vendorKey);
        
        return txtParams;
    }
}
