#! /bin/sh
. $HOME/.bash_profile
APP_NAME=f-sbc-sip-authentication
JAR_PATH=/home/springboot/$APP_NAME/$APP_NAME\.jar
JAVA_VM='-Xms2g -Xmx2g -Xmn1g'
JAVA_ENV='-DNACOS_SERVER=172.16.251.77:8848 -DNACOS_USERNAME=cqt -DNACOS_PASSWORD=cqt!010@Nacos'

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
