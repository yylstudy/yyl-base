package com.linkcircle.mq;

import com.linkcircle.mq.config.CustomListenerContainerConfiguration;
import com.linkcircle.mq.config.CustomRabbitListenerAnnotationBeanPostProcessor;
import com.linkcircle.mq.config.MqApplicationContext;
import com.linkcircle.mq.config.MqConfiguration;
import org.springframework.context.annotation.Import;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/5/6 18:36
 */
@Import({CustomRabbitListenerAnnotationBeanPostProcessor.class, MqApplicationContext.class,
        CustomListenerContainerConfiguration.class, MqConfiguration.class})
public class MqAutoConfiguration {
}
