package com.cqt.hmyc.web.bind.service.recycle;

import com.cqt.hmyc.web.bind.mapper.PrivateRecyclePushFailMapper;
import com.cqt.hmyc.web.bind.service.axb.AxbBindConverter;
import com.cqt.hmyc.web.bind.service.recycle.db.DbOperationStrategyManager;
import com.cqt.hmyc.web.bind.service.recycle.recycle.RecycleNumberStrategyManager;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.bind.dto.BindRecycleDTO;
import com.cqt.model.bind.entity.PrivateRecyclePushFail;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.RedisException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author linshiqiang
 * @date 2021/11/1 13:30
 */
@Service
@Slf4j
public class NumberRecycleService {

    @Resource
    private PrivateRecyclePushFailMapper recyclePushFailMapper;

    @Resource
    private AxbBindConverter axbBindConverter;

    @Resource
    private RecycleNumberStrategyManager recycleNumberStrategyManager;

    @Resource
    private DbOperationStrategyManager dbOperationStrategyManager;

    public void savePushFail(BindRecycleDTO bindRecycleDTO) {
        try {
            PrivateRecyclePushFail recyclePushFail = axbBindConverter.bindRecycleDto2PrivateRecyclePushFail(bindRecycleDTO);
            recyclePushFailMapper.insert(recyclePushFail);
        } catch (Exception e) {
            log.error("savePushFail db 异常: {}", e.getMessage());
        }
    }

    /**
     * 保存数据库
     *
     * @param mqBindInfoBO 数据
     */
    public void saveBindInfo(MqBindInfoBO mqBindInfoBO) {

        dbOperationStrategyManager.operate(mqBindInfoBO);
    }

    /**
     * 回收号码
     * db 删除记录后, 若redis抛出RedisException异常, 回滚delete操作
     */
    @Transactional(rollbackFor = {RedisException.class})
    public void recycleNumber(BindRecycleDTO bindRecycleDTO) {

        recycleNumberStrategyManager.recycle(bindRecycleDTO);
    }

}
