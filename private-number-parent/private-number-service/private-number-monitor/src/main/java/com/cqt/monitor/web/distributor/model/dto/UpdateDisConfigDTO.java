package com.cqt.monitor.web.distributor.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author linshiqiang
 * @since 2022-12-02 10:32
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDisConfigDTO {

    private String serverIp;

    private List<String> nodeNameList;

    private String distributorConfigPath;

    private String disListName;

    /**
     * 修改权重值
     */
    private String weight;

    public static UpdateDisConfigDTO init(String serverIp, List<String> nodeNameList, String distributorConfigPath,
                                          String disListName, String weight) {

        return UpdateDisConfigDTO.builder()
                .serverIp(serverIp)
                .nodeNameList(nodeNameList)
                .distributorConfigPath(distributorConfigPath)
                .disListName(disListName)
                .weight(weight)
                .build();
    }
}
