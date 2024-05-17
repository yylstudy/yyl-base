package com.cqt.push.service;

import com.cqt.model.common.Result;
import com.cqt.model.hmbc.vo.HmbcResult;

import javax.servlet.http.HttpServletResponse;

/**
 * 拨测结果接收Service
 *
 * @author Xienx
 * @date 2023年02月08日 15:08
 */
public interface DialTestResultService {

    /**
     * 号码拨测结果处理
     *
     * @param hmbcResult 拨测结果
     * @param response   HttpServletResponse
     */
    Result hmbcResult(HmbcResult hmbcResult, HttpServletResponse response);
}
