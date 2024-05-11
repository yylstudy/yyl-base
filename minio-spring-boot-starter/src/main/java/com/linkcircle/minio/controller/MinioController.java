package com.linkcircle.minio.controller;

import com.linkcircle.minio.constant.MinioConstant;
import com.linkcircle.minio.util.MinioUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/15 10:07
 */
@RestController
public class MinioController {
    @Autowired
    private MinioUtil minioUtil;
    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    private static Logger log = LoggerFactory.getLogger(MinioController.class);

    @RequestMapping("/minioDownload/{bucket}/**")
    public void download(@PathVariable("bucket") String bucket, HttpServletRequest request, HttpServletResponse response){
        String uri = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        // 获取映射的路径
        String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String objectName = antPathMatcher.extractPathWithinPattern(pattern,uri);
        InputStream is = null;
        try{
            objectName = URLDecoder.decode(objectName,"utf-8");
            is = minioUtil.get(bucket,objectName);
            log.info("minio bucket:{}, object:{}",bucket,objectName);
            String filename = objectName;
            if(filename.contains(MinioConstant.DELIMITER)){
                filename = filename.substring(filename.lastIndexOf(MinioConstant.DELIMITER));
            }
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            byte[] bytes = new byte[1024];
            int len;
            while ((len = is.read(bytes)) > 0) {
                outputStream.write(bytes, 0, len);
            }
            outputStream.close();
        }catch (Exception e){
            throw new RuntimeException("文件下载失败",e);
        }finally {
            if(is!= null){
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
        }
    }
}
