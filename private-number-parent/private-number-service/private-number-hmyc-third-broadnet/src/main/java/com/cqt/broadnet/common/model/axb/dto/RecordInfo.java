package com.cqt.broadnet.common.model.axb.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author huweizhong
 * date  2023/6/5 15:54
 */
@Data
public class RecordInfo {

    @TableId(type = IdType.INPUT)
    private String recordId;

    private String recordUrl;
}
