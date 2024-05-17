package com.cqt.model.hmbc.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

/**
 * 单个位置更新的请求参数
 *
 * @author scott
 * @date 2022年05月24日 13:51
 */
@Data
@NoArgsConstructor
@ApiModel(value = "单个位置更新的请求参数")
public class LocationUpdatingReq implements Serializable {

    private static final long serialVersionUID = -6072515135384489304L;

    /**
     * X号码
     */
    @NotEmpty(message = "X号码不能为空")
    @ApiModelProperty(value = "X号码")
    private String number;

    /**
     * 号码的imsi码
     */
    @NotEmpty(message = "号码的imsi码不能为空")
    @ApiModelProperty(value = "号码的imsi码")
    private String imsi;

    /**
     * MAP的接口地址
     */
    @NotEmpty(message = "MAP的接口地址不能为空")
    @ApiModelProperty(value = "MAP的接口地址")
    private String mapUrl;

    /**
     * 最后一次失败原因
     */
    @JsonIgnore
    private String failCause;


    public LocationUpdatingReq(DialTestNumberDTO numberInfo, String mapUrl) {
        this.mapUrl = mapUrl;
        this.imsi = numberInfo.getImsi();
        this.number = numberInfo.getNumber();
    }
}
