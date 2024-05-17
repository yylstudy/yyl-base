package com.cqt.model.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 平台信息实体类
 * @author hlx
 * @date 2022-01-24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("monitor_platform_info")
public class MonitorPlatformInfo {

    /**
     * uuid
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 平台名称
     */
    private String platform;

    private String executeUrl;

    /**
     * 漫游号数量错误次数
     */
    private Integer errorNum;

    /**
     * 平台当前状态
     */
    private Integer type;

    /**
     * 信息
     */
    private String message;

    private String gtCode;

    /**
     * 入库时间
     */
    private Date createTime;
}
