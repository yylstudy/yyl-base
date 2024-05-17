package com.cqt.broadnet.common.model.axb.dto;

import lombok.Data;

/**
 * @author huweizhong
 * date  2023/5/29 9:17
 */
@Data
public class AxbCallRecordDTO {

    private String bindId;

    private String callId;

    private Integer cityId;

    private String recordUrl;

    private String recordMode;
}
