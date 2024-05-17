package com.repush.dao.domain;

import lombok.Data;

@Data
public class LostMsg {
    private String id;
    private String caller;
    private String imsi;
    private String msgId;
    private String content;
    private String totalNum;
    private String lostNum;

}
