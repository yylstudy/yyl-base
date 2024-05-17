package com.cqt.monitor.web.distributor.model.dto;

import com.cqt.model.sipconfig.Distributor;
import lombok.Data;

/**
 * @author linshiqiang
 * @since 2022-12-05 9:47
 */
@Data
public class UpdateDistributorDbDTO {

    /**
     * SBC IP 一台
     */
    private String serverIp;

    /**
     * dis组 list名称
     */
    private String disListName;

    /**
     * dis组配置 对象
     */
    private Distributor distributor;
}
