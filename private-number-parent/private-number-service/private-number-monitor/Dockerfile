FROM java:8

VOLUME /tmp

ADD private-monitor.jar /monitor.jar

RUN bash -c 'touch /monitor.jar'

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","monitor.jar"]