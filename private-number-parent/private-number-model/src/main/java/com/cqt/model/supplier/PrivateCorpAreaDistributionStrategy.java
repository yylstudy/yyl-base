package com.cqt.model.supplier;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 企业地市号码分配策略(PrivateCorpAreaDistributionStrategy)表实体类
 *
 * @author linshiqiang
 * @since 2022-12-20 14:13:20
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivateCorpAreaDistributionStrategy {

    /**
     * 策略id(vcc_id+area_code)
     */
    @ApiModelProperty(value = "策略id(vcc_id+area_code)")
    @TableId(type = IdType.INPUT)
    private String strategyId;

    /**
     * 企业id
     */
    @ApiModelProperty(value = "企业id")
    private String vccId;

    /**
     * 地市编码
     */
    @ApiModelProperty(value = "地市编码")
    private String areaCode;

    /**
     * 分配策略信息json [{supplier_id,weight}]
     */
    @ApiModelProperty(value = "分配策略信息json [{supplierId,weight}]")
    private String strategyInfo;


}

