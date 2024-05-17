package com.cqt.model.freeswitch.vo;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LuaWriteVO implements Serializable {
    private static final long serialVersionUID = -6842057420803459739L;

    @JsonProperty("req_id")
    private String reqId;

    @JsonProperty("result")
    private Boolean result;

    @JsonProperty("msg")
    private String msg;

    @JsonProperty("real_file_path")
    private String realFilePath;
}
