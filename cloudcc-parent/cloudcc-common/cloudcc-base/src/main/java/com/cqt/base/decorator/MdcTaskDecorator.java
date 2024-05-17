package com.cqt.base.decorator;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * @author linshiqiang
 * @since 2022-11-10 15:11
 */
public class MdcTaskDecorator implements TaskDecorator {

    @NonNull
    @Override
    public Runnable decorate(@NonNull Runnable runnable) {

        return new MdcContinueRunnableDecorator(runnable);
    }

    /**
     * 执行线程装饰器
     */
    protected static class MdcContinueRunnableDecorator implements Runnable {

        final Map<String, String> logContextMap;
        
        private final Runnable delegate;

        MdcContinueRunnableDecorator(Runnable runnable) {
            delegate = runnable;
            logContextMap = MDC.getCopyOfContextMap();
        }

        @Override
        public void run() {
            MDC.setContextMap(logContextMap);
            try {
                delegate.run();
            } finally {
                MDC.clear();
            }
        }
    }
}
