package com.cqt.hmyc.web.fotile.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2021/4/8 10:33
 * 绑定关系操作返回
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BindOperationResultVO implements Serializable {

    /**
     * 流水号
     */
    @ApiModelProperty("流水号")
    private String streamNumber;

    /**
     * 消息id
     */
    @ApiModelProperty("消息id")
    private String messageId;

    /**
     * 业务id
     */
    @ApiModelProperty("业务id")
    private String businessId;

    /**
     * 绑定ID
     */
    @ApiModelProperty("绑定ID")
    private String bindId;

    /**
     * 状态码
     * 0000 成功
     */
    @ApiModelProperty("状态码")
    private String stateCode;

    /**
     * 备注描述
     */
    @ApiModelProperty("备注描述")
    private String remark;


    public static BindOperationResultVO fail(String businessId, String streamNumber, String messageId, String bindId, String remark, String stateCode) {
        return BindOperationResultVO.builder()
                .businessId(businessId)
                .streamNumber(streamNumber)
                .messageId(messageId)
                .bindId(bindId)
                .remark(remark)
                .stateCode(stateCode)
                .build();
    }

    public static BindOperationResultVO fail(String stateCode, String remark) {
        return BindOperationResultVO.builder()
                .remark(remark)
                .stateCode(stateCode)
                .build();
    }

    public static BindOperationResultVO fail(String remark) {
        return BindOperationResultVO.builder()
                .remark(remark)
                .stateCode("9999")
                .build();
    }

    public static BindOperationResultVO success(String remark) {
        return BindOperationResultVO.builder()
                .remark(remark)
                .stateCode("0000")
                .build();
    }

}
