package com.cqt.model.monitor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 执行脚本配置实体类
 * @author hlx
 * @date 2022-01-25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("monitor_execute_info")
public class MonitorExecuteInfo {

    /**
     * uuid
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 平台
     */
    private String platform;

    /**
     *  调用的url
     */
    private String executeUrl;

    /**
     *  脚本执行参数
     */
    private String bash;

    /**
     *  脚本类型
     */
    private String type;

    /**
     *  GT码
     */
    private String gtCode;
}
