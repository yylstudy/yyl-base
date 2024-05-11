#### 引用

```
<dependency>
    <groupId>com.linkcircle</groupId>
    <artifactId>dynamic-datasource-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

多数据源配置

```yaml
spring:
  datasource:
    dynamic:
      enabled: false   
      datasource:
        master:
          poolName: master
          type: com.linkcircle.ss.LHikariDataSource
          driverClassName: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://172.16.252.130:3306/test?characterEncoding=UTF-8&useSSL=true&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true&allowMultiQueries=true&nullCatalogMeansCurrent=true
          username: root
          password: RHhiT97UsukJpKyuqpPG1JhrfVH0q/qeRL5uOjC0V3E54yPn+WmVxu90p7/6lzrU6rTuOpDeRdcZ8aYqEcqvYA==
      primary: master
      hikari:
        maximum-pool-size: 10
        minimum-idle: 1
        connection-test-query: 'select 1'
        validation-timeout: 1000
        max-lifetime: 900000
        idle-timeout: 30000
        connection-timeout: 30000
```

多数据源默认加载表sys_tenant_db中的数据源，

```sql
CREATE TABLE `sys_tenant_db` (
  `tenant_id` varchar(32) NOT NULL,
  `db_user` varchar(255) DEFAULT NULL,
  `db_password` varchar(255) DEFAULT NULL,
  `db_host` varchar(255) DEFAULT NULL,
  `db_port` varchar(5) DEFAULT NULL,
  `db_database` varchar(90) DEFAULT NULL,
  `status` varchar(1) DEFAULT NULL,
  PRIMARY KEY (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户数据库表';
```

若是想要变更，实现JdbcDataSourceFactory并声明为spring的bean即可

方法

```
DynamicDatasourceUtil.doExecute()
```

可实现指定数据源调用



也可在http请求头中实现自切换，如下是过滤器的实现

```java
package com.linkcircle.act.filter;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.linkcircle.act.filter.loginInfo.LoginInfoVo;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2023/6/2 17:13
 */
public class DynamicDatasourceFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String tenantId = LoginInfoVo.getTenantId();
        if(!StringUtils.hasText(tenantId)){
            tenantId = request.getHeader("tenantId");
        }
        try{
            if(StringUtils.hasText(tenantId)){
                DynamicDataSourceContextHolder.push(tenantId);
            }
            filterChain.doFilter(request, response);
        }finally {
            if(StringUtils.hasText(tenantId)){
                DynamicDataSourceContextHolder.clear();
            }
        }

    }
}

```

