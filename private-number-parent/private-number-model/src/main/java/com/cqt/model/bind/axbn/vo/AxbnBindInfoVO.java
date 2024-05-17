package com.cqt.model.bind.axbn.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/4/13 9:45
 */
@Data
public class AxbnBindInfoVO implements Serializable {

    private static final long serialVersionUID = -5824667342558667138L;

    /**
     * 绑定关系ID
     */
    @JsonProperty("bind_id")
    private String bindId;

    /**
     * A号码；18600008888或0108888999;
     */
    @JsonProperty("tel_a")
    private String telA;

    /**
     * B号码；18600008888或0108888999,
     */
    @JsonProperty("tel_b")
    private String telB;

    /**
     * X虚号；所有B号码呼叫tel_x都能转接到tel_a号码, tel_a呼叫tel_x转接到tel_b号码
     */
    @JsonProperty("tel_x")
    private String telX;

    /**
     * 1:tel_a 只需要能联系到tel_b;
     * 2:tel_a 需要能联系到所有B号码;
     * 以上两种模式中所有B号码呼叫tel_x都能转接到tel_a号码, tel_a呼叫tel_x转接到tel_b号码；
     */
    @JsonProperty("mode")
    private Integer mode;

    /**
     * 地市编码
     */
    @JsonProperty("area_code")
    private String areaCode;

    /**
     * 有效期 s
     */
    private Long expiration;


    /**
     * tel_other_b与tel_y对应关系；例"otherB_Y":[{"18613883404":"18540257293"},{"18611111111":"18540250502"}]
     */
    @JsonProperty("otherB_Y")
    private List<Map<String, String>> otherByList;
}
