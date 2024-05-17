package com.cqt.hmyc.web.bind.mapper.axb;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.model.bind.axb.entity.PrivateBindAxbInitUserTelPool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author linshiqiang
 * @date 2022/2/16 16:50
 * AXB用户初始化号码池标志记录 mapper
 */
@Mapper
public interface PrivateBindAxbInitUserTelPoolMapper extends BaseMapper<PrivateBindAxbInitUserTelPool> {

    @Select("SELECT id, version from private_bind_axb_init_user_tel_pool where id = #{id} limit 1")
    PrivateBindAxbInitUserTelPool selectVersion(String id);
}
