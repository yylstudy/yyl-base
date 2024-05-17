package com.cqt.queue.calltask.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cqt.model.calltask.dto.CallableTimeDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Date;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-12-05 10:17
 */
public abstract class AbstractOutCallTask {

    /**
     * 所有外显号码
     */
    public List<String> getDisplayNumberList(String displayNumber) throws JsonProcessingException {
        ObjectMapper objectMapper = getObjectMapper();
        return objectMapper.readValue(displayNumber, new TypeReference<List<String>>() {
        });
    }

    private ObjectMapper getObjectMapper() {
        return SpringUtil.getBean(ObjectMapper.class);
    }

    /**
     * 检查呼出时段
     *
     * @param callableTime 呼出时段 [{"beginTime":"00:00:00","endTime":"23:00:00" }]
     * @return 是否可呼出
     * @throws JsonProcessingException json转化异常
     */
    public boolean checkCallable(String callableTime) throws JsonProcessingException {
        if (StrUtil.isEmpty(callableTime)) {
            return false;
        }
        ObjectMapper objectMapper = getObjectMapper();
        List<CallableTimeDTO> callableTimeList = objectMapper.readValue(callableTime, new TypeReference<List<CallableTimeDTO>>() {
        });
        DateTime dateTime = DateUtil.date();
        for (CallableTimeDTO callableTimeDTO : callableTimeList) {
            String startTime = callableTimeDTO.getStartTime();
            String endTime = callableTimeDTO.getEndTime();
            DateTime begin = DateUtil.parse(DateUtil.formatDate(dateTime) + StrUtil.SPACE + startTime);
            DateTime end = DateUtil.parse(DateUtil.formatDate(dateTime) + StrUtil.SPACE + endTime);
            boolean in = DateUtil.isIn(DateUtil.date(), begin, end);
            if (in) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断任务开始结束时间
     *
     * @return 是否允许外呼
     */
    public boolean isInPermitTime(Date startTime, Date endTime) {
        return DateUtil.isIn(DateUtil.date(), startTime, endTime);
    }
}
