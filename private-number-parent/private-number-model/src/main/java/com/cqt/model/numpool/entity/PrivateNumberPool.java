package com.cqt.model.numpool.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * 号码池(PrivateNumberPool)实体类
 *
 * @author linshiqiang
 * @since 2021-09-09 14:42:36
 */
@Data
public class PrivateNumberPool implements Serializable {
    private static final long serialVersionUID = 221078075111860937L;

    /**
     * 主键策略uuid
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 企业id
     */
    @NotBlank(message = "企业id不能为空")
    private String vccId;

    /**
     * X号码
     */
    @NotBlank(message = "X号码不能为空")
    private String number;

    /**
     * 号码类型, AXB, AXE
     */
    @NotBlank(message = "类型不能为空")
    private String numType;

    /**
     * 短信每日发送上限
     */
    private Integer dailyShortMessage;

    /**
     * 短信每月发送上限
     */
    private Integer monthlyShortMessage;

    /**
     * 地区编码 010
     */
    @NotBlank(message = "地区不能为空")
    private String areaCode;

    /**
     * 主池  副池标识   MASTER  主池   SLAVE 副池
     */
    private String place;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;


    /**
     * 创建人
     */
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;

}
