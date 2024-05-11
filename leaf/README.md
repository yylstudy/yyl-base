修改美团的leaf配置到nacos中，github地址为https://github.com/Meituan-Dianping/Leaf.git   
使用说明：  
nacos上新建配置	leaf-server.properties，配置内容如下:   
leaf.name=com.sankuai.leaf.opensource.test   
leaf.segment.enable=true   
leaf.jdbc.url=jdbc:mysql://172.16.252.121:3306/water-boot?useUnicode=true&characterEncoding=utf-8&useSSL=false   
leaf.jdbc.username=root   
leaf.jdbc.password=cqt@1234   
leaf.snowflake.enable=false   
#leaf.snowflake.zk.address=   
#leaf.snowflake.port=   
logLevel=debug   
logging.config=classpath:logback-level.xml   

新建项目 引入此项目依赖，配置nacos相关配置，即可通过feign或ribbon调用获取分布式主键   
具体接口查看LeafController   


改造点：   
1.0.1：leaf.properties外部化配置，从nacos config中获取      
1.0.2：雪花模式下，只在启动的时候查询或创建zk持续顺序节点，获取workId，获取完成后不依赖zk，   
用于目前双机房下只有一个zk集群的情况下，两个机房的leaf服务都注册到同一个zk集群     
