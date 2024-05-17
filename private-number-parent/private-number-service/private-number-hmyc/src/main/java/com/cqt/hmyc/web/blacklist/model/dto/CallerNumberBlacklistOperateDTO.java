package com.cqt.hmyc.web.blacklist.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2024-02-04 10:14
 */
@Data
public class CallerNumberBlacklistOperateDTO {

    /**
     * 对号码列表要进行的操作：
     * 0：加入黑名单
     * 1：解除黑名单
     * 默认取值为 0
     */
    @NotNull(message = "opType 不能为空")
    private Integer opType;

    /**
     * 要加入黑名单的号码列表
     */
    @NotNull(message = "numInfoList 不能为空")
    private List<String> numInfoList;

}
