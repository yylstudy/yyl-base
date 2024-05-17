package com.cqt.hmyc.web.blacklist.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author linshiqiang
 * date:  2024-02-04 10:11
 * 来电黑名单
 */
@Data
@TableName("private_caller_number_blacklist")
public class CallerNumberBlacklist {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String vccId;

    private String callerNumber;

    private String calleeNumber;

    private String businessType;

    private Date createTime;
}
