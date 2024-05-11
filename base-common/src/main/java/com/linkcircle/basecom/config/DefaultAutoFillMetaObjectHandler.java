package com.linkcircle.basecom.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.linkcircle.basecom.common.LoginUserInfo;
import com.linkcircle.basecom.filter.LoginUserInfoHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 填充默认字段
 * @createTime 2024/2/29 13:49
 */
@Slf4j
public class DefaultAutoFillMetaObjectHandler implements MetaObjectHandler {
    public DefaultAutoFillMetaObjectHandler(){
        log.info("初始化：{}完成============================",this.getClass().getSimpleName());
    }
    @Override
    public void insertFill(MetaObject metaObject) {
        Long userId = getLoginUserId();
        if(userId!=null){
            this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        }
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long userId = getLoginUserId();
        if(userId!=null){
            //更新updateBy，无论原来是否有值
            setFieldValByName("updateBy",userId,metaObject);
            //当updateBy有值的时候，不更新
            //this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
        }
        //更新updateTime，无论原来是否有值
        setFieldValByName("updateTime",new Date(),metaObject);
        //当updateTime有值的时候，不更新
        //this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }
    protected Long getLoginUserId(){
        LoginUserInfo loginUserInfo = LoginUserInfoHolder.get();
        if(loginUserInfo !=null){
            return loginUserInfo.getId();
        }
        return null;
    }
}
