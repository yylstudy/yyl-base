#### 引用

```
<dependency>
    <groupId>com.linkcircle</groupId>
    <artifactId>minio-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 默认依赖

```
minio 8.0.3
```

#### 使用说明

##### 文件上传

MinioUtil

```
String upload(MultipartFile file, String dir)；
String upload(InputStream stream, String relativePath)；
```

返回文件下载地址



##### 文件下载

使用文件上传返回的下载地址或者使用MinioUtil中的

```
InputStream get(String objectName)；
InputStream get(String bucket, String objectName)；
```



##### 文件删除

```
void remove(String objectName)；
void remove(String bucket, String objectName)；
```