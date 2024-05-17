package com.cqt.model.hmbc.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommandEntity {
    private String oneMsg;
    private String twoMsg;
    private String threeMsg;
    private String fourMsg;
    private String fiveMsg;
    private String sxiMsg;
    private String sevenMsg;
    private String eightMsg;
    private String nineMsg;
    private String tenMsg;
    private String eleven;
}
