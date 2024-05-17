package com.cqt.model.bind.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author linshiqiang
 * @date 2021/10/15 10:13
 * mysql 锁
 */
@Data
public class PrivateLock {

    @TableId
    private String lockName;

    private Date lockTime;

    private String lockIp;
}
