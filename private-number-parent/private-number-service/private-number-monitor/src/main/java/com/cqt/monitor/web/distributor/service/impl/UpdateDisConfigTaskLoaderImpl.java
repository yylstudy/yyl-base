package com.cqt.monitor.web.distributor.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cqt.cloud.api.agent.AgentRemoteClient;
import com.cqt.common.constants.SystemConstant;
import com.cqt.common.enums.OperateTypeEnum;
import com.cqt.common.future.TaskLoader;
import com.cqt.common.util.JsonAndXmlUtil;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.model.common.NewFileDTO;
import com.cqt.model.common.properties.FreeSwitchProperties;
import com.cqt.model.sipconfig.Distributor;
import com.cqt.monitor.web.distributor.event.RecordFailNodeEvent;
import com.cqt.monitor.web.distributor.event.UpdateDistributorDbEvent;
import com.cqt.monitor.web.distributor.model.dto.GetDisConfigDTO;
import com.cqt.monitor.web.distributor.model.dto.UpdateDisConfigDTO;
import com.cqt.monitor.web.distributor.model.dto.UpdateDistributorDbDTO;
import com.cqt.monitor.web.distributor.model.vo.UpdateDisConfigVO;
import com.cqt.redis.util.RedissonUtil;
import com.dtflys.forest.http.ForestResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @since 2022-12-02 10:29
 * 修改SBC dis组 配置文件
 */
@Slf4j
@Component
public class UpdateDisConfigTaskLoaderImpl implements TaskLoader<UpdateDisConfigVO, UpdateDisConfigDTO> {

    @SneakyThrows
    @Override
    public UpdateDisConfigVO load(UpdateDisConfigDTO updateDisConfigDTO) {
        RedissonUtil redissonUtil = SpringUtil.getBean(RedissonUtil.class);
        String serverIp = updateDisConfigDTO.getServerIp();
        // lock sbc ip
        String lockKey = PrivateCacheUtil.getUpdateSbcDisMonitorLockKey(serverIp);
        redissonUtil.lock(lockKey, 10L);
        UpdateDisConfigVO updateDisConfigVO;
        try {
            // 原dis组 配置内容
            Optional<String> contentOptional = getDisConfig(GetDisConfigDTO.init(serverIp, updateDisConfigDTO.getDistributorConfigPath()));
            if (!contentOptional.isPresent()) {
                return UpdateDisConfigVO.setVO(serverIp, false, false, "获取SBC dis组配置失败", null, false);
            }
            updateDisConfigVO = doUpdate(updateDisConfigDTO, contentOptional.get(), updateDisConfigDTO.getWeight());

            // 中间号 更新dis组 管理平台表
            if (updateDisConfigVO.getSuccess()) {
                UpdateDistributorDbDTO updateDistributorDbDTO = new UpdateDistributorDbDTO();
                updateDistributorDbDTO.setServerIp(serverIp);
                updateDistributorDbDTO.setDisListName(updateDisConfigDTO.getDisListName());
                updateDistributorDbDTO.setDistributor(updateDisConfigVO.getDistributor());
                SpringUtil.publishEvent(new UpdateDistributorDbEvent(this, updateDistributorDbDTO));
            }

        } finally {
            redissonUtil.unLock(lockKey);
        }

        return updateDisConfigVO;
    }

