package com.linkcircle.basecom.handler.defaultHandler;

import com.linkcircle.basecom.common.DictModel;
import com.linkcircle.basecom.constants.CommonConstant;
import com.linkcircle.basecom.handler.DictHandler;
import com.linkcircle.basecom.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 字段服务
 * @createTime 2024/3/29 17:56
 */
@Component
@ConditionalOnClass(name="org.apache.ibatis.binding.MapperProxy")
public class DefaultDictHandler implements DictHandler {
    @Autowired
    private DataSource dataSource;
    @Autowired(required = false)
    private RedisTemplate redisTemplate;
    @Override
    public List<DictModel> getDictItemByDictCode(String dictCode) {
        String dictCachekey = CommonConstant.DEFAULT_DICT_CACHE_PREFIX+dictCode;
        String valueStr = (String)redisTemplate.opsForValue().get(dictCachekey);
        if(StringUtils.hasText(valueStr)){
            return JsonUtil.parseList(valueStr,DictModel.class);
        }
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "SELECT b.item_value,b.item_text FROM sys_dict a,sys_dict_item b" +
                " where a.id = b.dict_id and a.dict_code=?";
        try{
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            ps.setString(1,dictCode);
            rs = ps.executeQuery();
            List<DictModel> list = new ArrayList<>();
            while(rs.next()){
                String itemValue = rs.getString(1);
                String itemText = rs.getString(2);
                DictModel dictModel = new DictModel(itemValue,itemText);
                list.add(dictModel);
            }
            redisTemplate.opsForValue().set(dictCachekey,JsonUtil.toJSONString(list));
            return list;
        }catch (Exception e){
            throw new RuntimeException(e);
        }finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            JdbcUtils.closeConnection(connection);
        }
    }
}
