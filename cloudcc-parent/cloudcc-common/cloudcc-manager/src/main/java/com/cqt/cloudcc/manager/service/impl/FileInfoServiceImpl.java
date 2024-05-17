package com.cqt.cloudcc.manager.service.impl;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.util.CacheUtil;
import com.cqt.cloudcc.manager.service.FileInfoService;
import com.cqt.mapper.SkillInfoMapper;
import com.cqt.starter.redis.util.RedissonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author linshiqiang
 * date:  2023-11-08 16:44
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileInfoServiceImpl implements FileInfoService {

    private final RedissonUtil redissonUtil;

    private final SkillInfoMapper skillInfoMapper;

    @Override
    public String getFilePath(String companyCode, String fileId) {
        String fileIdKey = CacheUtil.getFileIdKey(companyCode, fileId);
        try {
            String filePath = redissonUtil.get(fileIdKey);
            if (StrUtil.isNotEmpty(filePath)) {
                return filePath;
            }
        } catch (Exception e) {
            log.error("[getFilePath] key: {}, get redis error: ", fileIdKey, e);
        }

        String filePath = skillInfoMapper.getFilePath(fileId);
        if (StrUtil.isNotEmpty(filePath)) {
            try {
                redissonUtil.set(fileIdKey, filePath);
            } catch (Exception e) {
                log.error("[getFilePath] key: {}, write redis error: ", fileIdKey, e);
            }
        }
        return filePath;
    }
}
