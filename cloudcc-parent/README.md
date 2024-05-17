# 项目结构

- cloudcc-parent  父工程
  - [db](db)
  - [doc](doc)
  - [docker](docker)
  - [style](style)
  - [cloudcc-common](cloudcc-common)  通用模块
    - [cloudcc-model](cloudcc-common%2Fcloudcc-model)   通用实体类
    - [cloudcc-base](cloudcc-common%2Fcloudcc-base)    通用类 util, 枚举, 常量...
    - [cloudcc-mapper](cloudcc-common%2Fcloudcc-mapper)  通用mapper
    - [cloudcc-manager](cloudcc-common%2Fcloudcc-manager)
  - [cloudcc-api](cloudcc-api) 接口定义
    - [cloudcc-rpc-api](cloudcc-api%2Fcloudcc-rpc-api)  rpc接口定义
    - [cloudcc-feign-api](cloudcc-api%2Fcloudcc-feign-api) feign接口定义
  - [cloudcc-service](cloudcc-service)  具体服务
    - [cloudcc-client-server](cloudcc-service%2Fcloudcc-client-server)  前端SDK对接服务-netty
    - [cloudcc-call-control](cloudcc-service%2Fcloudcc-call-control) 话务控制-对接底层接口
    - [cloudcc-queue-control](cloudcc-service%2Fcloudcc-queue-control) 排队控制-呼入排队
    - [cloudcc-sdk-interface](cloudcc-service%2Fcloudcc-sdk-interface)  后端SDK-坐席状态
    - [cloudcc-cdr-inside](cloudcc-service%2Fcloudcc-cdr-inside) 内部话单
    - [cloudcc-cdr-outside](cloudcc-service%2Fcloudcc-cdr-outside) 外部话单
    - [cloudcc-sf-aftersales](cloudcc-service%2Fcloudcc-sf-aftersales)
    - [cloudcc-ivr-fsdtb](cloudcc-service%2Fcloudcc-ivr-fsdtb) ivr文件生效
  - [cloudcc-starter](cloudcc-starter) 通用starter
    - [spring-boot-starter-dynamic-mybatis-plus](cloudcc-starter%2Fspring-boot-starter-dynamic-mybatis-plus)
    - [spring-boot-starter-redis](cloudcc-starter%2Fspring-boot-starter-redis)  redisson客户端
    - [spring-boot-starter-docs](cloudcc-starter%2Fspring-boot-starter-docs) 在线接口文档
    - [spring-boot-starter-rabbitmq](cloudcc-starter%2Fspring-boot-starter-rabbitmq)
    - [spring-boot-starter-sharding-jdbc](cloudcc-starter%2Fspring-boot-starter-sharding-jdbc) Mybatis PLus +
      Sharding-Jdbc分库分表
    - [spring-boot-starter-xxljob](cloudcc-starter%2Fspring-boot-starter-xxljob)

# 代码规范建议
## 代码格式检测插件
[chechstyle插件idea安装](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea)

1. `Preferences/Settings --> Other Settings --> Checkstyle` 或者 `Preferences/Settings --> Tools --> Checkstyle`
2. 在checkstyle插件中设置checkstyle版本至少为8.30,并将扫描作用域设置为`All resource(including tests)`
3. 导入源代码下`style/NacosCheckStyle.xml`文件到checkstyle插件。
4. 用checkstyle插件扫描你修改的代码。

## 代码规范

# 自定义starter组件集成

## redis

> 客户端为redisson, 已集成双机房双写

### 依赖引入

```xml

<dependency>
  <groupId>com.cqt</groupId>
  <artifactId>spring-boot-starter-redis</artifactId>
</dependency>
```
### yaml配置
```yaml
spring:
  redis:
    password: cqt@1234
    masterConnectionPoolSize: 200
    cluster:
      nodes:
        - 172.16.251.74:9100
        - 172.16.251.75:9100
        - 172.16.251.76:9100
      max-redirects: 5
      location: A
    cluster2:
      # 激活异地机房
      active: true
      nodes:
        - 172.16.251.87:9100
        - 172.16.251.88:9100
        - 172.16.251.89:9100
      max-redirects: 5
      location: B
    timeout: 10000
    database: 0
    lettuce:
      pool:
        maxIdle: 8
        minIdle: 0
        maxActive: 8
        timeBetweenEvictionRuns: 600s
```

### 使用

```java
    private final RedissonUtil redissonUtil;
```

## mybatis-plus + dynamic多数据源 + 动态表名

### 依赖引入

