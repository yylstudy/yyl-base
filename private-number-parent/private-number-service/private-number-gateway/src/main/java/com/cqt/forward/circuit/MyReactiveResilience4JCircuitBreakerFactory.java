package com.cqt.forward.circuit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreaker;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description
 * @createTime 2022/1/13 15:28
 */

public class MyReactiveResilience4JCircuitBreakerFactory extends
        ReactiveCircuitBreakerFactory<Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration, Resilience4JConfigBuilder> {
    private Function<String, Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration> defaultConfiguration = id -> new Resilience4JConfigBuilder(
            id).circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            .timeLimiterConfig(TimeLimiterConfig.ofDefaults()).build();

    private CircuitBreakerRegistry circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();

    private Map<String, Customizer<CircuitBreaker>> circuitBreakerCustomizers = new HashMap<>();

    @Override
    public ReactiveCircuitBreaker create(String id) {
        Assert.hasText(id, "A CircuitBreaker must have an id.");
        Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration config = getConfigurations()
                .computeIfAbsent(id, defaultConfiguration);
        return new ReactiveResilience4JCircuitBreaker(id, config, circuitBreakerRegistry,
                Optional.ofNullable(circuitBreakerCustomizers.get(id)));
    }

    @Override
    protected Resilience4JConfigBuilder configBuilder(String id) {
        return new Resilience4JConfigBuilder(id);
    }

    public CircuitBreakerRegistry getCircuitBreakerRegistry() {
        return circuitBreakerRegistry;
    }

    @Override
    public void configureDefault(
            Function<String, Resilience4JConfigBuilder.Resilience4JCircuitBreakerConfiguration> defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration;
    }

    public void configureCircuitBreakerRegistry(CircuitBreakerRegistry registry) {
        this.circuitBreakerRegistry = registry;
    }

    public void addCircuitBreakerCustomizer(Customizer<CircuitBreaker> customizer,
                                            String... ids) {
        for (String id : ids) {
            circuitBreakerCustomizers.put(id, customizer);
        }
    }

}
