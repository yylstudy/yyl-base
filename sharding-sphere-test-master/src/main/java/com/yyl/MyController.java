package com.yyl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSONObject;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.spring.boot.ShardingSphereAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionManager;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2021/12/17 16:03
 */
@RestController
@Slf4j
public class MyController {

    @Value("${threadNum}")
    private Integer threadNum;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Value("${messageNum}")
    private Integer messageNum;
    @Value("${queryNum:100}")
    private Integer queryNum;
    @Value("${queueNum}")
    private Integer queueNum;
    @Autowired
    private ShardingSphereDataSource shardingSphereDataSource;
    @Autowired
    private TransactionManager transactionManager;
    @Autowired
    private ShardingSphereAutoConfiguration shardingSphereAutoConfiguration;
    @RequestMapping("test222")
    public String test2() throws Exception{
        log.info("dataSource:{}",shardingSphereDataSource);
        Field field = ReflectionUtils.findField(ShardingSphereAutoConfiguration.class,"dataSourceMap");
        ReflectionUtils.makeAccessible(field);
        Map<String, DataSource> dataSourceMap = (Map<String, DataSource>)ReflectionUtils.getField(field,shardingSphereAutoConfiguration);
        HikariDataSource dataSource = (HikariDataSource)dataSourceMap.get("master");
        Connection connection1 = dataSource.getConnection();
        Connection connection2 = dataSource.getConnection();
        Connection connection3 = dataSource.getConnection();
        Connection connection4 = dataSource.getConnection();
        Connection connection5 = dataSource.getConnection();
        log.info("shardingSphereAutoConfiguration:{}",shardingSphereAutoConfiguration);
        log.info("transactionManager:{}",transactionManager);
        return "success";
    }
    @RequestMapping("test333")
    public String test333() throws Exception{
        log.info("insert message start");
        long t1 = System.currentTimeMillis();
        ExecutorService pool = new ThreadPoolExecutor(threadNum, threadNum,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(queueNum),new ThreadPoolExecutor.CallerRunsPolicy());

        for(int i=0;i<messageNum;i++){
            pool.execute(()->{
                Random random = new Random();
                int j = random.nextInt(99999);
                jdbcTemplate.execute("insert into test1_20220_95338 values("+j+",'yyl1','test1')");
            });
        }
        pool.shutdown();
        log.info("executorService:{}",pool);
        while(true){
            if(pool.isTerminated()){
                log.info("所有的子线程都结束了");
                break;
            }
            Thread.sleep(100);
        }
        long t2 = System.currentTimeMillis();
        log.info("insert message end");
        log.info("time is:{}",t2-t1);
        return (t2-t1)+"";

    }
    @RequestMapping("test111")
    public String test1() throws Exception{
        log.info("insert message start");
        long t1 = System.currentTimeMillis();
        ExecutorService pool = new ThreadPoolExecutor(threadNum, threadNum,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(queueNum),new ThreadPoolExecutor.CallerRunsPolicy());

        for(int i=0;i<messageNum;i++){
            String streamnumber = UUID.randomUUID().toString().replace("-","");
            Random random = new Random();
            int ss = random.nextInt(100);
            int fix = random.nextInt(9999);
            String callingpartynumber = 1596870+StringUtils.leftPad(String.valueOf(fix),4);
            String sql = "INSERT INTO `acr_record_transfer` (`streamnumber`, `servicekey`, `callcost`, `calledpartynumber`, `callingpartynumber`, `chargemode`, `specificchargedpar`, " +
                    "`translatednumber`, `startdateandtime`, `stopdateandtime`, `duration`, `chargeclass`, `transparentparamet`, `calltype`, `callersubgroup`, `calleesubgroup`, " +
                    "`acrcallid`, `oricallednumber`, `oricallingnumber`, `callerpnp`, `calleepnp`, `reroute`, `groupnumber`, `callcategory`, `chargetype`, `userpin`, `acrtype`, " +
                    "`videocallflag`, `serviceid`, `forwardnumber`, `extforwardnumber`, `srfmsgid`, `msserver`, `begintime`, `releasecause`, `releasereason`, `areanumber`, `calledareacode`, " +
                    "`localorlong`, `id`, `dtmfkey`, `callintime`, `operator`) " +
                    "VALUES ('"+streamnumber+"', "+ss+", '0', '95078381', '"+callingpartynumber+"', '48', '95078381', '15968704825', '20221101003519', '20221101003607'," +
                    " '49', '999', '', '48840', 'IN-440100YD-YH', 'OUT-3410-HEZHONG02', 'C20221101003518AC57dcb63c24c', '95078381', '15968704825', '', '', '1', '3410', '1', '0', '', " +
                    "'2', '49', '', '57dcb63c-24cd-4f25-99eb-e2ab1f941826', '', 'media/3410/20221101/2022110100/15968704825-95078381-8c0cd571-33c1-44c7-bd2a-c9284ef2a67e.wav', " +
                    "'10.105.3.47', '20221101003518', '1', 'CALL_CALLER_HANG_UP', '0577', '', NULL, '0', '', '', '4')";
            pool.execute(()->{
                jdbcTemplate.execute(sql);
            });
        }
        pool.shutdown();
        log.info("executorService:{}",pool);
        while(true){
            if(pool.isTerminated()){
                log.info("所有的子线程都结束了");
                break;
            }
            Thread.sleep(100);
        }
        long t2 = System.currentTimeMillis();
        log.info("insert message end");
        log.info("time is:{}",t2-t1);
        return (t2-t1)+"";
    }

