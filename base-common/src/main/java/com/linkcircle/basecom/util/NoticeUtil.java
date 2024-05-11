package com.linkcircle.basecom.util;

import cn.hutool.http.HttpRequest;
import com.linkcircle.basecom.config.ApplicationContextHolder;
import com.linkcircle.basecom.config.SmsProperties;
import com.linkcircle.basecom.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/1 16:19
 */
@Slf4j
public class NoticeUtil {
    /**
     * 发送邮件 多个,分割
     * @param mailers 邮箱地址，
     * @param subject 主题
     * @param text 邮件内容
     */
    public static void sendMail(String mailers, String subject, String text){
        sendMail(mailers,subject,text,null,null);
    }

    /**
     * 发送邮件 多个,分割
     * @param mailers 邮箱地址，
     * @param subject 主题
     * @param text 邮件内容
     * @param fileResource 附件资源
     * @param attachmentName 附件名称
     */
    public static void sendMail(String mailers, String subject, String text, Resource fileResource, String attachmentName){
        try{
            JavaMailSenderImpl mailSender;
            try{
                mailSender = ApplicationContextHolder.getBean(JavaMailSenderImpl.class);
            }catch (NoSuchBeanDefinitionException e){
                log.error("请检查是否有spring-boot-starter-mail依赖");
                throw new BusinessException("请检查是否有spring-boot-starter-mail依赖");
            }
            MailProperties mailProperties = ApplicationContextHolder.getBean(MailProperties.class);
            if(!StringUtils.hasText(mailProperties.getHost())){
                throw new BusinessException("请检查邮箱配置是否正确，参考MailProperties");
            }
            MimeMessageHelper messageHelper = new MimeMessageHelper(mailSender.createMimeMessage(),true);
            messageHelper.setFrom(mailProperties.getUsername());
            messageHelper.setTo(mailers.split(","));
            if(fileResource!=null){
                messageHelper.addAttachment(attachmentName, fileResource);
            }
            messageHelper.setSubject(subject);
            messageHelper.setText(text,true);
            mailSender.send(messageHelper.getMimeMessage());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     * 发送短信
     * @param phone 手机号
     * @param content 短信内容
     * @return
     */
    public static boolean sendSms(String phone,String content) {
        SmsProperties smsProperties = ApplicationContextHolder.getBean(SmsProperties.class);
        if(!StringUtils.hasText(smsProperties.getSmsUrl())){
            throw new BusinessException("请检查短信配置是否正确，参考SmsProperties");
        }
        String messagetag ="99";
        StringBuilder sb = new StringBuilder();
        sb.append("userid="+messagetag);
        sb.append("&account="+smsProperties.getMsgAccount());
        sb.append("&password="+smsProperties.getMsgPwd());
        sb.append("&content="+smsProperties.getMsgSign()+content);
        sb.append("&mobile="+phone);
        sb.append("&extno=106907891234");
        sb.append("&action=send");
        sb.append("&rt=json");
        String contentType = "application/x-www-form-urlencoded; charset=UTF-8";
        String str = HttpRequest.post(smsProperties.getSmsUrl())
                .header("Content-Type",contentType)
                .body(sb.toString()).timeout(10000).execute().body();
        log.info("str:{}",str);
        Map<String,String> map = JsonUtil.parseObject(str, Map.class);
        Object status = map.get("status");
        if(null != status && status.toString().equals("0")){
            return true;
        }
        return false;
    }

}
