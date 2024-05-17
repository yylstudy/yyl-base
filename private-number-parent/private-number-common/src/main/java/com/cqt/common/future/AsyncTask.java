package com.cqt.common.future;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @since 2022/8/29 17:13
 * 异步任务
 */
@Component
@Slf4j
public class AsyncTask<R, P> {

    public List<R> sendAsyncBatch(List<P> list, TaskLoader<R, P> loader, Executor executor) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<CompletableFuture<R>> futureList = list.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return loader.load(item);
                    } catch (Exception e) {
                        log.error("item: {}, loader task error: ", item, e);
                    }
                    return null;
                }, executor)).collect(Collectors.toList());

        return futureList.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public List<R> sendAsyncBatch(List<P> list, TaskLoader<R, P> loader) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<CompletableFuture<R>> futureList = list.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return loader.load(item);
                    } catch (Exception e) {
                        log.error("item: {}, loader task error: ", item, e);
                    }
                    return null;
                })).collect(Collectors.toList());

        return futureList.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("rawtypes")
    public List<R> sendAsyncBatch(List<P> list, Integer batchCount, Executor executor, TaskLoader<R, P> loader) {

        List<R> resultList = new CopyOnWriteArrayList<>();
        if (CollUtil.isNotEmpty(list)) {
            // 将任务拆分分成每50个为一个任务
            CollUtil.split(list, batchCount)
                    .forEach(tempList -> {
                        CompletableFuture[] completableFutures = tempList.stream()
                                .map(p -> CompletableFuture.supplyAsync(() -> {
                                                    try {
                                                        return loader.load(p);
                                                    } catch (InterruptedException e) {
                                                        e.printStackTrace();
                                                    }
                                                    return null;
                                                }, executor)
                                                .handle((result, throwable) -> {
                                                    if (Objects.nonNull(throwable)) {
                                                        log.error("async error:{}", throwable.getMessage());
                                                    } else if (Objects.nonNull(result)) {
                                                        log.info("async success:{}", result);
                                                    } else {
                                                        log.error("async result is null");
                                                    }
                                                    return result;
                                                }).whenComplete((r, ex) -> {
                                                    if (Objects.nonNull(r)) {
                                                        resultList.add(r);
                                                    }
                                                })
                                ).toArray(CompletableFuture[]::new);
                        CompletableFuture.allOf(completableFutures).join();
                        log.info("result count: {}", resultList.size());
                    });
        }
        return resultList;
    }

}