    @RequestMapping("test444")
    public String test444() throws Exception{
        log.info("insert message start");
        long t1 = System.currentTimeMillis();
        ExecutorService pool = new ThreadPoolExecutor(threadNum, threadNum,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(queueNum),new ThreadPoolExecutor.CallerRunsPolicy());

        for(int i=0;i<messageNum;i++){
            String streamnumber = UUID.randomUUID().toString().replace("-","");
            Random random = new Random();
            int ss = random.nextInt(100);
            int fix = random.nextInt(9999);
            String callingpartynumber = 1596870+StringUtils.leftPad(String.valueOf(fix),4);
            String sql = "INSERT INTO `acr_record_transfes` (`streamnumber`, `servicekey`, `callcost`, `calledpartynumber`, `callingpartynumber`, `chargemode`, `specificchargedpar`, " +
                    "`translatednumber`, `startdateandtime`, `stopdateandtime`, `duration`, `chargeclass`, `transparentparamet`, `calltype`, `callersubgroup`, `calleesubgroup`, " +
                    "`acrcallid`, `oricallednumber`, `oricallingnumber`, `callerpnp`, `calleepnp`, `reroute`, `groupnumber`, `callcategory`, `chargetype`, `userpin`, `acrtype`, " +
                    "`videocallflag`, `serviceid`, `forwardnumber`, `extforwardnumber`, `srfmsgid`, `msserver`, `begintime`, `releasecause`, `releasereason`, `areanumber`, `calledareacode`, " +
                    "`localorlong`, `id`, `dtmfkey`, `callintime`, `operator`) " +
                    "VALUES ('"+streamnumber+"', "+ss+", '0', '95078381', '"+callingpartynumber+"', '48', '95078381', '15968704825', '20221101003519', '20221101003607'," +
                    " '49', '999', '', '48840', 'IN-440100YD-YH', 'OUT-3410-HEZHONG02', 'C20221101003518AC57dcb63c24c', '95078381', '15968704825', '', '', '1', '3410', '1', '0', '', " +
                    "'2', '49', '', '57dcb63c-24cd-4f25-99eb-e2ab1f941826', '', 'media/3410/20221101/2022110100/15968704825-95078381-8c0cd571-33c1-44c7-bd2a-c9284ef2a67e.wav', " +
                    "'10.105.3.47', '20221101003518', '1', 'CALL_CALLER_HANG_UP', '0577', '', NULL, '0', '', '', '4')";
            pool.execute(()->{
                jdbcTemplate.execute(sql);
            });
        }
        pool.shutdown();
        log.info("executorService:{}",pool);
        while(true){
            if(pool.isTerminated()){
                log.info("所有的子线程都结束了");
                break;
            }
            Thread.sleep(100);
        }
        long t2 = System.currentTimeMillis();
        log.info("insert message end");
        log.info("time is:{}",t2-t1);
        return (t2-t1)+"";
    }


