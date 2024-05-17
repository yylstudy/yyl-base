package com.cqt.hmbc.handler;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * 自定义数据填充器
 *
 * @author Xienx
 * @date 2023年02月14日 10:24
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (log.isDebugEnabled()) {
            log.debug("start insert fill...");
        }

        this.setFieldValByName("createTime", DateUtil.date(), metaObject);
        this.setFieldValByName("updateTime", DateUtil.date(), metaObject);
        this.setFieldValByName("createBy", "private-number-hmbc", metaObject);
        this.setFieldValByName("updateBy", "private-number-hmbc", metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (log.isDebugEnabled()) {
            log.debug("start update fill...");
        }
        this.setFieldValByName("updateTime", DateUtil.date(), metaObject);
        this.setFieldValByName("updateBy", "private-number-hmbc", metaObject);
    }
}
