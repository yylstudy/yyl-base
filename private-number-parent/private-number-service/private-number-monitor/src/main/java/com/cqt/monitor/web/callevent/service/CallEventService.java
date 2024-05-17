package com.cqt.monitor.web.callevent.service;

import com.cqt.model.common.Result;
import com.cqt.monitor.web.callevent.mapper.AcrMapper;
import com.cqt.monitor.web.callevent.mapper.EventInMinMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * @author huweizhong
 * date  2017/10/30 16:06
 */
@Service
@RequiredArgsConstructor
public class CallEventService {


    private final EventInMinMapper eventInMinMapper;

    private final AcrMapper acrMapper;

    private static final String TABLE_PRE = "private_call_event_stats_";
    private static final String ACR_TABLE_PRE = "acr_record_";

    private static final String CON_TABLE_PRE = "private_corp_concurrency_info_";


    public Result getPickUp(String vccId, String time) {
        String replace = time.split(" ")[0].replace("-", "");
        String tableName = TABLE_PRE + replace;
        try {
            Map<String, BigDecimal > map = eventInMinMapper.getPickupRate(tableName, time, vccId);
            BigDecimal pickUp = map.get("pickup");
            BigDecimal cdr = map.get("cdr");
            if (cdr.compareTo(new BigDecimal(0)) == 0){
                return Result.ok(0);

            }
            BigDecimal divide = pickUp.divide(cdr, 2, RoundingMode.HALF_UP);
            return Result.ok(divide);
        }catch (Exception e){
            return Result.fail(500, String.valueOf(e));
        }

    }

    public Result getCount(String vccId, String month) {
        String tableName = ACR_TABLE_PRE + vccId + "_" + month;
        try {
            List<Map<String, Object>> acrCount = acrMapper.getAcrCount(tableName);
            return Result.ok(acrCount);
        }catch (Exception e){
            return Result.fail(500, String.valueOf(e));
        }
    }

    public Result getConcurrency(String vccId, String time) {
        String replace = time.split(" ")[0].replace("-", "").substring(0,6);
        String tableName = CON_TABLE_PRE + replace;
        try {
            String concurrency = eventInMinMapper.getConcurrency(tableName, time, vccId);
            return Result.ok(concurrency);
        }catch (Exception e){
            return Result.fail(500, String.valueOf(e));
        }
    }



}
