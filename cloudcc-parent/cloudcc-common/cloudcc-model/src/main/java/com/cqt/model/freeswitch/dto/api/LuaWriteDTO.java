package com.cqt.model.freeswitch.dto.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LuaWriteDTO implements Serializable {
    private static final long serialVersionUID = -6842057420803459739L;

    @JsonProperty("req_id")
    private String reqId = UUID.randomUUID().toString();

    @JsonProperty("company_code")
    private String companyCode;

    @JsonProperty("service_code")
    private String serviceCode = "cloudcc";

    @JsonProperty("filename")
    private String fileName;

    @JsonProperty("content")
    private String content;
}