```xml

<dependency>
  <groupId>com.cqt</groupId>
  <artifactId>spring-boot-starter-dynamic-mybatis-plus</artifactId>
</dependency>
```

### yaml

```yaml
dynamic:
  table-rule:
    # 逻辑表名(不带后缀) : 真实表名模板
    cc_agent_status_log: cc_agent_status_log_{company_code}_{month}

spring:
  jackson:
    default-property-inclusion: non_null
  datasource:
    dynamic:
      primary: ms
      strict: true
      datasource:
        cdr:
          url: jdbc:mysql://172.16.250.234:3300/cloudcc_cdr?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true
          username: root
          password: cqt@fj889977
          type: com.zaxxer.hikari.HikariDataSource
        ms:
          url: jdbc:mysql://172.16.250.234:3300/cloudcc_ms?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true
          username: root
          password: cqt@fj889977
          type: com.zaxxer.hikari.HikariDataSource
      hikari:
        min-idle: 10
        max-pool-size: 100
        idle-timeout: 60000
        max-lifetime: 600000
        connection-timeout: 3000
        connection-test-query: SELECT 1 FROM DUAL
        validation-timeout: 10000
```

### 操作数据库demo

```java
// 写在mapper接口上
@DS("cdr")  
```

```java
public class Demo {
  public void test() {
    RequestDataHelper.setRequestData(CommonConstant.COMPANY_CODE_KEY, extStatusLog.getCompanyCode());
    RequestDataHelper.setRequestData(CommonConstant.MONTH_KEY, month);
    try {
      // mapper接口执行

    } catch (Exception e) {
      log.error("db异常: ", e);
    } finally {
      RequestDataHelper.remove();
    }
  }
}
```

## sharding-jdbc+分表

### 依赖引入

```xml

<dependency>
  <groupId>com.cqt</groupId>
  <artifactId>spring-boot-starter-sharding-jdbc</artifactId>
</dependency>
```

### yaml配置

```yaml
spring:
  shardingsphere:
    datasource: ##数据源配置
      default-data-source-name: ds0
      names: ds0,ds1
      ds0:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.jc.jdbc.Driver
        jdbc-url: jdbc:mysql://172.16.250.109:3300/cloudcc_refactor?useSSL=false&rewriteBatchedStatements=true
        username: root
        password: C@hjzx531lqsym
        pool-name: ds0-pool
        maximum-pool-size: 100
        minimum-idle: 30
        max-lifetime: 600000
        connection-timeout: 3000
        connection-test-query: SELECT 1 FROM DUAL
        validation-timeout: 10000
      ds1:
        type: com.zaxxer.hikari.HikariDataSource
        driver-class-name: com.mysql.jc.jdbc.Driver
        jdbc-url: jdbc:mysql://172.16.250.109:3300/cloudcc_cdr?useSSL=false&rewriteBatchedStatements=true
        username: root
        password: C@hjzx531lqsym
        pool-name: ds1-pool
        maximum-pool-size: 100
        minimum-idle: 30
        max-lifetime: 600000
        connection-timeout: 3000
        connection-test-query: SELECT 1 FROM DUAL
        validation-timeout: 10000
    props:
      sql-show: true
      sql-simple: true
    rules:
      sharding:
        tables:
          cc_ext_status:
            actual-data-nodes: ds1.cc_ext_status
            table-strategy:
              hint:
                sharding-algorithm-name: cc_ext_status-table-hint
        sharding-algorithms:
          # 指定分配字段值
          cc_ext_status-table-hint:
            # 策略类型 org.apache.shardingsphere.sharding.algorithm.sharding.classbased.ClassBasedShardingAlgorithm
            type: CLASS_BASED
            props:
              # 分片策略类型: STANDARD, COMPLEX, HINT
              strategy: HINT
              # 自定义分片算法
              algorithmClassName: com.cqt.starter.sharding.algorithms.MyHintAlgorithms

```

### 使用

> 直接操作mapper

## RocketMQ

### 依赖

```xml

<dependency>
  <groupId>org.apache.rocketmq</groupId>
  <artifactId>rocketmq-spring-boot-starter</artifactId>
  <exclusions>
    <exclusion>
      <artifactId>rocketmq-acl</artifactId>
      <groupId>org.apache.rocketmq</groupId>
    </exclusion>
    <exclusion>
      <artifactId>rocketmq-client</artifactId>
      <groupId>org.apache.rocketmq</groupId>
    </exclusion>
  </exclusions>
</dependency>

<dependency>
<groupId>org.apache.rocketmq</groupId>
<artifactId>rocketmq-client</artifactId>
<exclusions>
  <exclusion>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
  </exclusion>
</exclusions>
</dependency>

<dependency>
<groupId>org.apache.rocketmq</groupId>
<artifactId>rocketmq-acl</artifactId>
</dependency>
```

