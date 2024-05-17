package com.cqt.model.bind.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author linshiqiang
 * @date 2021/12/1 14:39
 * AXB用户初始化号码池标志记录
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MtBindInitUserTelPool implements Serializable {

    private static final long serialVersionUID = 353889720843737549L;

    /**
     * 主键 area_code:tel
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 地市编码
     */
    private String areaCode;

    /**
     * 用户号码 tel_a/tel_b
     */
    private String tel;

    /**
     * 初始化时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 初始化标志 1 初始化主池, 2 使用了备池
     */
    private Integer initFlag;

    @Version
    private Integer version;

}
