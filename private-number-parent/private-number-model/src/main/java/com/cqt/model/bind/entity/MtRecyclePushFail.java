package com.cqt.model.bind.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author linshiqiang
 * @date 2021/11/1 13:54
 */
@Data
public class MtRecyclePushFail {

    @TableId
    private String requestId;

    private String telA;

    private String telB;

    private String tel;

    private String telX;

    private String extNum;

    private String expireTime;

    private String areaCode;

    private String numType;

    private String operateType;
}
