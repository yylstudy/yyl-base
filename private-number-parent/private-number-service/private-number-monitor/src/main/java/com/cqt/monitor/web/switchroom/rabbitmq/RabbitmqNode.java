package com.cqt.monitor.web.switchroom.rabbitmq;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2022/1/21 15:46
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RabbitmqNode {

    private String host;

    private Integer port;

    private String username;

    private String password;

    private String uri;

    private Integer timeout;

    /**
     * mq检测类型  1 Java代码, 0 15672接口
     */
    private Integer mqCheckType;

}
