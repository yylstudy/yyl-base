package com.cqt.model.freeswitch.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class PlayRecordDTO implements Serializable {
    private static final long serialVersionUID = -1804528466643980117L;

    @JsonProperty("req_id")
    private String reqId;

    @JsonProperty("company_code")
    private String companyCode;

    @JsonProperty("service_code")
    private String serviceCode;

    @JsonProperty("file_path")
    private String filePath;

    public PlayRecordDTO(String companyCode, String filePath) {
        this.reqId = UUID.randomUUID().toString();
        this.companyCode = companyCode;
        this.filePath = filePath;
        this.serviceCode = "cloudcc";
    }
}
