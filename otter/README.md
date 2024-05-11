监控改造基于otter版本 4.2.18，其余版本未测试，改造内容

暴露监控接口，此接口查询channel及其pipeline下的状态以及延迟时间，将异常的channel和pipeline内容返回到此接口中，延迟时间默认为超过1800s告警，可在

manager的otter.properties中的增加配置，单位为ms

```
#otter延迟时间告警，默认为半小时 单位为毫秒
otter.delayTime = 1800000
#otter最后同步时间告警，默认为12小时 单位为分钟
otter.lastSyncTime = 720
```

集成现有环境： 

进入manager目录，

cd lib 替换manager.deployer-4.2.18.jar

cd webapp/WEB-INF 替换web.xml和applicationContext.xml



最后重启manager，访问[http://ip:port/linkcircle-monitor](http://172.16.252.111:8082/linkcircle-monitor) 即可获取监控结果，例：

<http://172.16.252.111:8082/linkcircle-monitor>

正常情况

```
{"code":"0","message":"otter正常"}
异常情况
{"code":"-1","message":"channel名字为：121<->130运行状态为：挂起，请检查！\r\n"}
```