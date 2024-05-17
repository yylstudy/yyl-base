package com.cqt.model.common;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author linshiqiang
 * @date 2022/2/15 15:29
 */
@Data
public class BaseAuth {

    @ApiModelProperty(value = "appkey", example = "173", hidden = true)
    private String appkey;

    @ApiModelProperty(value = "时间戳", example = "1634786279204")
    private Long ts;

    @ApiModelProperty(value = "签名", example = "1234567890")
    private String sign;

}
