package com.cqt.model.monitor.entity.ding;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @date 2021-09-27
 * @author hlx
 */
@Data
public class DingAt {

    private List<String> atMobiles;

    @JSONField(name = "isAtAll")
    private boolean isAtAll;

}
