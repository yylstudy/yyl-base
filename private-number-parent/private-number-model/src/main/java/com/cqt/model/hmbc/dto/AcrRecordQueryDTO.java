package com.cqt.model.hmbc.dto;


import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 话单记录查询参数定义
 *
 * @author scott
 * @date 2022年07月20日 16:45
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(value = "AcrRecordQueryDTO", description = "话单记录查询参数")
public class AcrRecordQueryDTO implements Serializable {

    private static final long serialVersionUID = -8193815179060960333L;

    /**
     * 话单表名称
     */
    private String tableName;

    /**
     * 业务标识
     */
    private String serviceKey;

    /**
     * 被叫号码
     */
    private String calledpartynumber;

    /**
     * 主叫号码
     */
    private List<String> callerNumbers;


    public AcrRecordQueryDTO(String vccId, String yyyyMm) {
        // 要查询话单的时间所在月份
        this.tableName = String.format("acr_record_%s_%s", vccId, yyyyMm);
    }
}
