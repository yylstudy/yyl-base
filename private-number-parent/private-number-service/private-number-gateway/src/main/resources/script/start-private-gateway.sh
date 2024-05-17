#! /bin/sh
. $HOME/.bash_profile
APP_NAME=private-number-gateway
JAR_PATH=/home/springboot/private-number-gateway/$APP_NAME\.jar
JAVA_VM='-Xms2g -Xmx2g -Xmn=1g'
JAVA_ENV='-Dspring.cloud.nacos.server-addr=172.31.250.28:8848,172.31.250.29:8848,172.31.250.30:8848 -Dspring.cloud.nacos.discovery.group=iccp-a'

A=`ps -ef|grep $JAR_PATH |grep -v grep|grep -v gcc  | wc -l`
echo $A
if [ $A == 0  ];then
        sleep 1
        echo $JAR_PATH " not run!"

        nohup java -jar $JAVA_VM $JAVA_ENV $JAR_PATH >/dev/null 2>/dev/null &

        echo $JAR_PATH " start ok"
		date >> /home/shellJob/logs/start.log
		echo $JAR_PATH " ok" >> /home/shellJob/logs/start.log
else
        echo $JAR_PATH " are running, do nothing!"
fi

