package com.cqt.model.corpinfo.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author linshiqiang
 * @date 2022/5/25 9:54
 */
@Data
public class ExpireTimeDTO {

    private Date expireStartTime;

    private Date expireEndTime;

    private String vccName;

}
