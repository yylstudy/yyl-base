package com.linkcircle.system.dto;

import com.linkcircle.basecom.page.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Data
public class SysOperateLogQueryDto extends PageParam {


    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;


    @Schema(description = "用户名称")
    private String username;

    @Schema(description = "请求结果 false失败 true成功")
    private Boolean successFlag;

}
