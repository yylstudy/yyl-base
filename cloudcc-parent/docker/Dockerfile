FROM openjdk:11-jdk-slim

# 维护者信息
MAINTAINER cqt
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

RUN echo "" > /etc/apt/sources.list \
    && echo "deb http://mirrors.aliyun.com/debian/ bullseye main non-free contrib" | tee -a /etc/apt/sources.list \
	&& echo "deb-src http://mirrors.aliyun.com/debian/ bullseye main non-free contrib" | tee -a /etc/apt/sources.list \
	&& echo "deb http://mirrors.aliyun.com/debian-security/ bullseye-security main" | tee -a /etc/apt/sources.list \
	&& echo "deb-src http://mirrors.aliyun.com/debian-security/ bullseye-security main" | tee -a /etc/apt/sources.list \
	&& echo "deb http://mirrors.aliyun.com/debian/ bullseye-updates main non-free contrib" | tee -a /etc/apt/sources.list \
	&& echo "deb-src http://mirrors.aliyun.com/debian/ bullseye-updates main non-free contrib" | tee -a /etc/apt/sources.list \
	&& echo "deb http://mirrors.aliyun.com/debian/ bullseye-backports main non-free contrib" | tee -a /etc/apt/sources.list \
	&& echo "deb-src http://mirrors.aliyun.com/debian/ bullseye-backports main non-free contrib" | tee -a /etc/apt/sources.list

RUN apt-get update && apt-get install -y curl

# 构建镜像时传参数据
ARG APP_NAME
#ARG APP_PORT
ARG JAVA_OPTS

# 设置环境变量
ENV APP_NAME ${APP_NAME}
ENV APP_JAR ${APP_NAME}.jar
ENV APP_PORT ${APP_PORT}
#ENV JAVA_OPTS ${JAVA_OPTS}

# 添加jar包到容器中
ADD ${APP_JAR} /home/app/${APP_JAR}

# 对外暴漏的端口号
#EXPOSE ${APP_PORT}

CMD java -jar ${JAVA_OPTS} /home/app/${APP_JAR}
