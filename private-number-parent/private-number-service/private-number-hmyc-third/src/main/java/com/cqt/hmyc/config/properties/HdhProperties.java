package com.cqt.hmyc.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * hdh properties
 *
 * @author dingsh
 * @date 2022/07/27
 */
@Data
@Component
@ConfigurationProperties(prefix = "third.hdh")
public class HdhProperties {

    /**
     * 和多号 编码字典
     */
    private String audioCodeDataId;

}
