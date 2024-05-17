package com.cqt.hmbc.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.cqt.cloud.api.msrn.LocationUpdatingFeignClient;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.MapLocationUpdatingStatusEnum;
import com.cqt.model.common.ResultT;
import com.cqt.model.hmbc.dto.LocationUpdatingReq;
import com.cqt.model.hmbc.dto.LocationUpdatingRsp;
import com.cqt.model.hmbc.properties.HmbcProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 位置更新接口
 * 管理与iccp-msrn的接口交互
 *
 * @author Xienx
 * @date 2022-05-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationUpdatingService implements SystemConstant {

    private final HmbcProperties hmbcProperties;
    private final LocationUpdatingFeignClient locationUpdatingApi;

    /**
     * 同步方法
     * excel批量位置更新
     *
     * @return 位置更新的结果
     */
    public List<LocationUpdatingRsp> locationUpdatingSync4Job(List<LocationUpdatingReq> reqInfos) {
        // 位置更新结果
        List<LocationUpdatingRsp> result = new ArrayList<>();

        long startMill = System.currentTimeMillis();
        try {
            // 每次请求最大数量限制100条, 所以这里进行分割
            List<List<LocationUpdatingReq>> partition = CollectionUtil.split(reqInfos, hmbcProperties.getLocationUpdating().getPerBatchLimit());

            // 分批次进行位置更新请求
            for (List<LocationUpdatingReq> list : partition) {
                result.addAll(locationUpdateJob(list));
            }
            return result;
        } finally {
            log.info("[批量位置更新], 共更新{}个号码, 累计耗时:{}ms", reqInfos.size(), System.currentTimeMillis() - startMill);
        }
    }


    private List<LocationUpdatingRsp> locationUpdateJob(List<LocationUpdatingReq> list) {
        long startMill = System.currentTimeMillis();
        MDC.put(REQUEST_ID, IdUtil.objectId());
        try {
            // feign调用iccp-msrn服务位置更新接口进行位置更新
            ResultT<List<LocationUpdatingRsp>> tmpResult = locationUpdatingApi.locationUpdatingBatchSync(list);
            log.info("[{}] -> 本次位置更新结果: {}", MDC.get(REQUEST_ID), tmpResult);
            // 判断返回结果是否成功
            if (HttpStatus.SC_OK != tmpResult.getCode()) {
                // 如果这次请求响应失败了, 则这次请求更新的号码全都失败
                return list.stream()
                        .map(this::errorRsp)
                        .collect(Collectors.toList());
            }
            return tmpResult.getResult();
        } finally {
            log.info("[{}] -> 本次位置更新执行耗时: {} ms", MDC.get(REQUEST_ID), System.currentTimeMillis() - startMill);
            MDC.clear();
        }
    }

    private LocationUpdatingRsp errorRsp(LocationUpdatingReq locationUpdatingReq) {
        LocationUpdatingRsp locationUpdatingRsp = new LocationUpdatingRsp();
        locationUpdatingRsp.setNumber(locationUpdatingReq.getNumber());
        locationUpdatingRsp.setStatus(MapLocationUpdatingStatusEnum.UNKNOWN_EXCEPTION.getCode());
        locationUpdatingRsp.setErrorReason("请求iccp-msrn服务异常");

        return locationUpdatingRsp;
    }
}
