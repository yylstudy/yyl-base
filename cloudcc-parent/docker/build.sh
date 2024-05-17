#!/bin/bash

harbor='58.220.49.186:11000'
versions='v6.0.0'
project='cloudcc'

# build
buildApp(){

   cd /home/springboot
   # 传入的包名称
   local service_name=$1;
   # 构建镜像
   docker build -f ./Dockerfile \
      --build-arg APP_NAME="${service_name}" \
      -t ${service_name}:${versions} .
   echo "====================docker build image ${service_name}:${versions} finish===================="

   # 打标签, 推送镜像到私服
   docker tag ${service_name}:${versions} ${harbor}/${project}/${service_name}:${versions}
   echo docker tag ${service_name}:${versions} ${harbor}/${project}/${service_name}:${versions}
   echo "====================tag image ${service_name}:${versions} finish===================="

   docker push ${harbor}/${project}/${service_name}:${versions}

   echo "====================push image to harbor ${service_name}:${versions} finish===================="
   echo docker push ${harbor}/${project}/${service_name}:${versions}
}

apps=($@)
for i in ${apps[@]}
do
    buildApp $i
done

# sh build.sh cloudcc-sdk-interface cloudcc-call-control cloudcc-client-server
