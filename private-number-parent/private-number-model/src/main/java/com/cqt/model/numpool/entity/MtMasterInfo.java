package com.cqt.model.numpool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 美团主副池配置信息
 *
 * @author hlx
 */
@Data
@TableName("mt_master_info")
public class MtMasterInfo {

    /**
     * uuid
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 企业id
     */
    private String vccid;

    /**
     * 主池数量
     */
    private int num;

    /**
     * 添加人
     */
    private String createBy;

    /**
     * 添加时间
     */
    private String createTime;
}
