#### 引用

```
<dependency>
    <groupId>com.linkcircle</groupId>
    <artifactId>common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 默认依赖

```
spring-boot-starter-web  随spring boot版本
spring-boot-starter-actuator  随spring boot版本
spring-boot-starter-validation 随spring boot版本
spring-boot-starter-aop 随spring boot版本
hutool-all  5.8.22
knife4j-openapi3  4.3.0
lk-ss   1.6
mysql-connector-java   8.0.22
lombok  1.18.12
mapstruct 1.5.5.Final
```

#### 使用说明

##### 统一接口返回格式、异常

所有接口返回值均为Result，接口校验异常使用BusinessException，登录错误异常使用NoAuthException，经由GlobalExceptionHandler统一异常处理器转成Result

##### spring容器上下文

ApplicationContextHolder

##### spring mvc配置

1、日期默认格式为yyyy-MM-dd HH:mm:ss

2、增加Java8的LocalDateTime、LocalDate、LocalTime序列化和反序列化器，

3、防止雪花ID WEB失真，序列化时将所有的Long序列化为String

4、默认添加跨域过滤器

详细查看MvcConfig

##### 参数校验

@Email   邮箱参数校验
@Phone  手机号参数校验
@CheckEnum 枚举类型参数校验

```
    @NotNull(message = "手机号不能为空")
    @Phone(message = "手机号格式不正确")
    private String phone;
    @NotNull(message = "邮箱不能为空")
    @Email
    private String email;
    @CheckEnum(value = GenderEnum.class, message = "性别必须在指定范围 {value}")
    private Integer sex;
```

需要注意@CheckEnum使用@CheckEnum注解，里面的枚举必须继承BaseEnum

```
@AllArgsConstructor
@Getter
public enum GenderEnum implements BaseEnum {
    UNKNOWN(0, "未知"),
    MAN(1, "男"),
    WOMAN(2, "女");
    private final Integer code;
    private final String desc;
}
```

##### web脱敏

@WebSensitive

```
    @WebSensitive(strategy = CurrencySensitiveStrategy.MobilePhone.class)
    private String phone;
