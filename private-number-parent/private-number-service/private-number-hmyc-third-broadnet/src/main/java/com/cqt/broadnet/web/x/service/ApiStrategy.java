package com.cqt.broadnet.web.x.service;

import com.cqt.broadnet.common.model.x.vo.CallControlResponseVO;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author linshiqiang
 * date:  2023-02-15 14:00
 * 接口请求策略
 */
public interface ApiStrategy {

    /**
     * 接口方法名称
     *
     * @return 接口方法名称
     */
    String getMethod();

    /**
     * 执行方法
     *
     * @param jsonStr json字符串
     * @return 结果
     * @throws JsonProcessingException json异常
     */
    CallControlResponseVO execute(String jsonStr) throws JsonProcessingException;
}
