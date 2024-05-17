package com.cqt.model.company.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author linshiqiang
 * date:  2023-11-18 15:47
 */
@Data
@TableName("cloudcc_platform_config")
public class PlatformConfig {

    private String id;

    private String configCode;

    private String configValue;

    private String configType;
}
