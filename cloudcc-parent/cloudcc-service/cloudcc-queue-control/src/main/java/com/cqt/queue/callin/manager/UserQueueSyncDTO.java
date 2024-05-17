package com.cqt.queue.callin.manager;

import com.cqt.base.enums.OperateTypeEnum;
import com.cqt.model.queue.dto.UserQueueUpDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * @author linshiqiang
 * date:  2023-12-04 15:36
 */
@Data
public class UserQueueSyncDTO implements Serializable {

    private UserQueueUpDTO userQueueUpDTO;

    private OperateTypeEnum operateTypeEnum;

    private String companyCode;

    private String uuid;

    public UserQueueSyncDTO() {
    }

    public UserQueueSyncDTO(UserQueueUpDTO userQueueUpDTO, OperateTypeEnum operateTypeEnum) {
        this.userQueueUpDTO = userQueueUpDTO;
        this.operateTypeEnum = operateTypeEnum;
    }

    public UserQueueSyncDTO(String companyCode, String uuid) {
        this.companyCode = companyCode;
        this.uuid = uuid;
        this.operateTypeEnum = OperateTypeEnum.DELETE;
    }
}
