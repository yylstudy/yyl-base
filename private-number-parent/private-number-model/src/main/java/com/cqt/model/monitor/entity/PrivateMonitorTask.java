package com.cqt.model.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

/**
 * @author linshiqiang
 * @date 2022/1/25 15:16
 * 定时任务
 */
@Data
public class PrivateMonitorTask {

    @TableId(type = IdType.ASSIGN_UUID)
    private Long id;

    @TableField("task_name")
    private String taskName;

    private String type;

    private Integer status;

    private String cron;

    private Date createTime;
}
