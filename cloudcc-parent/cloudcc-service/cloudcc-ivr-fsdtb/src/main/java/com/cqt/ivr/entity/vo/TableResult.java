/**
 * Copyright © 2017 公司名. All rights reserved.
 * 
 * @Title: TableResult.java
 * @Prject: CTDService
 * @Package: com.linkcircle.ui.entity
 * @Description: TODO
 * @author: awbsheng@gmail.com
 * @date: 2017年8月23日 下午3:01:27
 * @version: V1.0
 */
package com.cqt.ivr.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/** 
 * @ClassName: TableResult 
 * @Description: TODO
 * @author: wbsheng@gmail.com
 * @date: 2017年8月23日 下午3:01:27  
 */
@Data
@JsonInclude(Include.NON_NULL)
@ApiModel("响应实体类")
public class TableResult {
	@ApiModelProperty(value = "响应码", example = "0", required = true)
	private Integer status;
	@ApiModelProperty(value = "响应说明", example = "请求成功", required = true)
	private String message;
	@ApiModelProperty(value = "总数（分页查询时返回）")
	private Long total;
	@ApiModelProperty(value = "总页数（分页查询时返回）")
	private Long TotalPages;
	@ApiModelProperty(value = "数据（数组）")
	private String[] data;
	@ApiModelProperty(value = "数据（集合）")
	private List<?> rows;
	@ApiModelProperty(value = "数据（对象）")
	private Object result;
}
