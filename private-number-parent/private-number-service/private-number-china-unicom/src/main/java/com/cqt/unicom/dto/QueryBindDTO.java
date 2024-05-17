package com.cqt.unicom.dto;

import com.cqt.model.bind.query.BindInfoApiQuery;
import lombok.Data;

/**
 * @author huweizhong
 * date  2023/7/5 19:25
 * 联通查询AXB绑定关系
 */
@Data
public class QueryBindDTO {

    private String appId;

    private String timestamp;

    private String sign;

    private String callId;

    private String caller;

    private String callee;

    public BindInfoApiQuery buildBindInfoApiQuery() {
        BindInfoApiQuery bindInfoApiQuery = new BindInfoApiQuery();
        bindInfoApiQuery.setCaller(caller);
        bindInfoApiQuery.setCalled(callee);
        bindInfoApiQuery.setCallId(callId);
        return bindInfoApiQuery;
    }

}
