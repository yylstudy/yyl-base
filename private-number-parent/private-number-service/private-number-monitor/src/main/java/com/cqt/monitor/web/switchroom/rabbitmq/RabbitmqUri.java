package com.cqt.monitor.web.switchroom.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author linshiqiang
 * @date 2022/1/21 16:27
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RabbitmqUri {

    private Set<RabbitmqNode> hostAndPortSet;

}
