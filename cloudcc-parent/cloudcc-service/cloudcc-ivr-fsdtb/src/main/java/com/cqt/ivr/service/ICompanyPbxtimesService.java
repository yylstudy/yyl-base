package com.cqt.ivr.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cqt.ivr.entity.CompanyPbxtimes;
import com.cqt.ivr.entity.vo.JudgeTimeQuantumRes;

/**
 *
 * @author ld
 * @since 2023-07-24
 */
public interface ICompanyPbxtimesService extends IService<CompanyPbxtimes> {
    JudgeTimeQuantumRes validationIsWorkingTime(String timeId);
}
