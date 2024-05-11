package com.linkcircle.basecom.config;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/5 10:14
 */

public class ThreadPool {
    private static ThreadPoolExecutor pool = new ThreadPoolExecutor(
            4,
            4,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200000),
            r->new Thread(r, "system-task-thread" + r.hashCode()));
    public static void execute(Runnable runnable){
        pool.execute(runnable);
    }
}
