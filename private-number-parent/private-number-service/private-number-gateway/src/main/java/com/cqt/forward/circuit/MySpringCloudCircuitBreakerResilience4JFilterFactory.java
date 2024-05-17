package com.cqt.forward.circuit;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.gateway.support.ServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description
 * @createTime 2022/1/13 14:57
 */
@Component
public class MySpringCloudCircuitBreakerResilience4JFilterFactory extends MySpringCloudCircuitBreakerFilterFactory {

    public MySpringCloudCircuitBreakerResilience4JFilterFactory(
            ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory,
            ObjectProvider<DispatcherHandler> dispatcherHandlerProvider) {
        super(reactiveCircuitBreakerFactory, dispatcherHandlerProvider);
    }

    @Override
    public String name() {
        return "AllExceptionCircuitBreaker";
    }

    @Override
    protected Mono<Void> handleErrorWithoutFallback(Throwable t) {
        if (t instanceof java.util.concurrent.TimeoutException) {
            return Mono.error(new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,
                    t.getMessage(), t));
        }
        if (t instanceof CallNotPermittedException) {
            return Mono.error(new ServiceUnavailableException());
        }
        return Mono.error(t);
    }


}
