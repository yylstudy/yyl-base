package com.cqt.model.corpinfo.dto;

import lombok.Data;

/**
 * @author linshiqiang
 * date:  2022-12-20 16:21
 * 供应商与权重
 * @since 2.2.0
 */
@Data
public class SupplierWeight {

    /**
     * 供应商id
     */
    private String supplierId;

    /**
     * 供应商id hash
     */
    private String supplierIdHash;

    /**
     * 权重
     */
    private Double weight;
}
