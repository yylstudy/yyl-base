package com.cqt.common.future;

/**
 * @author linshiqiang
 * @since 2022/8/29 17:12
 * 具体业务逻辑实现接口
 */
@FunctionalInterface
public interface TaskLoader<R, P> {

    /**
     * 任务处理
     *
     * @param p 任务参数
     * @return 任务处理结果
     * @throws InterruptedException 异常
     */
    R load(P p) throws InterruptedException;
}
