package com.cqt.model.cdr.dto;

import com.cqt.model.cdr.entity.CallCenterMainCdr;
import com.cqt.model.cdr.entity.CallCenterSubCdr;
import com.cqt.model.cdr.entity.CdrChanneldata;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * date:  2023-07-26 10:39
 * 话单入库发送mq消息体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class CdrMessageDTO implements Serializable {

    private static final long serialVersionUID = 8833530864417071776L;

    /**
     * 是否为留言
     */
    private Boolean voiceMailFlag;

    /**
     * 呼入转IVR, 没有分配坐席
     */
    private Boolean callInIvrNoAgent;

    /**
     * fs服务器标识
     */
    private String serviceId;

    /**
     * 主话单实体
     */
    private CallCenterMainCdr mainCdr;

    /**
     * 子话单实体
     */
    private List<CallCenterSubCdr> subCdr;

    /**
     * 通道变量
     */
    private Map<String, Object> channelData;

    /**
     * 通道变量
     */
    private CdrChanneldata cdrChanneldata;

    public static CdrMessageDTO getCdrMessageDTO(String message, ObjectMapper objectMapper, String logId) {
        CdrMessageDTO cdrMessageDTO = null;
        try {
            cdrMessageDTO = objectMapper.readValue(message, CdrMessageDTO.class);
            CdrChanneldata cdrChanneldata = objectMapper.convertValue(cdrMessageDTO.getChannelData(), CdrChanneldata.class);
            cdrMessageDTO.setCdrChanneldata(cdrChanneldata);
            log.info(logId + "json解析成对象成功，cdrMessageDTO:" + cdrMessageDTO);
        } catch (Exception e) {
            log.error(logId + "消息：{}，处理异常", message, e);
        }
        return cdrMessageDTO;
    }
}
