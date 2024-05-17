package com.cqt.broadnet.common.model.axb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Xienx
 * @date 2023-05-25 15:17:15:17
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BroadNetAxbBindDTO implements Serializable {

    private static final long serialVersionUID = 7199096710198325290L;

    /**
     * 请求id, 唯一
     */
    @ApiModelProperty(value = "请求id", example = "1234567890", required = true)
    private String requestId;

    /**
     * 号码A 遵循E.164标准
     */
    @ApiModelProperty(value = "号码A", example = "861381245678", required = true)
    private String telA;

    /**
     * 号码B 遵循E.164标准
     */
    @ApiModelProperty(value = "号码B", example = "861381245678", required = true)
    private String telB;

    /**
     * 虚拟号码X
     */
    @ApiModelProperty(value = "虚拟号码X", example = "8613812345679")
    private String xNumber;

    /**
     * 区号
     */
    @ApiModelProperty(value = "区号", example = "21", required = true)
    private Integer areaCode;

    /**
     * 过期时间
     */
    @ApiModelProperty(value = "过期时间", example = "7200", required = true)
    private Long expiration;

    /**
     * 录音参数
     */
    @ApiModelProperty(value = "录音参数")
    private Extra extra;

    private String sign;

    @Data
    public static class Extra implements Serializable {

        private static final long serialVersionUID = 7342471739795178354L;

        /**
         * 录音标记
         * 0 表示不录音, 1 表示需要录音
         */
        @ApiModelProperty(value = "录音标记", example = "0")
        @JsonProperty(index = 3)
        private Integer record;

        /**
         * A号码播放的提示音, 没携带则不放音
         */
        @ApiModelProperty(value = "A号码播放的提示音", example = "0")
        @JsonProperty(index = 1)
        private String beepA;

        /**
         * B号码播放的提示音, 没携带则不放音
         */
        @ApiModelProperty(value = "B号码播放的提示音", example = "0")
        @JsonProperty(index = 2)
        private String beepB;
    }
}
