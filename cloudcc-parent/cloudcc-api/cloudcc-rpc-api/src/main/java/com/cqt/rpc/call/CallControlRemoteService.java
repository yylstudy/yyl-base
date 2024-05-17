package com.cqt.rpc.call;

import com.cqt.model.client.base.ClientBase;

/**
 * @author linshiqiang
 * date:  2023-06-29 10:15
 * 呼叫控制服务 rpc接口
 */
public interface CallControlRemoteService {

    /**
     * 前端SDK -> ws - > call-control
     *
     * @param requestBody json参数
     * @return SdkResponseBaseVO 异步返回
     */
    ClientBase request(String requestBody) throws Exception;

}
