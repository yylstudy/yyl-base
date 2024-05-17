package com.cqt.broadnet.web.axb.job;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.cqt.broadnet.common.model.axb.dto.SmsBack;
import com.cqt.broadnet.web.axb.jsch.JschConnectionPool;
import com.cqt.broadnet.web.axb.jsch.SftpProperties;
import com.cqt.broadnet.web.axb.mapper.SmsMapper;

import com.cqt.common.constants.ThirdConstant;
import com.cqt.redis.util.RedissonUtil;
import com.google.common.collect.Lists;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.chrono.MinguoDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author huweizhong
 * date  2023/8/16 16:07
 */
@Service
@EnableScheduling   // 1.开启定时任务
@Slf4j
@RequiredArgsConstructor
public class UploadSmsJob {

    private final SmsMapper smsMapper;

    public static String CONTENT_FORMAT = "%s|++|%s|++|%s|++|%s|++|%s|++|%s";

    public static String FILE_FORMAT = "CQT_SMS_";

    private final JschConnectionPool jschConnectionPool;

    private final RedissonUtil redissonUtil;


    private final SftpProperties sftpProperties;





    @XxlJob("UploadSmsJob")
    public void smsJob() throws InterruptedException {
        List<SmsBack> smsSdrs = smsMapper.selectList(null);
        log.info("短信表中数据长度："+smsSdrs.size());
        if (smsSdrs.size() == 0 ){
            log.info("短信表为空");
            return;
        }
        List<String> collect = smsSdrs.stream().map(SmsBack::getMsgIdentifier).collect(Collectors.toList());
        smsMapper.deleteBatchIds(collect);
        if (smsSdrs.size()>5000){
            List<List<SmsBack>> partition = Lists.partition(smsSdrs, 5000);
            for (List<SmsBack> smsBacks : partition) {
                uppload(smsBacks);
            }
            return;
        }
        uppload(smsSdrs);
    }

    private void uppload(List<SmsBack> smsSdrs) throws InterruptedException {
        int i = 1;
        List<String> strings = new ArrayList<>();
        for (SmsBack smsSdr : smsSdrs) {
            String format1 = String.format(CONTENT_FORMAT, i,smsSdr.getVirtualCalled(),smsSdr.getCalling(),smsSdr.getCalled(),smsSdr.getTimeStamp(),smsSdr.getContent());
            strings.add(format1);
            i++;
        }
        String  year = DateUtil.format(new Date(), "yyyy");
        String  monthValue = DateUtil.format(new Date(), "MM");
        String dayOfMonth = DateUtil.format(new Date(), "dd");
        //本地存放目录
        String filePath = "/home"+"/"+year+"/"+monthValue+"/"+dayOfMonth+"/";
//        String filePath = "C:\\temp\\txt";
        String date = DateUtil.format(new Date(), "yyyyMMdd");
        redissonUtil.increment(date, Duration.ofDays(2));
        String string = redissonUtil.getString(date);

        if (string.length() == 1){
            string = "_000"+ string;
        } else if (string.length() == 2) {
            string = "_00" + string;
        } else if (string.length() == 3) {
            string = "_0" + string;
        }else {
            string = "_" + string;
        }
        String fileName = FILE_FORMAT + date + string  ;
        writeDataHubData(strings, fileName+ ".txt",filePath);
        ChannelSftp channelSftp = null;
        try {
            log.info("开始上传文件");
             channelSftp = jschConnectionPool.borrowObject();
            String path = getPath(channelSftp, String.format(sftpProperties.getPath(), year, monthValue, dayOfMonth));
            //sftp服务器存放目录
            String sftpTempPath = path + fileName + ".temp";
            String sftpRealPath = path + fileName + ".txt";
            File file = new File(filePath + File.separator + fileName+ ".txt");
            @Cleanup  InputStream inputStream = Files.newInputStream(file.toPath());
            channelSftp.put(inputStream,sftpTempPath);
            channelSftp.rename(sftpTempPath,sftpRealPath);
        }catch (Exception e){
            log.error("文件上传异常："+e);
        }finally {
            jschConnectionPool.returnObject(channelSftp);
        }
    }


    private void mkdir(ChannelSftp channelSftp, String path) {
        try {
            channelSftp.stat(path);
        } catch (SftpException e) {
            try {
                channelSftp.mkdir(path);
            } catch (SftpException ex) {
                log.error("mkdir error: ", ex);
            }
        }
    }


    private String getPath(ChannelSftp channelSftp, String path) {
        try {
            channelSftp.stat(path);
        } catch (SftpException e) {
            List<String> stringList = StrUtil.split(path, StrUtil.SLASH);
            StrBuilder builder = new StrBuilder(StrUtil.SLASH);
            for (String str : stringList) {
                if (StrUtil.isEmpty(str)) {
                    continue;
                }
                builder.append(str).append(StrUtil.SLASH);
                mkdir(channelSftp, builder.toString());
            }
        }
        return path;
    }

    /**
     * 写入txt文件
     *
     */
    public static boolean writeDataHubData(List<String> result, String fileName,String filePath) {
        long start = System.currentTimeMillis();
        boolean flag = false;
        BufferedWriter out = null;
        try {
            if (result != null && !result.isEmpty() && StringUtils.isNotEmpty(fileName)) {
                File pathFile = new File(filePath);
                if (!pathFile.exists()) {
                    boolean mkdirs = pathFile.mkdirs();
                }
                String relFilePath = filePath + File.separator + fileName;
                File file = new File(relFilePath);
                if (!file.exists()) {
                    boolean newFile = file.createNewFile();
                }
                out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), "GBK"));

                for (String info : result) {

                    out.write(info);
                    out.newLine();
                }
                flag = true;
                log.info("写入文件耗时：***************" + (System.currentTimeMillis() - start) + "毫秒" + "文件长度：" + result.size());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return flag;
    }


}
