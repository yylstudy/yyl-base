package com.cqt.monitor.web.callevent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Description: private_warning_rule
 * @Author: jeecg-boot
 * @Date:   2022-07-14
 * @Version: V1.0
 */
@Data
@TableName("private_warning_rule")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="private_warning_rule对象", description="private_warning_rule")
public class PrivateWarningRule implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
	/**关联config表主键id*/

    @ApiModelProperty(value = "关联config表主键id")
    private String configId;
	/**
     * 基础数据 0-话单量，1-呼叫量，2-绑定数量，3-绑定失败率，4-振铃率，5-振铃失败率，6-接通率，7-未接通率
     */

    @ApiModelProperty(value = "基础数据 0-话单量，1-呼叫量，2-绑定数量，3-绑定失败率，4-振铃率，5-振铃失败率，6-接通率，7-未接通率")
    private Integer basicData;
    /**
     * 统计周期
     */

    @ApiModelProperty(value = "统计周期")
    private Integer countCycle;
    /**
     * 周期条件 1-连续1周期，2-连续2周期，3-连续3周期
     */

    @ApiModelProperty(value = "周期条件 1-连续1周期，2-连续2周期，3-连续3周期")
    private Integer cycleCondition;
	/**周期计算 0-总计，1-不计算*/

    @ApiModelProperty(value = "周期计算 0-总计，1-不计算")
    private Integer cycleCount;
	/**条件*/

    @ApiModelProperty(value = "条件")
    private String compareCondition;
	/**阈值*/

    @ApiModelProperty(value = "阈值")
    private String threshold;
}
