package com.cqt.model.monitor.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 切换记录实体类
 *
 * @author hlx
 * @date 2022-01-24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("monitor_toggle_info")
public class MonitorToggleInfo {

    /**
     * uuid
     */
    @TableId(type = IdType.ASSIGN_UUID)
    @JSONField(serialize = false)
    private String id;

    /**
     * 平台
     */
    private String platform;

    /**
     * 脚本类型  TOGGLE 切换 RECOVER  恢复
     */
    private String type;

    private String gtCode;
    /**
     * 执行脚本的url
     */
    private String executeUrl;

    /**
     * 结果   0 失败  1 成功
     */
    private Integer result;

    /**
     * 执行信息
     */
    private String message;

    /**
     * 执行时间
     */
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date executeTime;


    /**
     * 切换原因
     */
    @JSONField(serialize = false)
    private String reason;
}
