package com.cqt.thirdchinanet.porperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author huweizhong
 * date  2022/12/8 15:15
 */
@ConfigurationProperties(value = "chinanet")
@Component
@Data
public class ChinanetPorperties {

    private String chinaNetId;

}
