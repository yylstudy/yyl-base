package com.linkcircle.minio.util;

import com.linkcircle.minio.config.MinioProperties;
import com.linkcircle.minio.constant.MinioConstant;
import io.minio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/12 17:43
 */
@Service
public class MinioUtil {

    private Logger log = LoggerFactory.getLogger(MinioUtil.class);

    private MinioProperties minioProperties;

    private MinioClient minioClient;

    private String minioUrl;
    private String downloadUrl;

    public MinioUtil(MinioProperties minioProperties){
        this.minioProperties = minioProperties;
        minioClient = MinioClient.builder()
                .endpoint(minioProperties.getUrl())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
        minioUrl = minioProperties.getUrl();
        if(!minioUrl.endsWith(MinioConstant.DELIMITER)){
            minioUrl+=MinioConstant.DELIMITER;
        }
        downloadUrl = minioProperties.getDownloadUrl();
        if(!downloadUrl.endsWith(MinioConstant.DELIMITER)){
            downloadUrl+=MinioConstant.DELIMITER;
        }
    }


    /**
     * 文件上传
     * @param file 文件
     * @param dir bucket下的目录，可以为空
     * @return
     */
    public String upload(MultipartFile file, String dir){
        return upload(file,dir,null);
    }

    /**
     * 文件上传
     * @param stream 流
     * @param relativePath 文件相对路径，不能为空
     * @return
     */
    public String upload(InputStream stream, String relativePath){
        return upload(stream,relativePath,null);
    }

    /**
     * 上传文件
     * @return
     */
    public String upload(MultipartFile file, String dir, String bucket){
        try {
            bucket = checkAndGetBucket(bucket);
            InputStream stream = file.getInputStream();
            String orgName = file.getOriginalFilename();
            if("".equals(orgName)){
                orgName=file.getName();
            }
            String objectName = "";
            orgName = FileUtil.getFileName(orgName);
            if(StringUtils.hasText(dir)){
                objectName+=dir;
            }
            objectName += MinioConstant.DELIMITER
                    +( orgName.indexOf(".")==-1
                    ?orgName + "_" + System.currentTimeMillis()
                    :orgName.substring(0, orgName.lastIndexOf(".")) + "_" + System.currentTimeMillis()
                    + orgName.substring(orgName.lastIndexOf("."))
            );
            if(objectName.startsWith(MinioConstant.DELIMITER)){
                objectName = objectName.substring(1);
            }
            PutObjectArgs objectArgs = PutObjectArgs.builder().object(objectName)
                    .bucket(bucket)
                    .contentType("application/octet-stream")
                    .stream(stream,stream.available(),-1).build();
            minioClient.putObject(objectArgs);
            stream.close();
            return downloadUrl+bucket+MinioConstant.DELIMITER+objectName;
        }catch (Exception e){
            throw new RuntimeException("文件上传失败",e);
        }
    }

    /**
     * 文件上传到minio
     * @param stream 文件流
     * @param relativePath 路径
     * @param bucket bucket，可为空，为空取值MinioProperties中的bucket
     * @return
     * @throws Exception
     */
    public String upload(InputStream stream,String relativePath,String bucket) {
        if(!StringUtils.hasText(relativePath)){
            throw new RuntimeException("relativePath为空，请填写minio文件上传的地址");
        }
        try{
            bucket = checkAndGetBucket(bucket);
            PutObjectArgs objectArgs = PutObjectArgs.builder().object(relativePath)
                    .bucket(bucket)
                    .contentType("application/octet-stream")
                    .stream(stream,stream.available(),-1).build();
            minioClient.putObject(objectArgs);
            stream.close();
            return downloadUrl+bucket+MinioConstant.DELIMITER+relativePath;
        }catch (Exception e){
            throw new RuntimeException("文件上传失败",e);
        }
    }
    /**
     * 获取文件
     * @param objectName 对象名称
     */
    public InputStream get(String objectName) {
        return get(null,objectName);
    }

    /**
     * 获取文件
     * @param bucket
     * @param objectName 对象名称
     */
    public InputStream get(String bucket, String objectName) {
        try {
            bucket = checkAndGetBucket(bucket);
            GetObjectArgs objectArgs = GetObjectArgs.builder().object(objectName)
                    .bucket(bucket).build();
            return minioClient.getObject(objectArgs);
        }catch (Exception e){
            throw new RuntimeException("获取文件失败",e);
        }
    }

    /**
     * 删除文件
     * @param objectName
     */
    public void remove(String objectName){
        remove(null,objectName);
    }
    /**
     * 删除文件
     * @param bucket
     * @param objectName 对象名称
     * @throws Exception
     */
    public void remove(String bucket, String objectName) {
        try {
            bucket = checkAndGetBucket(bucket);
            RemoveObjectArgs objectArgs = RemoveObjectArgs.builder().object(objectName)
                    .bucket(bucket).build();
            minioClient.removeObject(objectArgs);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    /**
     * 校验和获取bucket
     * @param bucket
     * @return
     * @throws Exception
     */
    private String checkAndGetBucket(String bucket) throws Exception{
        if(!StringUtils.hasText(bucket)){
            bucket = minioProperties.getBucket();
        }
        // 检查存储桶是否已经存在
        if(!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            throw new RuntimeException(String.format("bucket%s不存在",bucket));
        }
        return bucket;
    }

    public MinioProperties getMinioProperties() {
        return minioProperties;
    }

    public MinioClient getMinioClient() {
        return minioClient;
    }
}
