package com.cqt.agent.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.CharsetUtil;
import com.cqt.agent.model.dto.NewFileDTO;
import com.cqt.model.common.Result;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author linshiqiang
 * @date 2022/7/8 15:46
 */
@Slf4j
@RestController
@RequestMapping("file")
@Api(tags = "文件操作")
public class FileController {

    @PostMapping("uploadBatch")
    public Result uploadBatch(@RequestParam("filePath") String filePath, @RequestParam("files") MultipartFile[] files) throws IOException {
        if (!FileUtil.exist(filePath)) {
            FileUtil.mkdir(filePath);
        }
        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();
            String path = filePath + "/" + originalFilename;
            FileUtil.del(path);
            file.transferTo(FileUtil.file(path));
        }
        return Result.ok();
    }

    @PostMapping("upload")
    public Result upload(@RequestParam("file") MultipartFile file, @RequestParam("filePath") String filePath) throws IOException {
        if (!FileUtil.exist(filePath)) {
            FileUtil.mkdir(filePath);
        }
        String originalFilename = file.getOriginalFilename();
        String path = filePath + "/" + originalFilename;
        FileUtil.del(path);
        file.transferTo(FileUtil.file(path));
        return Result.ok();
    }

    @PostMapping("delete")
    public Result delete(@RequestBody List<String> fileList) throws IOException {
        for (String path : fileList) {
            boolean del = FileUtil.del(path);
            log.info("delete file: {}, result: {}", path, del);
        }
        return Result.ok();
    }

    @PostMapping("newFile")
    public Result newFile(@RequestBody NewFileDTO newFileDTO) {

        try {
            FileUtil.writeString(newFileDTO.getXml(), newFileDTO.getFilePath(), CharsetUtil.UTF_8);
        } catch (IORuntimeException e) {
            log.error("new file error: ", e);
            return Result.fail(500, "new file error: " + e.getMessage());
        }

        return Result.ok();
    }
}
