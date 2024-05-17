package com.cqt.recycle.web.corpinfo.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.cqt.common.future.TaskLoader;
import com.cqt.model.numpool.dto.SyncRemoteDTO;
import com.cqt.model.numpool.vo.SyncResultVO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author linshiqiang
 * @since 2022/8/29 17:26
 * 业务配置同步服务器处理任务
 */
@Slf4j
public class RequestRemoteTaskLoaderImpl implements TaskLoader<SyncResultVO, SyncRemoteDTO> {

    @Override
    public SyncResultVO load(SyncRemoteDTO syncRemoteDTO) {
        String ip = syncRemoteDTO.getIp();
        SyncResultVO resultVO = SyncResultVO.builder()
                .ip(ip)
                .success(true)
                .build();
        HttpResponse httpResponse;
        try {
            httpResponse = HttpRequest.post(syncRemoteDTO.getUrl())
                    .body(syncRemoteDTO.getRequestBody())
                    .timeout(10000)
                    .executeAsync();
            log.info("同步hmyc内存: {}, {}", ip, httpResponse.body());
        } catch (Exception e) {
            log.error("请求同步接口失败, ip: {}", ip, e);
            resultVO.setMessage("请求同步接口失败: " + e.getMessage());
            resultVO.setSuccess(false);
            return resultVO;
        }
        if (httpResponse.isOk()) {
            return resultVO;
        }
        resultVO.setMessage("接口返回状态码为: " + httpResponse.getStatus());
        resultVO.setSuccess(false);
        return resultVO;
    }
}
