package com.cqt.model.cdr.dto;

import com.cqt.model.cdr.entity.CdrDatapushPushEntity;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemoteCdrDTO implements Serializable {
    private static final long serialVersionUID = -1629274644447710954L;
    @JsonProperty("synTime")
    private Date synTime;
    private String requestId;
    @JsonProperty("data")
    private List<CdrDatapushPushEntity> data;
    private String token;
}