### yaml配置

```yaml
rocketmq:
  name-server: 172.16.251.52:9876
  consumer:
    # 按实际情况修改group
    group: call-control
    # 一次拉取消息最大值，注意是拉取消息的最大值而非消费最大值
    pull-batch-size: 100
  producer:
    # 发送同一类消息的设置为同一个group，保证唯一
    # 按实际情况修改group
    group: call-control
    # 发送消息超时时间，默认3000
    send-message-timeout: 3000
    # 发送消息失败重试次数，默认2
    retry-times-when-send-failed: 2
    # 异步消息重试此处，默认2
    retry-times-when-send-async-failed: 2
    # 消息最大长度，默认1024 * 1024 * 4(默认4M)
    max-message-size: 4194304
    # 压缩消息阈值，默认4k(1024 * 4)
    compress-message-body-threshold: 4096
    # 是否在内部发送失败时重试另一个broker，默认false
    retry-next-server: true
```

### 简单使用

发

```java
private final RocketMQTemplate rocketMQTemplate;
```

收

```java
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "hjzx-event-topic",
        consumerGroup = "call-control-group")
public class EventMessageConsumer implements RocketMQListener<String> {

    @Override
    public void onMessage(String message) {
       
    }
}
```



# docker 部署

## 构建镜像

```shell
# 一次性构建全部
sh build.sh all

# 指定包构建 build + 具体包名
build.sh build cloudcc-call-control
```

## 服务器准备

```shell
#!/bin/bash
# 查找Docker-CE的版本:
# yum list docker-ce.x86_64 --showduplicates | sort -r

# 准备 https://developer.aliyun.com/mirror/docker-ce
yum install -y yum-utils device-mapper-persistent-data lvm2
yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
sudo sed -i 's+download.docker.com+mirrors.aliyun.com/docker-ce+' /etc/yum.repos.d/docker-ce.repo
sudo yum makecache fast

# 安装docker
sudo yum -y install docker-ce
systemctl start docker.service
systemctl enable docker.service

# docker数据目录 Docker Root Dir迁移到home下, 默认是在 /var/lib/docker
docker info
systemctl stop docker
mkdir -p /home/docker
mv /var/lib/docker /home/docker
ln -s /home/docker /var/lib/docker
systemctl start docker
docker info
docker version

# 安装docker-compose
# https://github.com/docker/compose/releases/tag/v2.19.1
curl -L http://58.220.49.186:9999/docker/docker-compose-linux-x86_64 > /usr/local/bin/docker-compose
chmod 755 /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version

# 添加host
echo 58.220.49.186 harbor.cqt.com >> /etc/hosts

# 配置
mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
	"registry-mirrors": ["https://b9pmyelo.mirror.aliyuncs.com"],
	"insecure-registries" : [ "58.220.49.186:11000" ]
}
EOF

# 重启
systemctl restart docker

# 服务器登录harbor
docker login -u admin --password cqt@1234 http://58.220.49.186:11000

```

## 部署

```shell
# 拉取镜像
docker-compose pull

# 进入docker-compose.yml文件所在目录
docker-compose up -d

# 重建某个服务容器
docker-compose up --detach --build cloudcc-call-control

# 启停
docker stop/start/restart cloudcc-call-control

# 查看日志
docker logs -f cloudcc-call-control

# 查看进程
docker ps

```

# maven build

```shell
mvn clean -DskipTests=true package -am -pl cloudcc-service/cloudcc-call-control,cloudcc-service/cloudcc-sdk-interface,cloudcc-service/cloudcc-client-server,
```

```shell
mvn clean -DskipTests=true package -am -pl cloudcc-service/cloudcc-call-control,cloudcc-service/cloudcc-queue-control
```

```shell
mvn clean -DskipTests=true package -am -pl cloudcc-service/cloudcc-call-control,cloudcc-service/cloudcc-sdk-interface
```

```shell
mvn clean -DskipTests=true package -am -pl cloudcc-service/cloudcc-queue-control,cloudcc-service/cloudcc-sdk-interface
```

```shell
mvn clean -DskipTests=true package -am -pl cloudcc-service/cloudcc-queue-control
```

```shell
mvn clean -DskipTests=true package -am -pl cloudcc-service/cloudcc-call-control
```

```shell
mvn clean -DskipTests=true package -am -pl cloudcc-service/cloudcc-sdk-interface
```
