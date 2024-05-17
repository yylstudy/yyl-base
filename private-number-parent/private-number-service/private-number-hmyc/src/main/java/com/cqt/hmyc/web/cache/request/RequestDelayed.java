package com.cqt.hmyc.web.cache.request;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author linshiqiang
 * @date 2021/11/16 9:52
 * 重复requestId 延时队列
 */
@Slf4j
public class RequestDelayed implements Delayed, Runnable {

    /**
     * 触发时间
     */
    private final long time;

    public final String key;

    public final String threadName;

    public RequestDelayed(long time, TimeUnit unit, String key, String threadName) {
        this.threadName = threadName;
        this.time = System.currentTimeMillis() + (time > 0 ? unit.toMillis(time) : 0);
        this.key = key;
    }

    @Override
    public void run() {

    }

    @Override
    public long getDelay(TimeUnit unit) {
        return time - System.currentTimeMillis();
    }

    @Override
    public int compareTo(Delayed o) {
        RequestDelayed requestDelayed = (RequestDelayed) o;
        long diff = this.time - requestDelayed.time;
        if (diff >= 0) {
            return 1;
        } else {
            return -1;
        }
    }
}
