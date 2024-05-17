package com.cqt.forward.circuit;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.lang.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author linshiqiang
 * @date 2022-02-15 21:39
 */
@ConfigurationProperties(prefix = "resilience4j.circuitbreaker")
@Component
public class MyCircuitBreakerProperties {

    private Duration timeoutDuration = Duration.ofSeconds(1);

    private Map<String, InstanceProperties> configs = new HashMap<>(16);

    private Map<String, InstanceProperties> instances = new HashMap<>(16);

    public Duration getTimeoutDuration() {
        return timeoutDuration;
    }

    public void setTimeoutDuration(Duration timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
    }

    public Map<String, InstanceProperties> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, InstanceProperties> configs) {
        this.configs = configs;
    }

    public Map<String, InstanceProperties> getInstances() {
        return instances;
    }

    public void setInstances(Map<String, InstanceProperties> instances) {
        this.instances = instances;
    }

    public InstanceProperties getBackendConfig(String backend) {
        return configs.get(backend);
    }

    public InstanceProperties getBackendInstance(String backend) {
        return instances.get(backend);
    }

    /**
     * Class storing property values for configuring {@link io.github.resilience4j.circuitbreaker.CircuitBreaker}
     * instances.
     */
    public static class InstanceProperties {

        @Nullable
        private Duration waitDurationInOpenState;

        @Nullable
        private Duration slowCallDurationThreshold;

        @Nullable
        private Duration maxWaitDurationInHalfOpenState;

        @Nullable
        private Float failureRateThreshold;

        @Nullable
        private Float slowCallRateThreshold;

        @Nullable
        @Deprecated
        @SuppressWarnings("DeprecatedIsStillUsed") // Left for backward compatibility
        private Integer ringBufferSizeInClosedState;

        @Nullable
        private CircuitBreakerConfig.SlidingWindowType slidingWindowType;

        @Nullable
        private Integer slidingWindowSize;

        @Nullable
        private Integer minimumNumberOfCalls;

        @Nullable
        private Integer permittedNumberOfCallsInHalfOpenState;

        @Nullable
        @Deprecated
        @SuppressWarnings("DeprecatedIsStillUsed") // Left for backward compatibility
        private Integer ringBufferSizeInHalfOpenState;

        @Nullable
        private Boolean automaticTransitionFromOpenToHalfOpenEnabled;

        @Nullable
        private Boolean writableStackTraceEnabled;

        @Nullable
        private Boolean allowHealthIndicatorToFail;

        @Nullable
        private Integer eventConsumerBufferSize;

        @Nullable
        private Boolean registerHealthIndicator;

        @Nullable
        private Class<Predicate<Throwable>> recordFailurePredicate;

        @Nullable
        private Class<? extends Throwable>[] recordExceptions;

        @Nullable
        private Class<? extends Throwable>[] ignoreExceptions;

        @Nullable
        private String baseConfig;

        /**
         * flag to enable Exponential backoff policy or not for retry policy delay
         */
        @Nullable
        private Boolean enableExponentialBackoff;

        /**
         * exponential backoff multiplier value
         */
        private Double exponentialBackoffMultiplier;

        /**
         * exponential max interval value
         */
        private Duration exponentialMaxWaitDurationInOpenState;

        /**
         * flag to enable randomized delay  policy or not for retry policy delay
         */
        @Nullable
        private Boolean enableRandomizedWait;

        /**
         * randomized delay factor value
         */
        private Double randomizedWaitFactor;

        /**
         * Returns the failure rate threshold for the circuit breaker as percentage.
         *
         * @return the failure rate threshold
         */
        @Nullable
        public Float getFailureRateThreshold() {
            return failureRateThreshold;
        }

        /**
         * Sets the failure rate threshold for the circuit breaker as percentage.
         *
         * @param failureRateThreshold the failure rate threshold
         */
        public InstanceProperties setFailureRateThreshold(Float failureRateThreshold) {
            Objects.requireNonNull(failureRateThreshold);
            if (failureRateThreshold < 1 || failureRateThreshold > 100) {
                throw new IllegalArgumentException(
                        "failureRateThreshold must be between 1 and 100.");
            }

            this.failureRateThreshold = failureRateThreshold;
            return this;
        }

        /**
         * Returns the wait duration the CircuitBreaker will stay open, before it switches to half
         * closed.
         *
         * @return the wait duration
         */
        @Nullable
        public Duration getWaitDurationInOpenState() {
            return waitDurationInOpenState;
        }

        /**
         * Sets the wait duration the CircuitBreaker should stay open, before it switches to half
         * closed.
         *
         * @param waitDurationInOpenStateMillis the wait duration
         */
        public InstanceProperties setWaitDurationInOpenState(
                Duration waitDurationInOpenStateMillis) {
            Objects.requireNonNull(waitDurationInOpenStateMillis);
            if (waitDurationInOpenStateMillis.toMillis() < 1) {
                throw new IllegalArgumentException(
                        "waitDurationInOpenStateMillis must be greater than or equal to 1 millis.");
            }

            this.waitDurationInOpenState = waitDurationInOpenStateMillis;
            return this;
        }

        /**
         * Returns the ring buffer size for the circuit breaker while in closed state.
         *
         * @return the ring buffer size
         */
        @Nullable
        public Integer getRingBufferSizeInClosedState() {
            return ringBufferSizeInClosedState;
        }

        /**
         * Sets the ring buffer size for the circuit breaker while in closed state.
         *
         * @param ringBufferSizeInClosedState the ring buffer size
         * @deprecated Use {@link #setSlidingWindowSize(Integer)} instead.
         */
        @Deprecated
        public InstanceProperties setRingBufferSizeInClosedState(
                Integer ringBufferSizeInClosedState) {
            Objects.requireNonNull(ringBufferSizeInClosedState);
            if (ringBufferSizeInClosedState < 1) {
                throw new IllegalArgumentException(
                        "ringBufferSizeInClosedState must be greater than or equal to 1.");
            }

            this.ringBufferSizeInClosedState = ringBufferSizeInClosedState;
            return this;
        }

        /**
         * Returns the ring buffer size for the circuit breaker while in half open state.
         *
         * @return the ring buffer size
         */
        @Nullable
        public Integer getRingBufferSizeInHalfOpenState() {
            return ringBufferSizeInHalfOpenState;
        }

        /**
         * Sets the ring buffer size for the circuit breaker while in half open state.
         *
         * @param ringBufferSizeInHalfOpenState the ring buffer size
         * @deprecated Use {@link #setPermittedNumberOfCallsInHalfOpenState(Integer)} instead.
         */
        @Deprecated
        public InstanceProperties setRingBufferSizeInHalfOpenState(
                Integer ringBufferSizeInHalfOpenState) {
            Objects.requireNonNull(ringBufferSizeInHalfOpenState);
            if (ringBufferSizeInHalfOpenState < 1) {
                throw new IllegalArgumentException(
                        "ringBufferSizeInHalfOpenState must be greater than or equal to 1.");
            }

            this.ringBufferSizeInHalfOpenState = ringBufferSizeInHalfOpenState;
            return this;
        }

        /**
         * Returns if we should automatically transition to half open after the timer has run out.
         *
         * @return setAutomaticTransitionFromOpenToHalfOpenEnabled if we should automatically go to
         * half open or not
         */
        public Boolean getAutomaticTransitionFromOpenToHalfOpenEnabled() {
            return this.automaticTransitionFromOpenToHalfOpenEnabled;
        }

        /**
         * Sets if we should automatically transition to half open after the timer has run out.
         *
         * @param automaticTransitionFromOpenToHalfOpenEnabled The flag for automatic transition to
         *                                                     half open after the timer has run
         *                                                     out.
         */
        public InstanceProperties setAutomaticTransitionFromOpenToHalfOpenEnabled(
                Boolean automaticTransitionFromOpenToHalfOpenEnabled) {
            this.automaticTransitionFromOpenToHalfOpenEnabled = automaticTransitionFromOpenToHalfOpenEnabled;
            return this;
        }

        /**
         * Returns if we should enable writable stack traces or not.
         *
         * @return writableStackTraceEnabled if we should enable writable stack traces or not.
         */
        @Nullable
        public Boolean getWritableStackTraceEnabled() {
            return this.writableStackTraceEnabled;
        }

        /**
         * Sets if we should enable writable stack traces or not.
         *
         * @param writableStackTraceEnabled The flag to enable writable stack traces.
         */
        public InstanceProperties setWritableStackTraceEnabled(Boolean writableStackTraceEnabled) {
            this.writableStackTraceEnabled = writableStackTraceEnabled;
            return this;
        }

        @Nullable
        public Integer getEventConsumerBufferSize() {
            return eventConsumerBufferSize;
        }

        public InstanceProperties setEventConsumerBufferSize(Integer eventConsumerBufferSize) {
            Objects.requireNonNull(eventConsumerBufferSize);
            if (eventConsumerBufferSize < 1) {
                throw new IllegalArgumentException(
                        "eventConsumerBufferSize must be greater than or equal to 1.");
            }

            this.eventConsumerBufferSize = eventConsumerBufferSize;
            return this;
        }

        /**
         * @return the flag that controls if health indicators are allowed to go into a failed
         * (DOWN) status.
         * @see #setAllowHealthIndicatorToFail(Boolean)
         */
        @Nullable
        public Boolean getAllowHealthIndicatorToFail() {
            return allowHealthIndicatorToFail;
        }

        /**
         * When set to true, it allows the health indicator to go to a failed (DOWN) status. By
         * default, health indicators for circuit breakers will never go into an unhealthy state.
         *
         * @param allowHealthIndicatorToFail flag to control if the health indicator is allowed to
         *                                   fail
         * @return the InstanceProperties
         */
        public InstanceProperties setAllowHealthIndicatorToFail(
                Boolean allowHealthIndicatorToFail) {
            this.allowHealthIndicatorToFail = allowHealthIndicatorToFail;
            return this;
        }

        @Nullable
        public Boolean getRegisterHealthIndicator() {
            return registerHealthIndicator;
        }

        public InstanceProperties setRegisterHealthIndicator(Boolean registerHealthIndicator) {
            this.registerHealthIndicator = registerHealthIndicator;
            return this;
        }

        @Nullable
        public Class<Predicate<Throwable>> getRecordFailurePredicate() {
            return recordFailurePredicate;
        }

        public InstanceProperties setRecordFailurePredicate(
                Class<Predicate<Throwable>> recordFailurePredicate) {
            this.recordFailurePredicate = recordFailurePredicate;
            return this;
        }

        @Nullable
        public Class<? extends Throwable>[] getRecordExceptions() {
            return recordExceptions;
        }

        public InstanceProperties setRecordExceptions(
                Class<? extends Throwable>[] recordExceptions) {
            this.recordExceptions = recordExceptions;
            return this;
        }

        @Nullable
        public Class<? extends Throwable>[] getIgnoreExceptions() {
            return ignoreExceptions;
        }

        public InstanceProperties setIgnoreExceptions(
                Class<? extends Throwable>[] ignoreExceptions) {
            this.ignoreExceptions = ignoreExceptions;
            return this;
        }

        /**
         * Gets the shared configuration name. If this is set, the configuration builder will use
         * the the shared configuration backend over this one.
         *
         * @return The shared configuration name.
         */
        @Nullable
        public String getBaseConfig() {
            return baseConfig;
        }

        /**
         * Sets the shared configuration name. If this is set, the configuration builder will use
         * the the shared configuration backend over this one.
         *
         * @param baseConfig The shared configuration name.
         */
        public InstanceProperties setBaseConfig(String baseConfig) {
            this.baseConfig = baseConfig;
            return this;
        }

        @Nullable
        public Integer getPermittedNumberOfCallsInHalfOpenState() {
            return permittedNumberOfCallsInHalfOpenState;
        }

        public InstanceProperties setPermittedNumberOfCallsInHalfOpenState(
                Integer permittedNumberOfCallsInHalfOpenState) {
            Objects.requireNonNull(permittedNumberOfCallsInHalfOpenState);
            if (permittedNumberOfCallsInHalfOpenState < 1) {
                throw new IllegalArgumentException(
                        "permittedNumberOfCallsInHalfOpenState must be greater than or equal to 1.");
            }

            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
            return this;
        }

        @Nullable
        public Integer getMinimumNumberOfCalls() {
            return minimumNumberOfCalls;
        }

        public InstanceProperties setMinimumNumberOfCalls(Integer minimumNumberOfCalls) {
            Objects.requireNonNull(minimumNumberOfCalls);
            if (minimumNumberOfCalls < 1) {
                throw new IllegalArgumentException(
                        "minimumNumberOfCalls must be greater than or equal to 1.");
            }

            this.minimumNumberOfCalls = minimumNumberOfCalls;
            return this;
        }

        @Nullable
        public Integer getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public InstanceProperties setSlidingWindowSize(Integer slidingWindowSize) {
            Objects.requireNonNull(slidingWindowSize);
            if (slidingWindowSize < 1) {
                throw new IllegalArgumentException(
                        "slidingWindowSize must be greater than or equal to 1.");
            }

            this.slidingWindowSize = slidingWindowSize;
            return this;
        }

        @Nullable
        public Float getSlowCallRateThreshold() {
            return slowCallRateThreshold;
        }

        public InstanceProperties setSlowCallRateThreshold(Float slowCallRateThreshold) {
            Objects.requireNonNull(slowCallRateThreshold);
            if (slowCallRateThreshold < 1 || slowCallRateThreshold > 100) {
                throw new IllegalArgumentException(
                        "slowCallRateThreshold must be between 1 and 100.");
            }

            this.slowCallRateThreshold = slowCallRateThreshold;
            return this;
        }

        @Nullable
        public Duration getSlowCallDurationThreshold() {
            return slowCallDurationThreshold;
        }

        @Nullable
        public Duration getMaxWaitDurationInHalfOpenState() {
            return maxWaitDurationInHalfOpenState;
        }

        public InstanceProperties setSlowCallDurationThreshold(Duration slowCallDurationThreshold) {
            Objects.requireNonNull(slowCallDurationThreshold);
            if (slowCallDurationThreshold.toNanos() < 1) {
                throw new IllegalArgumentException(
                        "waitDurationInOpenStateMillis must be greater than or equal to 1 nanos.");
            }

            this.slowCallDurationThreshold = slowCallDurationThreshold;
            return this;
        }

        public InstanceProperties setMaxWaitDurationInHalfOpenState(Duration maxWaitDurationInHalfOpenState) {
            Objects.requireNonNull(maxWaitDurationInHalfOpenState);
            if (maxWaitDurationInHalfOpenState.toMillis() < 1) {
                throw new IllegalArgumentException(
                        "maxWaitDurationInHalfOpenState must be greater than or equal to 1 ms.");
            }

            this.maxWaitDurationInHalfOpenState = maxWaitDurationInHalfOpenState;
            return this;
        }

        @Nullable
        public CircuitBreakerConfig.SlidingWindowType getSlidingWindowType() {
            return slidingWindowType;
        }

        public InstanceProperties setSlidingWindowType(CircuitBreakerConfig.SlidingWindowType slidingWindowType) {
            this.slidingWindowType = slidingWindowType;
            return this;
        }


        public Boolean getEnableExponentialBackoff() {
            return enableExponentialBackoff;
        }

        public InstanceProperties setEnableExponentialBackoff(Boolean enableExponentialBackoff) {
            this.enableExponentialBackoff = enableExponentialBackoff;
            return this;
        }

        @Nullable
        public Double getExponentialBackoffMultiplier() {
            return exponentialBackoffMultiplier;
        }

        public InstanceProperties setExponentialBackoffMultiplier(
                Double exponentialBackoffMultiplier) {
            this.exponentialBackoffMultiplier = exponentialBackoffMultiplier;
            return this;
        }

        @Nullable
        public Duration getExponentialMaxWaitDurationInOpenState() {
            return exponentialMaxWaitDurationInOpenState;
        }

        public InstanceProperties setExponentialMaxWaitDurationInOpenState(
                Duration exponentialMaxWaitDurationInOpenState) {
            this.exponentialMaxWaitDurationInOpenState = exponentialMaxWaitDurationInOpenState;
            return this;
        }

        @Nullable
        public Boolean getEnableRandomizedWait() {
            return enableRandomizedWait;
        }

        public InstanceProperties setEnableRandomizedWait(Boolean enableRandomizedWait) {
            this.enableRandomizedWait = enableRandomizedWait;
            return this;
        }

        @Nullable
        public Double getRandomizedWaitFactor() {
            return randomizedWaitFactor;
        }

        public InstanceProperties setRandomizedWaitFactor(Double randomizedWaitFactor) {
            this.randomizedWaitFactor = randomizedWaitFactor;
            return this;
        }
    }

}
