package com.cqt.call.strategy.client.impl;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cqt.base.enums.CallDirectionEnum;
import com.cqt.base.enums.OSTypeEnum;
import com.cqt.base.enums.agent.AgentStatusEnum;
import com.cqt.base.enums.ext.ExtStatusEnum;
import com.cqt.base.util.ValidationUtil;
import com.cqt.call.service.DataQueryService;
import com.cqt.model.agent.dto.AgentStatusDTO;
import com.cqt.model.agent.entity.AgentInfo;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.ext.dto.ExtStatusDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author linshiqiang
 * date:  2023-07-12 9:25
 */
public abstract class AbstractClientChecker {

    /**
     * 转对象
     *
     * @param requestBody json
     * @param valueType   对象类型
     * @param <T>         对象类型
     * @return 对象
     * @throws JsonProcessingException json转化异常
     */
    public <T> T convert(String requestBody, Class<T> valueType) throws JsonProcessingException {
        ObjectMapper objectMapper = SpringUtil.getBean(ObjectMapper.class);
        T t = objectMapper.readValue(requestBody, valueType);
        // 参数校验
        ValidationUtil.validate(t);
        return t;
    }

    /**
     * 获取坐席外呼主显号
     *
     * @param checkAgentAvailableVO 校验坐席结果
     * @return 坐席外呼主显号
     */
    public String getDisplayNumber(CheckAgentAvailableVO checkAgentAvailableVO) {
        CompanyInfo companyInfo = checkAgentAvailableVO.getCompanyInfo();
        AgentInfo agentInfo = checkAgentAvailableVO.getAgentInfo();
        return getDataQueryService().getAgentDisplayNumber(companyInfo, agentInfo);
    }

    /**
     * 检查坐席是否为管理员
     * 监听, 耳语, 代接, 强拆
     *
     * @param companyCode     企业id
     * @param agentId         管理员坐席id
     * @param operatedAgentId 被管理坐席id
     * @return vo
     */
    public CheckAgentAvailableVO checkAgentAdmin(String companyCode, String agentId, String operatedAgentId) throws Exception {
        // agentId是否为管理员 且是空闲或忙碌状态
        CheckAgentAvailableVO adminAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.ONLINE,
                Lists.newArrayList(AgentStatusEnum.FREE, AgentStatusEnum.BUSY),
                companyCode, "", agentId);
        if (!adminAgentAvailableVO.getAvailable()) {
            return adminAgentAvailableVO;
        }
        // TODO 暂时不管是否为管理员

