package com.cqt.model.push.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author hlx
 * @date 2021-09-15
 */
@NoArgsConstructor
@Data
@TableName("private_fail_message")
public class PrivateFailMessage {

    /**
     * 主键id uuid策略
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 信息体json
     */
    private String body;

    /**
     * 机器ip
     */
    private String ip;

    /**
     * 推送失败的url
     */
//    private String url;

    /**
     * 重推次数
     */
    private int num;

    /**
     * 错误信息
     */
    private String errMsg;

    /**
     * 企业id
     */
    private String vccid;

    /**
     * PushTypeEnum 类型
     * STATUS 通话状态   BILL  通话话单
     */
    private String type;

    /**
     * 入库时间
     */
    private Date createTime;

//    @JSONField(name = "user_data")
//    private String userData;
}
