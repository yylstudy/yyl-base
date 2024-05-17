package com.cqt.model.numpool.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/5/20 9:30
 * 号码池变化同步号码隐藏服务
 */
@Data
public class NumberChangeSyncDTO implements Serializable {

    private static final long serialVersionUID = -5453449076125775676L;

    /**
     * 操作类型  INSERT DELETE
     * OperateTypeEnum
     */
    @NotEmpty(message = "operationType不能为空")
    private String operationType;

    /**
     * 企业id
     */
    @NotEmpty(message = "vccId不能为空")
    private String vccId;

    /**
     * 业务模式
     */
    @NotEmpty(message = "businessType不能为空")
    private String businessType;

    /**
     * 号码池类型
     */
    @NotEmpty(message = "poolType不能为空")
    private String poolType;

    /**
     * key: 地市编码
     * value: 主号码池列表
     */
    private Map<String, List<String>> masterNumberMap;

    /**
     * key: 地市编码
     * value: 备号码池列表
     */
    private Map<String, List<String>> slaveNumberMap;

}
