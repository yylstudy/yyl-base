package com.cqt.hmbc.retry;

import com.cqt.model.hmbc.dto.CdrRecordSimpleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * RetryQueryDTO
 *
 * @author Xienx
 * @date 2023年02月24日 10:03
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class RetryQueryDTO extends BaseRetryInfo {

    /**
     * 数据源名称
     */
    private String dsName;

    private CdrRecordSimpleEntity cdrRecord;

    @Override
    public String getBizId() {
        return String.format("企业 %s 查询被叫 %s 拨测话单", cdrRecord.getVccId(), cdrRecord.getCalledNum());
    }
}
