package com.cqt.forward.circuit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/1/12 15:53
 */
@Configuration
public class CustomizeCircuitBreakerConfig {

    private final MyCircuitBreakerProperties myCircuitBreakerProperties;

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CustomizeCircuitBreakerConfig(MyCircuitBreakerProperties myCircuitBreakerProperties, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.myCircuitBreakerProperties = myCircuitBreakerProperties;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @RefreshScope
    @Bean
    @SuppressWarnings("all")
    public MyReactiveResilience4JCircuitBreakerFactory defaultCustomizer() {
        /*
         * 1. 时间窗口 sliding-window-size=30
         * 2. 调用次数达到 minimum-number-of-calls=5, 开始统计失败率
         * 3. 调用次数达到5次, 成功3次, 失败2次,
         * 4. 调用失败率阈值failure-rate-threshold=20,  此时 失败率=2/5=40%  > 20%  熔断器OPEN
         * 5. 等待wait-duration-in-open-state=60s, 从OPEN到HALF_OPEN等待60秒
         */

        MyCircuitBreakerProperties.InstanceProperties instanceProperties = myCircuitBreakerProperties.getBackendConfig("default");
        CircuitBreakerConfig circuitBreakerConfig;
        if (instanceProperties == null) {
            circuitBreakerConfig = getCircuitBreakerConfig();
        } else {
            circuitBreakerConfig = CircuitBreakerConfig.custom()
                    // 滑动窗口的类型为时间窗口
                    .slidingWindowType(instanceProperties.getSlidingWindowType())
                    // 时间窗口的大小为10秒
                    .slidingWindowSize(instanceProperties.getSlidingWindowSize())
                    // 在单位时间窗口内最少需要5次调用才能开始进行统计计算
                    .minimumNumberOfCalls(instanceProperties.getMinimumNumberOfCalls())
                    // 在单位时间窗口内调用失败率达到10%后会启动断路器
                    .failureRateThreshold(instanceProperties.getFailureRateThreshold())
                    // 允许断路器自动由打开状态转换为半开状态
                    .enableAutomaticTransitionFromOpenToHalfOpen()
                    // 在半开状态下允许进行正常调用的次数
                    .permittedNumberOfCallsInHalfOpenState(instanceProperties.getPermittedNumberOfCallsInHalfOpenState())
                    // 断路器打开状态转换为半开状态需要等待60秒
                    .waitDurationInOpenState(instanceProperties.getWaitDurationInOpenState())
                    // 所有异常都当作失败来处理
                    .recordExceptions(Throwable.class)
                    .build();
        }


        MyReactiveResilience4JCircuitBreakerFactory factory = new MyReactiveResilience4JCircuitBreakerFactory();

        factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(myCircuitBreakerProperties.getTimeoutDuration()).build())
                .circuitBreakerConfig(circuitBreakerConfig).build());

        MyCircuitBreakerProperties.InstanceProperties instance = myCircuitBreakerProperties.getBackendInstance("otherRoom");
        if (instance == null) {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.getAllCircuitBreakers()
                    .filter(breaker -> "otherRoom".equals(breaker.getName()))
                    .getOrNull();
            if (circuitBreaker != null) {
                CircuitBreaker otherRoomCircuitBreaker = CircuitBreaker.of("otherRoom", circuitBreakerConfig);
                circuitBreakerRegistry.replace("otherRoom", otherRoomCircuitBreaker);
            } else {
                circuitBreakerRegistry.circuitBreaker("otherRoom", circuitBreakerConfig);
            }
        }

        return factory;
    }

    private CircuitBreakerConfig getCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                // 滑动窗口的类型为时间窗口
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.TIME_BASED)
                // 时间窗口的大小为10秒
                .slidingWindowSize(10)
                // 在单位时间窗口内最少需要5次调用才能开始进行统计计算
                .minimumNumberOfCalls(5)
                // 在单位时间窗口内调用失败率达到10%后会启动断路器
                .failureRateThreshold(10)
                // 允许断路器自动由打开状态转换为半开状态
                .enableAutomaticTransitionFromOpenToHalfOpen()
                // 在半开状态下允许进行正常调用的次数
                .permittedNumberOfCallsInHalfOpenState(10)
                // 断路器打开状态转换为半开状态需要等待60秒
                .waitDurationInOpenState(Duration.ofSeconds(60))
                // 所有异常都当作失败来处理
                .recordExceptions(Throwable.class)
                .build();
    }

}
