#!/bin/bash
# 查找Docker-CE的版本:
# yum list docker-ce.x86_64 --showduplicates | sort -r

# 准备 https://developer.aliyun.com/mirror/docker-ce
yum install -y yum-utils device-mapper-persistent-data lvm2
yum-config-manager --add-repo https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
sudo sed -i 's+download.docker.com+mirrors.aliyun.com/docker-ce+' /etc/yum.repos.d/docker-ce.repo
sudo yum makecache fast

# 安装docker
sudo yum -y install docker-ce-3:24.0.6-1.el7
systemctl start docker.service
systemctl enable docker.service

# docker数据目录 Docker Root Dir迁移到home下, 默认是在 /var/lib/docker
docker info
systemctl stop docker
mkdir -p /home/docker
mv /var/lib/docker /home/docker
ln -s /home/docker /var/lib/docker
systemctl start docker
docker info
docker version

# 安装docker-compose
# https://github.com/docker/compose/releases/tag/v2.19.1
curl -L http://58.220.49.186:9999/docker/docker-compose-linux-x86_64 > /usr/local/bin/docker-compose
chmod 755 /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version

# 添加host
echo 58.220.49.186 harbor.cqt.com >> /etc/hosts

# 配置
mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<-'EOF'
{
	"registry-mirrors": [
	  "https://b9pmyelo.mirror.aliyuncs.com",
	  "https://hub-mirror.c.163.com",
	  "https://docker.mirrors.ustc.edu.cn"
	],
	"insecure-registries" : [ "58.220.49.186:11000" ],
  "log-driver":"json-file",
  "log-opts": {"max-size":"100m", "max-file":"5"},
  "data-root": "/home/docker"
}
EOF

# 重启
systemctl restart docker

# 服务器登录harbor
docker login -u admin --password cqt@1234 http://58.220.49.186:11000
