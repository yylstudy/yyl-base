#### 引用

```
<dependency>
    <groupId>com.linkcircle</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 默认依赖

```
lk-ss   1.6
mysql-connector-java   8.0.22
mybatis-plus-boot-starter  3.5.4
hutool-all 5.8.22
```

#### 使用说明

##### 字段加解密

效果：插入数据库时，自动保存为密文，查询时自动转为明文

配置类中引入@EnableFieldEncrypt注解开启mybatis字段加密拦截器

```
@Configuration
@EnableFieldEncrypt
public class MyConfiguration {
}
```

mybatis entity中属性添加@FieldEncrypt注解

```
    /**
     * 编码
     */
    @FieldEncrypt
    private String idCard;
```



##### 字段脱敏

效果：插入数据库时，自动保存为脱敏字段

配置类中引入@EnableFieldSensitive注解开启mybatis字段脱敏拦截器

```
@Configuration
@EnableFieldSensitive
public class MyConfiguration {
}
```

mybatis entity中属性添加@FieldSensitive注解

```
    /**
     * 手机号
     */
    @FieldSensitive(strategy = CurrencySensitiveStrategy.MobilePhone.class)
    private String phone;
```





