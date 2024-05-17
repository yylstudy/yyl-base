package com.cqt.wechat.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 微信视频号号码池同步变更通知
 *
 * @author Xienx
 * @date 2023年04月06日 9:41
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WechatNotifyDTO implements Serializable {

    private static final long serialVersionUID = 2507224716079473696L;

    /**
     * 区号列表
     */
    @JSONField(name = "area_code_list")
    private List<String> areaCodeList;
}