        // 校验被管理的坐席
        CheckAgentAvailableVO operatedAgentAvailableVO = checkAgentAvailable(ExtStatusEnum.CALLING,
                Lists.newArrayList(AgentStatusEnum.CALLING),
                companyCode, "", operatedAgentId);
        if (!operatedAgentAvailableVO.getAvailable()) {
            return operatedAgentAvailableVO;
        }
        adminAgentAvailableVO.setOperatedAgentInfo(operatedAgentAvailableVO.getAgentInfo());
        adminAgentAvailableVO.setOperatedAgentStatusDTO(operatedAgentAvailableVO.getAgentStatusDTO());
        adminAgentAvailableVO.setOperatedExtInfoDTO(operatedAgentAvailableVO.getExtInfoDTO());
        adminAgentAvailableVO.setOperatedExtStatusDTO(operatedAgentAvailableVO.getExtStatusDTO());
        return adminAgentAvailableVO;
    }

    /**
     * 校验坐席可用性
     */
    public CheckAgentAvailableVO checkAgentAvailable(ExtStatusEnum extStatusEnum,
                                                     List<AgentStatusEnum> agentStatusEnumList,
                                                     String companyCode,
                                                     String extId,
                                                     String agentId) throws Exception {
        return checker(extStatusEnum, agentStatusEnumList, companyCode, extId, agentId, "");
    }

    /**
     * 校验坐席可用性
     */
    public CheckAgentAvailableVO checkAgentAvailable(ExtStatusEnum extStatusEnum,
                                                     List<AgentStatusEnum> agentStatusEnumList,
                                                     String companyCode,
                                                     String extId,
                                                     String agentId,
                                                     String calleeNumber) throws Exception {
        return checker(extStatusEnum, agentStatusEnumList, companyCode, extId, agentId, calleeNumber);
    }

    private CheckAgentAvailableVO checker(ExtStatusEnum extStatusEnum,
                                          List<AgentStatusEnum> agentStatusEnumList,
                                          String companyCode,
                                          String extId,
                                          String agentId,
                                          String calleeNumber) throws Exception {
        DataQueryService dataQueryService = getDataQueryService();
        // 企业配置信息
        CompanyInfo companyInfo = dataQueryService.getCompanyInfoDTO(companyCode);
        // 企业启用
        if (Objects.isNull(companyInfo)) {
            return CheckAgentAvailableVO.fail(false, StrFormatter.format("未找到企业配置: {}!", companyCode));
        }

        if (Objects.nonNull(companyInfo.getState()) && companyInfo.getState() == 0) {
            return CheckAgentAvailableVO.fail(false, StrFormatter.format("企业: {} 被禁用!", companyCode));
        }

        // 校验被叫号码是否在黑名单内, 禁止呼叫
        if (StrUtil.isNotEmpty(calleeNumber)) {
            boolean isBlack = dataQueryService.checkBlackNumber(companyCode, calleeNumber, CallDirectionEnum.OUTBOUND);
            if (isBlack) {
                return CheckAgentAvailableVO.fail(false, StrFormatter.format("号码: {} 在黑名单内, 禁止呼叫!", calleeNumber));
            }
        }

        // 坐席实时状态
        Optional<AgentStatusDTO> statusstatusOptional = dataQueryService.getActualAgentStatus(companyCode, agentId);
        if (!statusstatusOptional.isPresent()) {
            return CheckAgentAvailableVO.fail(false, StrFormatter.format("坐席: {}, 未签入!", agentId));
        }
        AgentStatusDTO agentStatusDTO = statusstatusOptional.get();
        String agentTargetStatus = agentStatusDTO.getTargetStatus();
        if (StrUtil.isNotEmpty(agentTargetStatus)) {
            AgentStatusEnum target = AgentStatusEnum.valueOf(agentTargetStatus);
            if (!agentStatusEnumList.contains(target)) {
                String desc = agentStatusEnumList.stream()
                        .map(AgentStatusEnum::getDesc)
                        .collect(Collectors.joining("、"));
                return CheckAgentAvailableVO.fail(false, StrFormatter.format("坐席: {}, 非{}状态!", agentId, desc));
            }
        }

        // 坐席配置信息
        AgentInfo agentInfo = dataQueryService.getAgentInfo(companyCode, agentId, false);
        if (Objects.isNull(agentInfo)) {
            return CheckAgentAvailableVO.fail(false, StrFormatter.format("未找到坐席配置: {}!", agentId));
        }
        // 坐席启用状态
        if (Objects.nonNull(agentInfo.getState()) && agentInfo.getState() == 0) {
            return CheckAgentAvailableVO.fail(false, StrFormatter.format("坐席: {}, 被禁用!", agentId));
        }

        if (StrUtil.isEmpty(extId)) {
            extId = agentInfo.getSysExtId();
        }
        ExtStatusDTO extStatusDTO = dataQueryService.getActualExtStatus(companyCode, extId);
        if (Objects.isNull(extStatusDTO)) {
            return CheckAgentAvailableVO.fail(false, StrFormatter.format("分机: {}, 未注册!", extId));
        }
        String extTargetStatus = extStatusDTO.getTargetStatus();
        if (!extStatusEnum.name().equals(extTargetStatus)) {
            return CheckAgentAvailableVO.fail(false, StrFormatter.format("分机: {}, 非{}状态!", extId, extStatusEnum.getDesc()));
        }

        // 坐席绑定分机与传入的分机是否一致
        String sysExtId = agentInfo.getSysExtId();
        if (StrUtil.isEmpty(sysExtId)) {
            return CheckAgentAvailableVO.fail(false, StrFormatter.format("坐席: {}, 未绑定分机!", agentId));
        }
        if (!sysExtId.equals(extId)) {
            return CheckAgentAvailableVO.fail(false, StrFormatter.format("分机: {} 与坐席绑定的分机不一致!", extId));
        }

        return CheckAgentAvailableVO.builder()
                .extStatusDTO(extStatusDTO)
                .agentStatusDTO(agentStatusDTO)
                .companyInfo(companyInfo)
                .agentInfo(agentInfo)
                .available(true)
                .build();
    }

    private DataQueryService getDataQueryService() {
        return SpringUtil.getBean(DataQueryService.class);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CheckAgentAvailableVO implements Serializable {

        private static final long serialVersionUID = 554494811152234631L;

        private CompanyInfo companyInfo;

        private ExtStatusDTO extStatusDTO;

        private AgentStatusDTO agentStatusDTO;

        private AgentInfo extInfoDTO;

        private AgentInfo agentInfo;

        private ExtStatusDTO operatedExtStatusDTO;

        private AgentStatusDTO operatedAgentStatusDTO;

        private AgentInfo operatedExtInfoDTO;

        private AgentInfo operatedAgentInfo;

        private Boolean available;

        private String message;

        /**
         * 校验失败返回
         */
        public static CheckAgentAvailableVO fail(Boolean available, String message) {
            return CheckAgentAvailableVO.builder()
                    .available(available)
                    .message(message)
                    .build();
        }

        @JsonIgnore
        public String getOs() {
            if (Objects.isNull(this.getAgentStatusDTO())) {
                return OSTypeEnum.Windows.name();
            }
            return this.getAgentStatusDTO().getOs();
        }

    }
}
