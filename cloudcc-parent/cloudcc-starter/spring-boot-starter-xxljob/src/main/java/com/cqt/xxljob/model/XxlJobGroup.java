package com.cqt.xxljob.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author linshiqiang
 * date 2023-02-02 14:03
 */
@Data
public class XxlJobGroup {

    private int id;

    private String appname;

    private String title;

    /**
     * 执行器地址类型：0=自动注册、1=手动录入
     */
    private int addressType;

    /**
     * 执行器地址列表，多地址逗号分隔(手动录入)
     */
    private String addressList;

    private Date updateTime;

    // registry list
    private List<String> registryList;  // 执行器地址列表(系统注册)

}
