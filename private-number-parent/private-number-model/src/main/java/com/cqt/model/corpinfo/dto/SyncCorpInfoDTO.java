package com.cqt.model.corpinfo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author linshiqiang
 * @date 2022/5/23 19:59
 * 企业信息同步
 */
@Data
public class SyncCorpInfoDTO {

    /**
     * 企业id
     */
    @NotBlank(message = "vccId 不能为空")
    private String vccId;

    /**
     * 操作类型  INSERT DELETE
     * OperateTypeEnum
     * 删除 更新 DELETE
     * 新增 INSERT
     */
    @NotBlank(message = "operationType 不能为空")
    private String operationType;
}
