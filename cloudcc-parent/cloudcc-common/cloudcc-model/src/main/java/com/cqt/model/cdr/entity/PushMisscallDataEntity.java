package com.cqt.model.cdr.entity;

import lombok.Data;

@Data
public class PushMisscallDataEntity {
    private Long id;

    private String systemcode;

    private String phone;

    private String agentid;

    private String calltime;

    private Integer reqcount;

    private String vccid;
}