package com.cqt.sms.model.dto;

import lombok.Data;

@Data
public class MatchRequest {

    String vccid;
    String msg;
    String xnumber;
}
