package com.cqt.model.queue.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * IVR服务信息(CloudccIvrServeInfo)表实体类
 *
 * @author linshiqiang
 * @since 2023-10-09 14:36:20
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@TableName("cloudcc_ivr_service_info")
public class IvrServiceInfo implements Serializable {

    private static final long serialVersionUID = -5180461206718503086L;
    
    /**
     * ivr服务id
     */
    @TableId(type = IdType.INPUT)
    @JsonProperty("ivr_service_id")
    private String id;

    /**
     * ivr服务名称
     */
    @TableField("service_name")
    @JsonProperty("ivr_service_name")
    private String serviceName;

    /**
     * 租户id同企业编码
     */
    @TableField(value = "tenant_id")
    @JsonProperty("tenantId")
    private String companyCode;

    /**
     * ivr的id
     */
    private String ivrId;

}