    @RequestMapping("test555")
    public String test555() throws Exception{
        log.info("query message start");
        long t1 = System.currentTimeMillis();
        ExecutorService pool = new ThreadPoolExecutor(threadNum, threadNum,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(queueNum),new ThreadPoolExecutor.CallerRunsPolicy());
        for(int i=0;i<queryNum;i++){
            Random random = new Random();
            int ss = random.nextInt(100);
            String sql = "select max(areanumber) from acr_record_transfer where servicekey="+ss;
            pool.execute(()->{
                long num = jdbcTemplate.queryForObject(sql,Long.class);
                log.info("num:{}",num);
            });
        }
        pool.shutdown();
        log.info("executorService:{}",pool);
        while(true){
            if(pool.isTerminated()){
                log.info("所有的子线程都结束了");
                break;
            }
            Thread.sleep(100);
        }
        long t2 = System.currentTimeMillis();
        log.info("insert message end");
        log.info("time is:{}",t2-t1);
        return (t2-t1)+"";
    }

    @RequestMapping("test666")
    public String test666() throws Exception{
        log.info("query message start");
        long t1 = System.currentTimeMillis();
        ExecutorService pool = new ThreadPoolExecutor(threadNum, threadNum,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(queueNum),new ThreadPoolExecutor.CallerRunsPolicy());
        for(int i=0;i<queryNum;i++){
            Random random = new Random();
            int ss = random.nextInt(100);
            String sql = "select max(areanumber) from acr_record_transfes where servicekey="+ss;
            pool.execute(()->{
                long num = jdbcTemplate.queryForObject(sql,Long.class);
                log.info("num:{}",num);
            });
        }
        pool.shutdown();
        log.info("executorService:{}",pool);
        while(true){
            if(pool.isTerminated()){
                log.info("所有的子线程都结束了");
                break;
            }
            Thread.sleep(100);
        }
        long t2 = System.currentTimeMillis();
        log.info("insert message end");
        log.info("time is:{}",t2-t1);
        return (t2-t1)+"";
    }


    @RequestMapping("test777")
    public String test777() throws Exception{
        String sql = "select * from acr_record_transfer where servicekey=100 limit 10000 ,10";
        jdbcTemplate.queryForList(sql,Object.class);
        sql = "select * from acr_record_transfer limit 10000 ,10";
        jdbcTemplate.queryForList(sql,Object.class);
        return "success";
    }

    @RequestMapping("test888")
    public String test888() throws Exception{
        String[] cmds = {"/bin/sh", "-c", "node /home/meshcentral/node_modules/meshcentral --logintoken user//admin"};
//        String[] cmds = {"dir"};
        Process process = Runtime.getRuntime().exec(cmds);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        while ((line = reader.readLine()) != null) {
            log.info("line："+line);
        }
        log.info("process.waitFor()"+process.waitFor());
        return "success";
    }

    @RequestMapping("test999")
    public String test999() throws Exception{
        long id = IdUtil.getSnowflakeNextId();
        String sql = "insert into test(id,name,create_time) values("+id+",'yyl','2022-10-10 09:09:09')";
        jdbcTemplate.execute(sql);
        id = IdUtil.getSnowflakeNextId();
        sql = "insert into test(id,name,create_time) values("+id+",'yyl','2022-12-10 09:09:09')";
        jdbcTemplate.execute(sql);
        id = IdUtil.getSnowflakeNextId();
        sql = "insert into test(id,name,create_time) values("+id+",'yyl','2022-12-12 09:09:09')";
        jdbcTemplate.execute(sql);
        return "success";
    }
}
