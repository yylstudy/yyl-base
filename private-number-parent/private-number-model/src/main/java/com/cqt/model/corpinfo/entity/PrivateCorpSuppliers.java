package com.cqt.model.corpinfo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author linshiqiang
 * @date 2022/7/27 14:21
 */
@Data
public class PrivateCorpSuppliers {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String vccId;

    private String supplierId;

    private String city;
}
