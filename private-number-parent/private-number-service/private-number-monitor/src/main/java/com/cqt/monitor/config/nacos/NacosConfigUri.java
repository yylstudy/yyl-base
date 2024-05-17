package com.cqt.monitor.config.nacos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2022/1/24 9:36
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NacosConfigUri {

    private String serverAddr;

    private String username;

    private String password;

    private String namespace;

    private String group;

}
