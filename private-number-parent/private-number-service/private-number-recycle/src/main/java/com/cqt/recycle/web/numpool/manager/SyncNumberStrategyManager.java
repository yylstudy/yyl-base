package com.cqt.recycle.web.numpool.manager;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.future.AsyncTask;
import com.cqt.model.common.Result;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.numpool.dto.NumberChangeSyncDTO;
import com.cqt.model.numpool.dto.SyncRemoteDTO;
import com.cqt.model.numpool.vo.SyncResultVO;
import com.cqt.recycle.web.corpinfo.service.RequestRemoteTaskLoaderImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @since 2022/5/26 11:16
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SyncNumberStrategyManager {

    private final Map<String, SyncNumberStrategy> STRATEGY_MAP = new ConcurrentHashMap<>();

    private final List<SyncNumberStrategy> syncNumberStrategyList;

    private final HideProperties hideProperties;

    private final AsyncTask<SyncResultVO, SyncRemoteDTO> asyncTask;

    private final ThreadPoolTaskExecutor recycleExecutor;

    @PostConstruct
    public void initStrategy() {
        for (SyncNumberStrategy strategy : syncNumberStrategyList) {
            STRATEGY_MAP.put(strategy.getBusinessType(), strategy);
        }
    }

    public Result sync(NumberChangeSyncDTO numberChangeSyncDTO) {
        if (log.isInfoEnabled()) {
            log.info("同步号码池信息: {}", JSON.toJSONString(numberChangeSyncDTO));
        }
        // for调用所有服务器的 hmyc服务 的sync接口
        List<String> serverIps = hideProperties.getServerIps();
        String syncUrl = hideProperties.getSyncNumberUrl();

        SyncNumberStrategy strategy = STRATEGY_MAP.get(numberChangeSyncDTO.getBusinessType());
        Optional<SyncNumberStrategy> strategyOptional = Optional.ofNullable(strategy);
        if (strategyOptional.isPresent()) {
            // 同步redis
            try {
                strategy.sync(numberChangeSyncDTO);
            } catch (Exception e) {
                log.info("同步redis号码池失败, 可能vccId: {}, 未分配业务模式, 绑定关系表未创建, 异常: ", numberChangeSyncDTO.getVccId(), e);
                return Result.fail(500, String.format("同步redis号码池失败, 可能vccId: %s, 未分配业务模式, 请先分配!",
                        numberChangeSyncDTO.getVccId()));
            }
        }

        // 调用hmyc服务接口
        List<SyncResultVO> list = remoteSync(serverIps, syncUrl, numberChangeSyncDTO);

        // 判断是否有失败的
        List<SyncResultVO> failList = list.stream().filter(item -> !item.getSuccess()).collect(Collectors.toList());
        Result result = Result.ok(list);
        if (CollUtil.isNotEmpty(failList)) {
            result.setCode(-1);
            result.setMessage("存在调用失败的服务!");
        }
        return result;
    }

    /**
     * 同步hmyc服务
     */
    public List<SyncResultVO> remoteSync(List<String> serverIps, String syncUrl, NumberChangeSyncDTO numberChangeSyncDTO) {

        List<SyncRemoteDTO> syncRemoteDtoList = serverIps.stream().map(ip -> SyncRemoteDTO.builder()
                .url(String.format(syncUrl, ip))
                .ip(ip)
                .requestBody(JSON.toJSONString(numberChangeSyncDTO))
                .build()).collect(Collectors.toList());

        RequestRemoteTaskLoaderImpl requestRemoteTaskLoader = new RequestRemoteTaskLoaderImpl();

        return asyncTask.sendAsyncBatch(syncRemoteDtoList, requestRemoteTaskLoader, recycleExecutor);
    }

}
