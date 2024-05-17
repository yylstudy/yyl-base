package com.cqt.monitor.web.distributor.event;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqt.common.util.JsonAndXmlUtil;
import com.cqt.model.sipconfig.Distributor;
import com.cqt.monitor.web.distributor.mapper.PrivateSipDistributorConfigInfoMapper;
import com.cqt.monitor.web.distributor.mapper.PrivateSipDistributorInfoMapper;
import com.cqt.monitor.web.distributor.model.dto.UpdateDistributorDbDTO;
import com.cqt.monitor.web.distributor.model.entity.PrivateSipDistributorConfigInfo;
import com.cqt.monitor.web.distributor.model.entity.PrivateSipDistributorInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * @since 2022-12-05 9:48
 * 中间号 更新dis组 管理平台表
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateDistributorDbEventListener implements ApplicationListener<UpdateDistributorDbEvent> {

    private final PrivateSipDistributorInfoMapper privateSipDistributorInfoMapper;

    private final PrivateSipDistributorConfigInfoMapper privateSipDistributorConfigInfoMapper;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public void onApplicationEvent(UpdateDistributorDbEvent event) {
        UpdateDistributorDbDTO updateDistributorDbDTO = event.getUpdateDistributorDbDTO();
        String serverIp = updateDistributorDbDTO.getServerIp();
        Distributor distributor = updateDistributorDbDTO.getDistributor();
        String disListName = updateDistributorDbDTO.getDisListName();

        String xml = JsonAndXmlUtil.jsonToXml(objectMapper.writeValueAsString(distributor));
        LambdaQueryWrapper<PrivateSipDistributorInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PrivateSipDistributorInfo::getDeviceIp, serverIp);
        queryWrapper.eq(PrivateSipDistributorInfo::getDeviceType, "SBC");
        queryWrapper.eq(PrivateSipDistributorInfo::getDeleteFlag, 0);
        PrivateSipDistributorInfo distributorInfo = new PrivateSipDistributorInfo();
        distributorInfo.setMd5(SecureUtil.md5(xml));
        distributorInfo.setDistributorContent(xml);
        distributorInfo.setUpdateTime(DateUtil.date());
        int update = privateSipDistributorInfoMapper.update(distributorInfo, queryWrapper);
        log.info("serverIp: {}, 修改dis组表 privateSipDistributorInfo: {}", serverIp, update);
        if (update > 0) {
            queryWrapper.last("limit 1");
            PrivateSipDistributorInfo sipDistributorInfo = privateSipDistributorInfoMapper.selectOne(queryWrapper);
            String distributorId = sipDistributorInfo.getDistributorId();
            List<Distributor.NodeList> nodeList = distributor.getConfiguration().getLists().getNodeList();
            List<Distributor.NodeList> nodeLists = nodeList.stream()
                    .filter(item -> item.getName().equals(disListName))
                    .collect(Collectors.toList());
            if (CollUtil.isNotEmpty(nodeLists)) {
                String nodeXml = getNodeXml(nodeLists);
                LambdaQueryWrapper<PrivateSipDistributorConfigInfo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(PrivateSipDistributorConfigInfo::getDistributorId, distributorId);
                wrapper.eq(PrivateSipDistributorConfigInfo::getListName, disListName);
                PrivateSipDistributorConfigInfo distributorConfigInfo = new PrivateSipDistributorConfigInfo();
                distributorConfigInfo.setListBody(nodeXml);
                int update1 = privateSipDistributorConfigInfoMapper.update(distributorConfigInfo, wrapper);
                log.info("serverIp: {}, disListName: {}, 修改dis组表 privateSipDistributorConfigInfo: {}", serverIp, disListName, update1);
            }
        }

    }

    private String getNodeXml(List<Distributor.NodeList> nodeLists) throws JsonProcessingException {
        Distributor.Lists lists = new Distributor.Lists();
        lists.setNodeList(nodeLists);
        return JsonAndXmlUtil.jsonToXml(objectMapper.writeValueAsString(lists));
    }

}
