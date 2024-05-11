package com.yyl;

import cn.hutool.core.util.IdUtil;
import com.linkcircle.basecom.common.LoginUserInfo;
import com.linkcircle.basecom.handler.OperateLogHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/27 10:03
 */
@Slf4j
public class MyDefaultOperateLogService implements OperateLogHandler {
    public MyDefaultOperateLogService(){
        log.info("初始化：{}完成============================",this.getClass().getSimpleName());
    }
    @Autowired
    private DataSource dataSource;
    @Override
    public void addLog(HttpServletRequest request, boolean isSuccess, String failReason, String content, String requestUrl, String operateMethod, long costTime, LoginUserInfo loginUserInfo, String ip) {
        String sql = "INSERT INTO `sys_operate_log`(`id`, `user_id`, `realname`, `content`, `url`, `method`," +
                "  `cost_time`, `ip`, `success_flag`, `fail_reason`,  `create_time`) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)){
            long id = IdUtil.getSnowflakeNextId();
            ps.setLong(1,id);
            if(loginUserInfo==null){
                ps.setNull(2, Types.INTEGER);
                ps.setNull(3,Types.INTEGER);
            }else{
                ps.setLong(2,loginUserInfo==null?null:loginUserInfo.getId());
                ps.setString(3,loginUserInfo==null?"":loginUserInfo.getUsername());
            }
            ps.setString(4,content);
            ps.setString(5,requestUrl);
            ps.setString(6,operateMethod);
            ps.setLong(7,costTime);
            ps.setString(8,ip);
            ps.setLong(9,isSuccess?1:0);
            ps.setString(10,failReason);
            ps.setDate(11,new Date(System.currentTimeMillis()));
            ps.execute();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
