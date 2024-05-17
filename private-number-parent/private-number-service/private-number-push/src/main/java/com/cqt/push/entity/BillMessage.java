package com.cqt.push.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author hlx
 * @date 2021-09-15
 */
@NoArgsConstructor
@Data
@TableName("mt_fail_bill")
public class BillMessage {

    /**
     * 主键id uuid策略
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 话单信息json
     */
    private String bill;

    /**
     * 机器ip
     */
    private String ip;

    /**
     * 推送失败的url
     */
    private String url;

    /**
     * 重推次数
     */
    private int num;

    /**
     * 错误信息
     */
    private String errMsg;

    /**
     * 企业id
     */
    private String vccid;

    /**
     * 入库时间
     */
    private Date createTime;
}
