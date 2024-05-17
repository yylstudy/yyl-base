package com.cqt.model.numpool.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @date 2022/8/29 17:39
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncRemoteDTO {

    /**
     * 目标同步的ip
     */
    private String url;

    /**
     * 目标同步的ip
     */
    private String ip;

    /**
     * 请求体json字符串
     */
    private String requestBody;

}
