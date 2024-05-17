package com.cqt.agent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author linshiqiang
 * @date 2022/5/17 17:14
 * 文件信息发送mq广播报文
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileSyncMqDTO implements Serializable {

    private static final long serialVersionUID = -4171300018121346627L;

    /**
     * 企业id
     */
    private String vccId;

    /**
     * 操作类型 上传, 删除
     */
    private String operateType;

    /**
     * 文件类型: video, voice, lua
     */
    private String fileType;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件byte数组
     */
    private byte[] fileContent;

    /**
     * 文件路径 /home/xxx
     */
    private String filePath;

    /**
     * 备份目录
     */
    private String fsBackFilePath;
}
