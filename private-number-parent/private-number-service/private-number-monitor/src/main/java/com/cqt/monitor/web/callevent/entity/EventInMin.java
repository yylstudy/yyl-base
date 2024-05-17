package com.cqt.monitor.web.callevent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EventInMin implements Serializable {

    @TableId(type = IdType.INPUT)
    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "话单量")
    private Integer cdrCount;


    @ApiModelProperty(value = "振铃量")
    private Integer ringing;

    @ApiModelProperty(value = "接通量")
    private Integer pickUpCount;


    @ApiModelProperty(value = "接通率")
    private BigDecimal pickUpRate;

    @ApiModelProperty(value = "振铃率")
    private BigDecimal ringRate;

    @ApiModelProperty(value = "VCCID")
    private String vccId;

    @ApiModelProperty(value = "供应商id")
    private String supplierId;

    @ApiModelProperty(value = "地市编码")
    private String areaCode;

    @ApiModelProperty(value = "当前时间")
    private String currentMin;

    @ApiModelProperty(value = "所属平台")
    private String platForm;


}
