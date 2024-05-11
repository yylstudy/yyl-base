package com.linkcircle.demo;

import com.linkcircle.basecom.entity.BaseEntity;
import lombok.Data;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysConfig extends BaseEntity {
    private Long id;
    /**
     * 参数key
     */
    private String key;
    /**
     * 参数的值
     */
    private String value;
    /**
     * 参数名称
     */
    private String name;
    /**
     * 备注
     */
    private String remark;

}
