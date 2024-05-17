package com.cqt.monitor.web.distributor.model.vo;

import com.cqt.model.sipconfig.Distributor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @since 2022-12-02 10:52
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDisConfigVO {

    /**
     * SBC IP
     */
    private String serverIp;

    /**
     * 是否修改成功
     */
    private Boolean success;

    /**
     * 是否告警
     */
    private Boolean alarm;

    /**
     * 结果消息
     */
    private String message;

    /**
     * dis组配置 对象
     */
    private Distributor distributor;

    /**
     * 告警消息是否合并
     */
    private Boolean mergeAlarm;

    public static UpdateDisConfigVO setVO(String serverIp, Boolean success, Boolean alarm, String message, Distributor distributor, Boolean mergeAlarm) {

        return UpdateDisConfigVO.builder()
                .serverIp(serverIp)
                .success(success)
                .alarm(alarm)
                .message(message)
                .distributor(distributor)
                .mergeAlarm(mergeAlarm)
                .build();
    }
}
