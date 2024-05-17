package com.cqt.rpc.client;

import com.cqt.model.client.base.ClientResponseBaseVO;

/**
 * @author linshiqiang
 * date:  2023-06-29 10:04
 */
public interface ClientServerRemoteService {

    /**
     * sdk-interface / call-control -> ws - > 前端SDK
     *
     * @param requestBody SDK 传入的参数json
     * @return SdkResponseBaseVO
     */
    ClientResponseBaseVO request(String requestBody);
}
