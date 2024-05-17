package com.cqt.ivr.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.ivr.entity.CompanyPbxtimeDaily;
import com.cqt.ivr.entity.CompanyPbxtimeSpecial;
import com.cqt.ivr.entity.CompanyPbxtimes;
import com.cqt.ivr.entity.vo.JudgeTimeQuantumRes;
import com.cqt.ivr.mapper.CompanyPbxtimeDailyMapper;
import com.cqt.ivr.mapper.CompanyPbxtimeSpecialMapper;
import com.cqt.ivr.mapper.CompanyPbxtimesMapper;
import com.cqt.ivr.service.ICompanyPbxtimesService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.cqt.ivr.utils.TimeUtils.*;

/**
 * @author ld
 * @since 2023-07-24
 */
@Service
public class CompanyPbxtimesServiceImpl extends ServiceImpl<CompanyPbxtimesMapper, CompanyPbxtimes> implements ICompanyPbxtimesService {

    @Resource
    CompanyPbxtimeDailyMapper companyPbxtimeDailyService;

    @Resource
    CompanyPbxtimeSpecialMapper companyPbxtimeSpecialService;


    @Override
    public JudgeTimeQuantumRes validationIsWorkingTime(String timeId) {
        Date currentTime = new Date();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(currentTime);
        String time = new SimpleDateFormat("HH:mm:ss").format(currentTime);

        try {
            // 查日程
            CompanyPbxtimes companyPbxtimes = this.getOne(new QueryWrapper<CompanyPbxtimes>().lambda().eq(CompanyPbxtimes::getId, timeId));
            if (companyPbxtimes == null) {
                return JudgeTimeQuantumRes.error("日程不存在！");
            }
            // 白名单
            if (validationDateIsInRoll(timeId, "1", date, time)) return JudgeTimeQuantumRes.OK("工作时间", "1");

            // 黑名单
            if (validationDateIsInRoll(timeId, "2", date, time)) return JudgeTimeQuantumRes.OK("非工作时间", "2");

            return validationDateIsInDaily(timeId, date, time, companyPbxtimes);
        } catch (ParseException e) {
            return JudgeTimeQuantumRes.error("传递的参数形式有误！");
        }
    }

    private JudgeTimeQuantumRes validationDateIsInDaily(String timeId, String date, String time, CompanyPbxtimes companyPbxtimes) throws ParseException {
        String startTime = companyPbxtimes.getStartDate();
        String endTime = companyPbxtimes.getEndDate();
        if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
            if (!validityTime(startTime, endTime, date, YEAR_MONTH_DAY)) {
                return JudgeTimeQuantumRes.OK("非工作时间", "2");
            }
        }
        String weekDay = acquisitionWeek(date);
        CompanyPbxtimeDaily companyPbxtimeDaily = companyPbxtimeDailyService.selectOne(new QueryWrapper<CompanyPbxtimeDaily>().lambda().eq(CompanyPbxtimeDaily::getTimeId, timeId).eq(CompanyPbxtimeDaily::getWeekDay, weekDay));
        String stime = companyPbxtimeDaily.getStartTime();
        String etime = companyPbxtimeDaily.getEndTime();
        if (stime != null && etime != null) {
            if (validityTime(stime, etime, time, HOUR_MINUTE_SECOND)) {
                return JudgeTimeQuantumRes.OK("工作时间", "1");
            }
        }
        return JudgeTimeQuantumRes.OK("非工作时间", "2");
    }

    private boolean validationDateIsInRoll(String timeId, String val, String date, String time) throws ParseException {
        List<CompanyPbxtimeSpecial> white = companyPbxtimeSpecialService.selectList(new QueryWrapper<CompanyPbxtimeSpecial>().lambda().eq(CompanyPbxtimeSpecial::getTimeId, timeId).eq(CompanyPbxtimeSpecial::getType, val));
        for (CompanyPbxtimeSpecial companyPbxtimeSpecial : white) {
            if (validityTime(companyPbxtimeSpecial.getStartDate(), companyPbxtimeSpecial.getEndDate(), date, YEAR_MONTH_DAY)) {
                if (validityTime(companyPbxtimeSpecial.getStartTime(), companyPbxtimeSpecial.getEndTime(), time, HOUR_MINUTE_SECOND)) {
                    return true;
                }
            }
        }
        return false;
    }
}
