package com.cqt.call.strategy.client;

import com.cqt.base.enums.MsgTypeEnum;
import com.cqt.model.client.base.ClientResponseBaseVO;

/**
 * @author linshiqiang
 * date:  2023-07-03 14:38
 * 前端客户端请求话务操作策略接口
 */
public interface ClientRequestStrategy {

    MsgTypeEnum getMsgType();

    ClientResponseBaseVO deal(String requestBody) throws Exception;
}
