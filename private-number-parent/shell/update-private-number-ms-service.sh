#! /bin/sh
. $HOME/.bash_profile
. /etc/profile
. ~/.bash_profile

# 上一个版本号
LAST_VERSION='2.2.0'
# DOWNLOAD_URL="http://172.16.251.68/docs/"
# 包预先存放位置
DOWNLOAD_URL="http://58.220.49.186:9999/private_number/qh-2.1.0/"

# 通用号码隐藏前端页面
dist() {
    mkdir -p /home/private-number/web

	  wget $DOWNLOAD_URL/dist.zip -O /home/private-number/web/dist.zip

	  unzip -o /home/private-number/web/dist.zip -d /home/private-number/web/dist
}

# 管理平台后端服务
updatePrivateNumberMsWeb(){
	  # web端 jar存放目录
    local JAR_DIR=/home/springboot/private-number-ms
	  # web端 jar备份目录
	  local JAR_BACK_PATH=$JAR_DIR/back

	  mkdir -p $JAR_DIR
	  mkdir -p $JAR_BACK_PATH
    local APP_NAME=$1
    local JAR_NAME=$APP_NAME\.jar
    # 备份jar包
    /bin/cp -rf $JAR_DIR/$JAR_NAME $JAR_BACK_PATH/$APP_NAME-$LAST_VERSION.jar
 	
    # 下载jar包
    wget $DOWNLOAD_URL/$JAR_NAME -O $JAR_DIR/$JAR_NAME
    echo "====================dowload  $JAR_NAME finish===================="
	
    # 杀进程
    ps -ef | grep $JAR_NAME| grep -v grep | awk '{print $2}' | xargs kill -9
    echo "====================kill  $JAR_NAME finish===================="
   
    # tail -100f /home/smp/logs/private-number-ms-gateway/gateway-info.log
    # tail -100f /home/smp/logs/private-number-ms-system/system-info.log
    # tail -100f /home/smp/logs/private-number-ms-report/report-info.log
    # tail -100f /home/smp/logs/private-number-ms-business/business-info.log
    # tail -1000f /home/smp/logs/private-number-ms-base-setting/setting-info.log 
}

# 通用更新包函数, 传入服务名称
commonServerUpdate() {
    local APP_NAME=$1
    local JAR_DIR=/home/springboot/$APP_NAME
    local JAR_BACK_PATH=$JAR_DIR/back

	  mkdir -p $JAR_DIR
  	mkdir -p $JAR_BACK_PATH
    
    local JAR_NAME=$APP_NAME\.jar
    # 备份jar包
    /bin/cp -rf $JAR_DIR/$JAR_NAME $JAR_BACK_PATH/$APP_NAME-$LAST_VERSION.jar
	
    # 下载jar包
    wget $DOWNLOAD_URL/$JAR_NAME -O $JAR_DIR/$JAR_NAME
    echo "====================dowload  $JAR_NAME finish===================="
	
    # 杀进程
    ps -ef | grep $JAR_NAME| grep -v grep | awk '{print $2}' | xargs kill -9
    echo "====================kill  $JAR_NAME finish===================="
	
  	# 查看启动日志
	  tail -100f /home/smp/logs/$APP_NAME/$APP_NAME-info.log
}

# 更新ms所有服务
updateMsAll() {
    updateGateway
    updateSystem
    updateSetting
    updateReport
    updateBusiness
}


#启动时带参数，根据参数执行
if [ ${#} -ge 1 ]
then
    echo "current verion: ${versions}"
    case ${1} in
        "dist")
            dist
        ;;
		"commonServerUpdate")
            commonServerUpdate ${2}
        ;;
		"updatePrivateNumberMsWeb")
            updatePrivateNumberMsWeb ${2}
        ;;
       "msAll")
            updateMsAll 
        ;;
        *)
            echo "${1}无任何操作"
        ;;
    esac
else
    echo "
		示例命令如：
		# 管理平台服务
		sh /home/shellJob/update-private-number-ms-service.sh dist
		sh /home/shellJob/update-private-number-ms-service.sh updatePrivateNumberMsWeb private-number-ms-gateway 
		sh /home/shellJob/update-private-number-ms-service.sh updatePrivateNumberMsWeb private-number-ms-system
		sh /home/shellJob/update-private-number-ms-service.sh updatePrivateNumberMsWeb private-number-ms-report
		sh /home/shellJob/update-private-number-ms-service.sh updatePrivateNumberMsWeb private-number-ms-core-business
		sh /home/shellJob/update-private-number-ms-service.sh updatePrivateNumberMsWeb private-number-ms-base-setting

		# 号码隐藏后端服务
		sh /home/shellJob/update-private-number-ms-service.sh commonServerUpdate private-number-gateway
		sh /home/shellJob/update-private-number-ms-service.sh commonServerUpdate private-number-hmyc
		sh /home/shellJob/update-private-number-ms-service.sh commonServerUpdate private-number-recycle
		sh /home/shellJob/update-private-number-ms-service.sh commonServerUpdate private-number-monitor
		sh /home/shellJob/update-private-number-ms-service.sh commonServerUpdate private-number-push
		sh /home/shellJob/update-private-number-ms-service.sh commonServerUpdate private-number-sms
    "
fi
