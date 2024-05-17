package com.cqt.monitor.web.switchroom.mysql;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2022/2/22 13:39
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MysqlUri {

    private String url;

    private String username;

    private String password;

}