    private UpdateDisConfigVO doUpdate(UpdateDisConfigDTO updateDisConfigDTO, String content, String newWeight) throws JsonProcessingException {
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        AgentRemoteClient agentRemoteClient = SpringUtil.getBean(AgentRemoteClient.class);
        FreeSwitchProperties freeSwitchProperties = SpringUtil.getBean(FreeSwitchProperties.class);
        // SBC服务器ip
        String serverIp = updateDisConfigDTO.getServerIp();
        // dis组配置文件路径
        String distributorConfigPath = updateDisConfigDTO.getDistributorConfigPath();

        // dis组节点名称(gateway的名称, 送给SN/ACD, 对应ip为serverIp), 将这些节点 weight=0
        List<String> nodeNameList = updateDisConfigDTO.getNodeNameList();
        // dis组名称
        String disListName = updateDisConfigDTO.getDisListName();

        // 解析dis组配置文件
        // 获取当前ip对应的节点, 将该节点的权重改为0
        Distributor distributor = getDistributor(content);

        List<Distributor.NodeList> lists = distributor.getConfiguration().getLists().getNodeList();
        List<Distributor.NodeList> nodeLists = lists.stream().filter(item -> disListName.equals(item.getName())).collect(Collectors.toList());
        // 修改前 原权重配置xml
        String beforeNodeXml = getNodeXml(nodeLists);
        if (CollUtil.isEmpty(nodeLists)) {
            log.error("serverIp: {}, path: {}, xml: {}, nacos配置未找到disListName: {}", serverIp, distributorConfigPath, content, disListName);
            String message = StrUtil.format(" SBC_IP: {}{} nacos配置未找到disListName: {}", serverIp, StrUtil.LF, disListName);
            return UpdateDisConfigVO.setVO(serverIp, false, true, message, null, false);
        }
        Distributor.NodeList list = nodeLists.get(0);
        List<Distributor.Node> nodeList = list.getNode();
        boolean weightUpdate = false;
        for (Distributor.Node node : nodeList) {
            if (nodeNameList.contains(node.getName())) {
                // 这里权重一定是个数字
                String weight = node.getWeight();
                if (!newWeight.equals(weight)) {
                    node.setWeight(newWeight);
                    log.info("serverIp: {}, disListName: {}, node: {}, 原权重为: {}, 修改权重为 {} 完成",
                            serverIp, disListName, node.getName(), weight, newWeight);
                    weightUpdate = true;
                }
            }
        }

        // 修改后 dis组list xml
        String nodeXml = getNodeXml(nodeLists);

        // 节点 权重已经修改过, 不进行后续操作
        if (!weightUpdate) {
            String message = StrUtil.format(" SBC_IP: {}{} 节点名称: {}{} 节点权重已经修改为 {}, 不进行修改权重操作: {}{}",
                    serverIp, StrUtil.LF, nodeNameList, StrUtil.LF, newWeight, StrUtil.LF, nodeXml);
            log.info("weightZero: {}", message);

            recordZeroWeightNode(newWeight);
            return UpdateDisConfigVO.setVO(serverIp, false, true, message, null, true);
        }

        // 修改SBC文件
        String xml = JsonAndXmlUtil.entityToXml(distributor);
        ForestResponse<String> response = agentRemoteClient.newFile(serverIp,
                new NewFileDTO(distributorConfigPath, xml.replaceAll(StrUtil.TAB, "  ")));
        if (response.isSuccess()) {
            String message = StrUtil.format(" SBC_IP: {}{} 节点名称: {}{} dis组修改节点权重为 {}, 修改成功. {} 修改前配置: {}{} 修改后配置: {}{}",
                    serverIp, StrUtil.LF, nodeNameList, StrUtil.LF, newWeight, StrUtil.LF, StrUtil.LF, beforeNodeXml, StrUtil.LF, nodeXml);
            log.info("newFile: {}", message);
            String reloadDistributorCmd = freeSwitchProperties.getCommand().getReloadDistributor();
            ForestResponse<String> execute = agentRemoteClient.execute(serverIp, reloadDistributorCmd);
            log.info("serverIp: {}, execute reloadDistributorCmd result: {}", serverIp, execute.getContent());

            recordZeroWeightNode(newWeight);
            return UpdateDisConfigVO.setVO(serverIp, true, true, message, distributor, false);
        }
        String message = StrUtil.format(" SBC_IP: {}{} dis组修改节点权重为 {} 修改失败, 错误信息:{}{}: {}{}",
                serverIp, StrUtil.LF, newWeight, StrUtil.LF, response.getException(), StrUtil.LF, nodeXml);
        log.error("newFile error: {}", message);
        return UpdateDisConfigVO.setVO(serverIp, false, true, message, null, false);
    }

    private static Distributor getDistributor(String content) throws JsonProcessingException {
        String toJson = JsonAndXmlUtil.xmlToJson(content, true,
                "/configuration/lists/list", "/configuration/lists/list/node");
        return JsonAndXmlUtil.jsonToEntity(Distributor.class, toJson);
    }

    /**
     * 记录失败的监控节点(当前设备) 权重为0
     */
    private void recordZeroWeightNode(String newWeight) {
        if (SystemConstant.ZERO.equals(newWeight)) {
            SpringUtil.publishEvent(new RecordFailNodeEvent(this, OperateTypeEnum.INSERT));
        }
    }

    public Optional<String> getDisConfig(GetDisConfigDTO configDTO) {
        AgentRemoteClient agentRemoteClient = SpringUtil.getBean(AgentRemoteClient.class);
        FreeSwitchProperties freeSwitchProperties = SpringUtil.getBean(FreeSwitchProperties.class);

        String serverIp = configDTO.getServerIp();
        String distributorConfigPath = configDTO.getDistributorConfigPath();
        String cat = freeSwitchProperties.getCommand().getCat();
        String catCmd = String.format(cat, distributorConfigPath);
        ForestResponse<String> response = agentRemoteClient.execute(serverIp, catCmd);
        if (response.isSuccess()) {

            return Optional.ofNullable(response.getContent());
        }
        log.error("serverIp: {}, dis: {}, get config fail: ", serverIp, distributorConfigPath, response.getException());
        return Optional.empty();
    }

    private String getNodeXml(List<Distributor.NodeList> nodeLists) throws JsonProcessingException {
        Distributor.Lists lists = new Distributor.Lists();
        lists.setNodeList(nodeLists);
        return JsonAndXmlUtil.entityToXml(lists);
    }

}
