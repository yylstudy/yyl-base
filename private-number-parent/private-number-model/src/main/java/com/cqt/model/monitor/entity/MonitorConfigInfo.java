package com.cqt.model.monitor.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 平台中间件和漫游号地址配置实体类
 * @author hlx
 * @date 2022-01-25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("monitor_config_info")
public class MonitorConfigInfo {


    /**
     * uuid
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 平台名称
     */
    private String platform;

    /**
     * 配置url
     */
    private String url;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 设备地址
     */
    private String host;

    /**
     * 设备端口
     */
    private Integer port;

    /**
     * 配置类型  redis  mq  msrn漫游号
     */
    private String type;
}
