package com.cqt.hmyc.web.model.hdh.axb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * hdh AXB模式  号码绑定接口参数
 *
 * @author dingsh
 * @since 2022-07-06
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HdhAxbResult implements Serializable {

    private static final long serialVersionUID = -8056276556567752106L;

    /**
     * 绑定请求响应码
     */
    private String code;

    /**
     * 返回结果描述：
     */
    private String message;

    /**
     * 绑定关系 ID
     */
    private String bindId;


    /**
     * 和多号平台生成绑定数据后返回的中间
     * 号
     */
    private String x_no;



}
