package com.cqt.monitor.web.distributor.model.dto;

import lombok.Data;

/**
 * @author linshiqiang
 * @since 2022-12-02 10:32
 */
@Data
public class GetDisConfigDTO {

    private String serverIp;

    private String distributorConfigPath;

    public GetDisConfigDTO(String serverIp, String distributorConfigPath) {
        this.serverIp = serverIp;
        this.distributorConfigPath = distributorConfigPath;
    }

    public static GetDisConfigDTO init(String serverIp, String distributorConfigPath) {

        return new GetDisConfigDTO(serverIp, distributorConfigPath);
    }
}
