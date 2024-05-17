package com.cqt.cloud.api.agent;

import com.cqt.model.common.NewFileDTO;
import com.cqt.model.common.Result;
import com.dtflys.forest.annotation.*;
import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnProgress;
import com.dtflys.forest.callback.OnSuccess;
import com.dtflys.forest.http.ForestResponse;

import java.io.File;
import java.util.List;

/**
 * @author linshiqiang
 * @date 2022/7/8 15:51
 * Freeswitch服务器, 接口调用客户端
 */
@Address(host = "{0}", port = "18802")
public interface AgentRemoteClient {

    /**
     * 命令执行
     *
     * @param host 设备ip
     * @param cmd  命令
     * @return 结果
     */
    @Post("/private-agent/command/execute")
    ForestResponse<String> execute(String host, @JSONBody String cmd);

    /**
     * 命令异步执行, 回调
     *
     * @param host      设备ip
     * @param cmd       命令
     * @param onSuccess 成功回调
     * @param onError   失败回调
     * @return 结果
     */
    @Post(value = "/private-agent/command/execute", async = true)
    ForestResponse<String> executeAsync(String host, @JSONBody String cmd, OnSuccess<String> onSuccess, OnError onError);

    /**
     * 命令异步执行
     *
     * @param host 设备ip
     * @param cmd  命令
     * @return 结果
     */
    @Post(value = "/private-agent/command/execute", async = true)
    ForestResponse<String> executeAsync(String host, @JSONBody String cmd);

    /**
     * 文件上传
     *
     * @param host       设备ip
     * @param file       文件
     * @param filePath   文件路径
     * @param onProgress 进度回调
     * @return 结果
     */
    @Post("/private-agent/file/upload")
    ForestResponse<Result> upload(String host, @DataFile(value = "file") File file, @Query("filePath") String filePath, OnProgress onProgress);

    /**
     * 删除服务器文件
     *
     * @param host     设备ip
     * @param fileList 文件路径
     * @return 结果
     */
    @Post(value = "/private-agent/file/delete")
    ForestResponse<Result> deleteFile(String host, @JSONBody List<String> fileList);

    /**
     * 插件文本文件
     *
     * @param host       设备ip
     * @param newFileDTO 信息
     * @return 结果
     */
    @Post("/private-agent/file/newFile")
    ForestResponse<String> newFile(String host, @JSONBody NewFileDTO newFileDTO);
}
