package com.cqt.model.numpool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用主副池配置信息
 *
 * @author hlx
 */
@Data
public class PrivateMasterInfo implements Serializable {

    private static final long serialVersionUID = 4028724279677996752L;

    /**
     * uuid
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 企业id
     */
    private String vccId;

    /**
     * 地市编码
     */
    private String areaCode;

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
