package com.cqt.hmyc.web.blacklist.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.web.blacklist.mapper.CallerNumberBlacklistMapper;
import com.cqt.hmyc.web.blacklist.model.dto.CallerNumberBlacklistOperateDTO;
import com.cqt.hmyc.web.blacklist.model.entity.CallerNumberBlacklist;
import com.cqt.model.common.Result;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author linshiqiang
 * date:  2024-02-04 10:18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallerNumberBlacklistService {

    private final CallerNumberBlacklistMapper callerNumberBlacklistMapper;

    private final RedissonUtil redissonUtil;

    private final HideProperties hideProperties;

    public Result callerBlacklist(CallerNumberBlacklistOperateDTO callerNumberBlacklistOperateDTO,
                                  String businessType,
                                  String vccId) {
        Integer opType = callerNumberBlacklistOperateDTO.getOpType();
        List<String> numInfoList = callerNumberBlacklistOperateDTO.getNumInfoList();
        if (numInfoList.size() > 100) {
            return Result.fail(400, "黑名单操作失败，黑名单号码数量超过100个");
        }

        // 新增
        if (opType == 0) {
            List<CallerNumberBlacklist> list = getCallerBlacklist(vccId, businessType, numInfoList);
            callerNumberBlacklistMapper.batchInsert(list);
            String now = DateUtil.now();
            for (CallerNumberBlacklist blacklist : list) {
                String callerBlacklistKey = PrivateCacheUtil.getCallerBlacklistKey(vccId,
                        businessType,
                        blacklist.getCallerNumber(),
                        blacklist.getCalleeNumber());
                redissonUtil.setString(callerBlacklistKey, now);
            }
            return Result.ok();
        }

        // 刪除
        if (opType == 1) {
            List<CallerNumberBlacklist> list = getCallerBlacklist(vccId, businessType, numInfoList);
            LambdaQueryWrapper<CallerNumberBlacklist> queryWrapper = new LambdaQueryWrapper<>();
            for (CallerNumberBlacklist blacklist : list) {
                queryWrapper.eq(CallerNumberBlacklist::getVccId, blacklist.getVccId())
                        .eq(CallerNumberBlacklist::getBusinessType, blacklist.getBusinessType())
                        .eq(CallerNumberBlacklist::getCallerNumber, blacklist.getCallerNumber())
                        .eq(CallerNumberBlacklist::getCalleeNumber, blacklist.getCalleeNumber());
                callerNumberBlacklistMapper.delete(queryWrapper);
                queryWrapper.clear();

                String callerBlacklistKey = PrivateCacheUtil.getCallerBlacklistKey(vccId,
                        businessType,
                        blacklist.getCallerNumber(),
                        blacklist.getCalleeNumber());
                redissonUtil.delKey(callerBlacklistKey);
            }
            return Result.ok();
        }
        return Result.fail(401, "opType不支持");
    }

    private List<CallerNumberBlacklist> getCallerBlacklist(String vccId, String businessType, List<String> numInfoList) {
        List<CallerNumberBlacklist> list = new ArrayList<>();
        DateTime date = DateUtil.date();
        for (String number : numInfoList) {
            CallerNumberBlacklist callerNumberBlacklist = new CallerNumberBlacklist();
            callerNumberBlacklist.setVccId(vccId);
            callerNumberBlacklist.setBusinessType(businessType);
            String[] numbers = number.split(":");
            callerNumberBlacklist.setCallerNumber(numbers[0]);
            callerNumberBlacklist.setCalleeNumber(numbers[1]);
            callerNumberBlacklist.setCreateTime(date);
            list.add(callerNumberBlacklist);
        }
        return list;
    }

    public boolean checker(String vccId, String businessType, String callerNumber, String calleeNumber) {
        Map<String, Boolean> callerBlacklistChecker = hideProperties.getCallerBlacklistChecker();
        if (CollUtil.isEmpty(callerBlacklistChecker)) {
            return false;
        }
        Boolean enable = callerBlacklistChecker.get(businessType);
        if (!Boolean.TRUE.equals(enable)) {
            return false;
        }
        String callerBlacklistKey = PrivateCacheUtil.getCallerBlacklistKey(vccId, businessType, callerNumber, calleeNumber);
        try {
            String data = redissonUtil.getString(callerBlacklistKey);
            if (StrUtil.isNotEmpty(data)) {
                return true;
            }
        } catch (Exception e) {
            log.error("来电黑名单redis查询异常: ", e);
        }
        return false;
    }
}
