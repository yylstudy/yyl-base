package com.cqt.model.bind.axbn.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author linshiqiang
 * @date 2022/3/22 14:41
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("private_bind_axbn_real_tel")
public class PrivateBindAxbnRealTel implements Serializable {

    private static final long serialVersionUID = -1325486265183436200L;


    /**
     * 主键 vcc_id:area_code:tel
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 企业id
     */
    private String vccId;

    /**
     * 绑定id
     */
    private String bindId;

    /**
     * 真实号码
     */
    private String tel;

    /**
     * 虚号X/Y
     */
    private String telX;

    /**
     * 以0开头的虚拟号区号（如010）
     */
    private String areaCode;

    /**
     * 创建时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
