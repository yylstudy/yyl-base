package com.cqt.model.unicom.dto;

import com.cqt.model.bind.query.BindInfoApiQuery;
import io.swagger.annotations.Api;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author zhengsuhao
 * @date 2022/12/5
 */
@Api(tags="联通集团总部(江苏)号码绑定查询入参")
@Data
public class NumberBindingQueryDTO {


    @NotBlank(message = "type不能为空")
    private String type;

    @NotBlank(message = "phoneNumberA不能为空")
    private String phoneNumberA;

    @NotBlank(message = "phoneNumberX不能为空")
    private String phoneNumberX;

    private String dgts;

    @NotBlank(message = "callId不能为空")
    private String callId;

    private String smsContent;

    public BindInfoApiQuery buildBindInfoApiQuery() {
        BindInfoApiQuery bindInfoApiQuery = new BindInfoApiQuery();
        bindInfoApiQuery.setCaller(phoneNumberA);
        bindInfoApiQuery.setCalled(phoneNumberX);
        bindInfoApiQuery.setDigitInfo(dgts);
        bindInfoApiQuery.setCallId(callId);
        bindInfoApiQuery.setBehaviorType("1".equals(type) ? "CALL" : "SMS");
        return bindInfoApiQuery;
    }
}