```

这样在返回json中会自动将phone脱敏，常用的脱敏策略见CurrencySensitiveStrategy

##### knife4j配置

默认配置knife4j，详看Knife4jConfig

##### 短信、邮件工具类

短信配置类SmsProperties，邮件配置类MailProperties

发送邮件，需要引入

```
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
```

```
NoticeUtil.sendMail("1594818954@qq.com","测试主题","测试内容");
NoticeUtil.sendSms("15255178553","测试短信");
```

##### 字典查询

依赖注入DictHandler（默认实现是DefaultDictHandler），提供getDictItemByDictCode，根据字典编码查询字典项，使用此功能，需要添加DataSource、RedisTemplate依赖，且数据库中存在表sys_dict、sys_dict_item

```
List<DictModel> dictModelList = dictHandler.getDictItemByDictCode(dictCode);
```

数据库脚本

```
CREATE TABLE `sys_dict` (
  `id` bigint(20) NOT NULL,
  `dict_code` varchar(50) NOT NULL COMMENT '编码',
  `dict_name` varchar(50) NOT NULL COMMENT '名称',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字段key';

CREATE TABLE `sys_dict_item` (
  `id` bigint(20) NOT NULL,
  `dict_id` bigint(20) NOT NULL,
  `item_value` varchar(50) NOT NULL COMMENT '编码',
  `item_text` varchar(50) NOT NULL COMMENT '名称',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` bigint(20) DEFAULT NULL,
  `update_by` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典的值';
```



##### excel操作类

ExcelUtil，需要引入easyexcel依赖

```
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>easyexcel</artifactId>
            <version>${easyexcel.version}</version>
        </dependency>
```

导出

```
    public void exportXlsx() {
        DownloadData downloadData = new DownloadData();
        downloadData.setSex("1");
        downloadData.setUsername("yyl");
        ExcelUtil.exportXlsx("测试","111", Arrays.asList(downloadData),DownloadData.class);
    }
```

```
@Data
public class DownloadData {
    @ColumnWidth(30)  //列宽度
    @ExcelProperty("生日")  //列名
    private Date birthday;
    @ExcelProperty(value = "性别",converter = DictConverter.class) //列名，转化器
    @ColumnWidth(20)  //列宽度
    @Dict(dictCode = "sex") //字典翻译，需要依赖DictHandler（默认是DefaultDictHandler）
    private String sex;
}
```

导入

```
    @GetMapping("read")
    public void read() throws Exception{
        FileInputStream fis = new FileInputStream(new File("C:\\Users\\yyl\\Desktop\\1.xlsx"));
        List<UploadData> list = ExcelUtil.read(fis,UploadData.class);
        System.out.println(list);
    }
```



##### 分页方法

需要引入mysql-plus依赖

分页方法示例

```
public Result<PageResult<SysConfig>> query(SysConfigQueryDTO queryForm) {
    Page<?> page = PageUtil.convert2PageQuery(queryForm);
    List<SysConfig> entityList = sysConfigMapper.queryByPage(page, queryForm);
    PageResult<SysConfig> pageResult = PageUtil.convert2PageResult(page, entityList);
    return Result.ok(pageResult);
}
```

```
public class SysConfigQueryDTO extends PageParam {
    @Schema(description = "key")
    @Size(max = 50, message = "key最多50字符")
    private String key;
}
```

##### 分页方法字典翻译

分页方法返回类型必须为Result<PageResult<?>>，分页方法上添加@AutoDict，返回属性上添加@Dict注解实现字段字典翻译，默认为原有字段加上"_dictText"字段

```
    @Dict(dictCode = "sex")
    private String sex;
    @Dict(dictCode = "sex",dictText = "sexCustomText")
    private String sexCustom;
```

翻译后，

```
{
	"sex": "1",
	"sex_dictText": "男",
	"sexCustom": "1",
	"sexCustomText": "男"
}
```

##### 自动填充默认字段

引入mybatis-plus依赖，spring配置类上添加注解@EnableAutoFillBaseEntity，

```
@Configuration
@EnableAutoFillBaseEntity
public class MyConfiguration {
}
```

mybaits entity继承BaseEntity

```
@Data
@TableName("corp")
public class Corp extends BaseEntity {
    @TableId(type = IdType.INPUT)
    private String id;
    private String name;
}
```

自动填充BaseEntity的createBy、createTime、updateBy、updateTime属性值



##### token校验和当前用户信息获取

spring配置类上添加注解@EnableJwtFilter，开启此功能，

```
@Configuration
@EnableJwtFilter(tokenHandler = SystemTokenHandler.class)
public class MyConfiguration {
}
```

注解提供方法tokenHandler，默认实现为DefaultTokenHandler，不校验token合法性，只是解析token获取用户登录信息，当前登录信息类为LoginUserInfo，



获取当前登录人信息

LoginUserInfo loginUserInfo = LoginUserInfoHolder.get();



LoginUserInfo包含属性较少，且不验证token有效性，下面自定义

```
public class SystemTokenHandler extends DefaultTokenHandler {
    @Override
    protected Result<JWT> checkAndGetJwt(HttpServletRequest request,String token) {
        if(StringUtils.isEmpty(token)){
            return Result.errorAuth("token为空");
        }
        JWT jwt = JWTUtil.parseToken(token);
        if(!jwt.setKey(JwtUtil.DEFAULT_SECRET.getBytes()).verify()) {
            return Result.errorAuth("无效的token");
        }
        if(!jwt.validate(0)) {
            return Result.errorAuth("token已过期，请重新登录");
        }
        return Result.ok(jwt);
    }
    @Override
    protected LoginUserInfo getLoginUserInfo(JWT jwt) {
        String phone = jwt.getPayloads().getStr("phone");
        String email = jwt.getPayloads().getStr("email");
        String username = jwt.getPayloads().getStr("username");
        String departId = jwt.getPayloads().getStr("departId");
        Long userId = jwt.getPayloads().getLong("id");
        String corpId = jwt.getPayloads().getStr("corpId");
        SystemLoginUserInfo systemLoginUserInfo = new SystemLoginUserInfo();
        systemLoginUserInfo.setPhone(phone);
        systemLoginUserInfo.setEmail(email);
        systemLoginUserInfo.setUsername(username);
        systemLoginUserInfo.setId(userId);
        systemLoginUserInfo.setCorpId(corpId);
        systemLoginUserInfo.setDepartId(departId);
        return systemLoginUserInfo;
    }
}
```

```
@Data
public class SystemLoginUserInfo extends LoginUserInfo {
    /**
     * 邮箱
     */
    private String email;
    /**
     * 当前登录企业
     */
    private String corpId;
    /**
     * 部门ID
     */
    private String departId;
}
```



```
public class SystemLoginUserInfoHolder {
    /**
     * 获取登录信息
     * @return
     */
    public static SystemLoginUserInfo getLoginUserInfo(){
        return LoginUserInfoHolder.get();
    }
    /**
     * 获取企业ID
     * @return
     */
    public static String getCorpId(){
        SystemLoginUserInfo loginUserInfo = getLoginUserInfo();
        if(loginUserInfo!=null){
            return loginUserInfo.getCorpId();
        }
        return "";
    }
}
```

//获取企业ID

String corpId = SystemLoginUserInfoHolder.getCorpId();

//获取当前登录人信息

SystemLoginUserInfo userInfo = SystemLoginUserInfoHolder.get();



##### 用户操作日志

需要使用到数据库表

```
CREATE TABLE `sys_operate_log` (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名称',
  `content` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '操作内容',
  `url` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求路径',
  `method` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求方法',
  `param` text COLLATE utf8mb4_unicode_ci COMMENT '请求参数',
  `cost_time` int(10) DEFAULT NULL COMMENT '请求耗时',
  `ip` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '请求ip',
  `success_flag` tinyint(4) DEFAULT NULL COMMENT '请求结果 0失败 1成功',
  `fail_reason` longtext COLLATE utf8mb4_unicode_ci COMMENT '失败原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作记录';
```

spring配置类上添加注解@EnableOperatelog，开启此功能

```
@Configuration
@EnableOperatelog
public class MyConfiguration {
}
```

在操作方法上添加@OperateLog注解，content方法支持spel表达式

```
@PostMapping("add")
@OperateLog(content = "新增企业#{#dto.name}")
public Result<String> add(@Valid @RequestBody CorpAddDTO dto) {
    return corpService.add(dto);
}
```

会保存操作日志到表sys_operate_log，其中的user_id、	username字段依赖前面的

LoginUserInfo loginUserInfo = LoginUserInfoHolder.get();



EnableOperatelog注解operateLogService方法默认是DefaultOperateLogHandler

可通过继承DefaultOperateLogHandler重写方法实现自定义的操作日志保存

##### 方法签名校验

spring配置类上添加注解@EnableSignCheck，开启此功能

```
@Configuration
@EnableSignCheck
public class MyConfiguration {
}
```

需要验签的方法上添加@SignCheck注解，实现方法的签名校验

```
@SignCheck
public String get(){
    MyLoginUserInfo myLoginUserInfo = LoginUserInfoHolder.get();
    log.info("myLoginUserInfo:{}",myLoginUserInfo);
    return "success";
}
```

@EnableSignCheck注解signHandler方法可以自定义签名处理器，默认是DefaultSignHandler，签名处理器有两个方法，

checkAppKey：校验appKey是否合法，DefaultSignHandler不校验

sign：签名方式，DefaultSignHandler使用HmacSHA256加密算法加密



#### Future

```
web层脱敏
数据库层脱敏
minio
多数据源
分库分表
读写分离
license控制
日志链路追踪
```

