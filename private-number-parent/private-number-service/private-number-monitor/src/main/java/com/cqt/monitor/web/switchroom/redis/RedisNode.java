package com.cqt.monitor.web.switchroom.redis;

import lombok.Data;

/**
 * For
 *
 * @author Jay.H.Zou
 * @date 7/25/2019
 */
@Data
public class RedisNode {

    public static final String CONNECTED = "connected";

    public static final String DISCONNECTED = "disconnected";

    private String nodeId;

    /***
     * 如果节点是slave，并且已知master节点，则这里列出master节点ID,否则的话这里列出"-"
     */
    private String masterId;

    private String host;

    private int port;

    private NodeRole nodeRole;

    /**
     * myself: 当前连接的节点
     * master: 节点是master.
     * slave: 节点是slave.
     * fail?: 节点处于PFAIL 状态。 当前节点无法联系，但逻辑上是可达的 (非 FAIL 状态).
     * fail: 节点处于FAIL 状态. 大部分节点都无法与其取得联系将会将改节点由 PFAIL 状态升级至FAIL状态。
     * handshake: 还未取得信任的节点，当前正在与其进行握手.
     * noaddr: 没有地址的节点（No address known for this node）.
     * noflags: 连个标记都没有（No flags at all）.
     */
    private String flags;

    /**
     * link-state: node-to-node 集群总线使用的链接的状态，我们使用这个链接与集群中其他节点进行通信.值可以是 connected 和 disconnected.
     */
    private String linkState;

    private String slotRange;

    private int slotNumber;

    private String containerId;

    private String containerName;

    private Boolean runStatus;

    public RedisNode(String nodeId, String host, int port, NodeRole nodeRole) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.nodeRole = nodeRole;
    }
}
