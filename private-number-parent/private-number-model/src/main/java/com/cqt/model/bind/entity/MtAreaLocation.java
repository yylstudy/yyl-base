package com.cqt.model.bind.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 美团 地市编码和机房对应关系
 *
 * @author linshiqiang
 * @since 2021-09-09 14:42:34
 */
@Data
@Builder
public class MtAreaLocation implements Serializable {

    private static final long serialVersionUID = -7662572180509120330L;

    @TableId(type = IdType.INPUT)
    private String areaCode;

    /**
     * 初始化位置, 在redis异常时修改
     */
    private String initLocation;

    /**
     * 变更的位置
     */
    private String updateLocation;

    private String createTime;

    private String updateTime;
}
