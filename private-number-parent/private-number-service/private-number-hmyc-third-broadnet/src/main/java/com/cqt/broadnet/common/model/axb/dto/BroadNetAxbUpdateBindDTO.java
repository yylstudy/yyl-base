package com.cqt.broadnet.common.model.axb.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Xienx
 * @date 2023-05-25 15:50:15:50
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BroadNetAxbUpdateBindDTO implements Serializable {

    private static final long serialVersionUID = -5917377292224116310L;

    /**
     * 请求类型 1 修改绑定信息 2 服务状态检查
     */
    private Integer requestType;

    /**
     * 修改信息结构体, requestType为1时必选
     */
    private ModInfo modInfo;

    /**
     * 检查信息结构体
     * requestType为2时必选
     */
    private CheckInfo checkInfo;

    @Data
    public static class ModInfo implements Serializable {

        private static final long serialVersionUID = -6622847983784806515L;

        /**
         * AXB 绑定id
         */
        @ApiModelProperty(value = "绑定id", required = true)
        private String bindId;

        /**
         * 新A号码 E.164标准
         */
        @ApiModelProperty(value = "新A号码")
        private String newANumber;

        /**
         * 新B号码 E.164标准
         */
        @ApiModelProperty(value = "新B号码")
        private String newBNumber;

        /**
         * 新绑定有效期
         */
        @ApiModelProperty(value = "新绑定有效期")
        private Long newExpiration;
    }

    @Data
    public static class CheckInfo implements Serializable {

        private static final long serialVersionUID = -1011046606546253356L;

        /**
         * AXB 绑定id
         */
        @ApiModelProperty(value = "AXB绑定id", required = true)
        private String bindId;
    }
}
