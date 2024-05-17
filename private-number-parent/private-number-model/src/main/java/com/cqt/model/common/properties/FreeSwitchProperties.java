package com.cqt.model.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author linshiqiang
 * @since 2022/7/14 14:01
 * freeswitch配置信息
 */
@Data
@Component
@ConfigurationProperties(prefix = "freeswitch")
public class FreeSwitchProperties {

    /**
     * /home/freeswitch-1.10.6-install.tar.gz
     */
    private String freeswitchPackagePath = "/home/freeswitch-1.10.6-install.tar.gz";

    /**
     * /home/freeswitch-1.10.6-install.sh
     */
    private String installShellPath = "/home/freeswitch-1.10.6-install.sh";

    private String destPth = "/home";

    /**
     * 安装freeswitch 脚本
     * sh /home/freeswitch-1.10.6-install.sh >install.log &
     */
    private String installCmd = "sh /home/freeswitch-1.10.6-install.sh >install.log &";

    /**
     * xml文件路径
     */
    private FilePath filePath = new FilePath();

    /**
     * 命令执行
     */
    private Command command = new Command();

    /**
     * profile延迟查询加载状态时间, 默认为5s
     */
    private Integer profileStatusDelayTime = 5;

    @Data
    public static class FilePath {

        /**
         * /usr/local/freeswitch/conf/sip_profiles/%s.xml
         */
        private String profile = "/usr/local/freeswitch/conf/sip_profiles/%s.xml";

        /**
         * /usr/local/freeswitch/conf/sip_profiles/%s/%s.xml
         */
        private String gateway = "/usr/local/freeswitch/conf/sip_profiles/%s/%s.xml";

        /**
         * /usr/local/freeswitch/conf/sip_profiles/%s
         */
        private String gatewayDir = "/usr/local/freeswitch/conf/sip_profiles/%s";

        private String dialplan = "/usr/local/freeswitch/conf/dialplan/%s.xml";

        private String distributor = "/usr/local/freeswitch/conf/autoload_configs/ctd_distributor/%s.xml";

        private String lua = "/usr/local/freeswitch/scripts/%s.lua";

        /**
         * 主要是 最大并发数
         * <param name="max-sessions" value="${maxSession}"/>
         */
        private String switchConf = "/usr/local/freeswitch/conf/autoload_configs/switch.conf.xml";
    }

    @Data
    public static class Command {

        /**
         * 查询fs 全局状态
         */
        private String fsStatus = "fs_cli -x \"status\"";

        /**
         * 查看fs版本命令
         * freeswitch -version
         */
        private String freeswitchVersion = "freeswitch -version";

        /**
         * 查看文本内容命令
         * cat %s
         */
        private String cat = "cat %s";

        /**
         * 加载  启动：fs_cli -x "sofia profile %s start"
         */
        private String startProfile = "fs_cli -x 'sofia profile %s start'";

        /**
         * 下架  停止：fs_cli -x "sofia profile %s stop"
         */
        private String stopProfile = "fs_cli -x 'sofia profile %s stop'";

        private String restartProfile = "fs_cli -x 'sofia profile %s restart'";

        private String profileStatus = "fs_cli -x 'sofia status profile %s'";

        private String gatewayStatus = "fs_cli -x 'sofia status gateway %s'";

        private String rescanGateway = "fs_cli -x  'sofia profile %s rescan'";

        /*
         * --------------------------------------------------------------------------------
         * sofia global siptrace <on|off>
         * sofia        capture  <on|off>
         *              watchdog <on|off>
         *
         * sofia profile <name> [start | stop | restart | rescan] [wait]
         *                      flush_inbound_reg [<call_id> | <[user]@domain>] [reboot]
         *                      check_sync [<call_id> | <[user]@domain>]
         *                      [register | unregister] [<gateway name> | all]
         *                      killgw <gateway name>
         *                      [stun-auto-disable | stun-enabled] [true | false]]
         *                      siptrace <on|off>
         *                      capture  <on|off>
         *                      watchdog <on|off>
         *
         * sofia <status|xmlstatus> profile <name> [reg [<contact str>]] | [pres <pres str>] | [user <user@domain>]
         * sofia <status|xmlstatus> gateway <name>
         *
         * sofia loglevel <all|default|tport|iptsec|nea|nta|nth_client|nth_server|nua|soa|sresolv|stun> [0-9]
         * sofia tracelevel <console|alert|crit|err|warning|notice|info|debug>
         *
         * sofia help
         * --------------------------------------------------------------------------------
         */
        private String killGw = "fs_cli -x  'sofia profile %s killgw %s'";

        /**
         * 加载xml
         */
        private String reloadXml = "fs_cli -x 'reloadxml'";

        /**
         * 加载轮询组 fs_cli -x 'reload mod_distributor'
         */
        private String reloadDistributor = "fs_cli -x 'reload mod_ctd_distributor'";

        /**
         * 删除文件 rm -rf %s
         */
        private String delFile = "rm -rf %s";

        /**
         * 创建文本内容命令, 会覆盖原内容
         * echo "xml content" > /usr/local/freeswitch/conf/sip_profiles/profile.xml
         * echo "%s" > %s
         */
        private String echo = "echo \"%s\" > %s";

        /**
         * ls %s
         */
        private String ls = " ls %s";

        private String mkdir = "mkdir -p %s";
    }


}
