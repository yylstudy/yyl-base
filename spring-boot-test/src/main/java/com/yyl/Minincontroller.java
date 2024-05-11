package com.yyl;

import com.linkcircle.minio.util.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/12 18:26
 */
@RestController
@Slf4j
public class Minincontroller {
    @Autowired
    private MinioUtil minioUtil;
    @RequestMapping("upload")
    public String upload() throws Exception{
        FileInputStream fis = new FileInputStream(new File("C:\\Users\\yyl\\Desktop\\1.png"));
        String path = minioUtil.upload(fis,"1.png");
        log.info("path1:{}",path);
        return "success";
    }
    @RequestMapping("MultipartFile")
    public String MultipartFile(MultipartFile file) throws Exception{
        String path = minioUtil.upload(file,"xxxx");
        log.info("path1:{}",path);
        return "success";
    }
    @RequestMapping("delete")
    public String delete() throws Exception{
        minioUtil.remove("1.png");
        return "success";
    }
}
