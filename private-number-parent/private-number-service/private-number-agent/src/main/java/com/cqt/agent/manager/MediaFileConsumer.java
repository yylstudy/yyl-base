package com.cqt.agent.manager;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.agent.model.dto.FileSyncMqDTO;
import com.cqt.common.enums.OperateTypeEnum;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * @author linshiqiang
 * @date 2022/5/16 14:20
 * 消费媒体文件广播消息
 */
@Component
@Slf4j
public class MediaFileConsumer {

    @RabbitListener(queues = "#{mediaFileQueue.name}")
    @RabbitHandler
    public void onMessage(Message msg, Channel channel) throws IOException {
        long deliveryTag = msg.getMessageProperties().getDeliveryTag();

        try {
            FileSyncMqDTO fileSyncMqDTO = JSON.parseObject(new String(msg.getBody()), FileSyncMqDTO.class);
            if (OperateTypeEnum.INSERT.name().equals(fileSyncMqDTO.getOperateType())) {
                byte[] fileContent = fileSyncMqDTO.getFileContent();
                File file = FileUtil.writeBytes(fileContent, fileSyncMqDTO.getFilePath());
                log.info("新增文件: {}", file.getPath());
            }
            if (OperateTypeEnum.DELETE.name().equals(fileSyncMqDTO.getOperateType())) {

                // 备份文件
                String fsBackFilePath = fileSyncMqDTO.getFsBackFilePath();
                FileUtils.copyFile(FileUtil.file(fileSyncMqDTO.getFilePath()), FileUtil.file(fsBackFilePath));
                log.info("备份文件: {}", fsBackFilePath);

                boolean del = FileUtil.del(fileSyncMqDTO.getFilePath());
                log.info("删除文件: {}, {}", fileSyncMqDTO.getFilePath(), del);
            }
        } catch (IOException e) {
            log.error("消费媒体文件广播消息异常: ", e);
        } finally {
            channel.basicAck(deliveryTag, true);
        }
    }

    public static void main(String[] args) throws IOException {
        File dest = FileUtil.file("D:\\home\\freeswitch-bak1\\video\\1010\\ceshisss2ss2.mp4");
        FileUtils.copyFile(FileUtil.file("D:\\home\\freeswitch\\video\\1010\\ceshisssss.mp4"),
                dest);
    }
}
