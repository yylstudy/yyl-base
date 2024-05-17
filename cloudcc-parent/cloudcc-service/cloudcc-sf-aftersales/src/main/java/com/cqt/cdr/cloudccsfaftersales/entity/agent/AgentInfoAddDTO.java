package com.cqt.cdr.cloudccsfaftersales.entity.agent;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

/**
 * @author Xienx
 * @date 2023-07-11 14:45:14:45
 */
@Data
public class AgentInfoAddDTO implements Serializable {

    private static final long serialVersionUID = 852464337581294232L;

    /**
     * 坐席工号添加模式
     */
    @Range(min = 1, max = 3, message = "添加模式为1~3")
    @NotNull(message = "坐席工号添加模式不能为空")
    private Integer addMode;

    /**
     * 坐席工号 （单个添加时填写）
     */
    @Length(min = 1, max = 10, message = "坐席工号长度支持1~10位")
    private String agentId;

    /**
     * 开始坐席工号
     */
    @Length(min = 1, max = 10, message = "开始坐席工号长度支持1~10位")
    private String startAgentId;

    /**
     * 结束坐席工号
     */
    @Length(min = 1, max = 10, message = "结束坐席工号长度支持1~10位")
    private String endAgentId;

    /**
     * 自定义坐席工号
     */
    @Size(min = 1, max = 10, message = "自定义添加最多支持10个坐席工号")
    private List<String> customAgentIds;

    /**
     * 坐席姓名
     */
    private String agentName;

    /**
     * 坐席密码
     */
    @NotBlank(message = "坐席密码不能为空")
    private String password;

    /**
     * 角色（菜单访问权限）
     */
    @NotEmpty(message = "角色id不能为空")
    private List<String> roleIdList;

    /**
     * 班组
     */
    private List<String> departIdList;

    /**
     * 数据权限
     */
    private Integer dataScope;

    /**
     * 外显号
     */
    private String displayNumber;

    /**
     * 状态 0:禁用 1:启用
     */
    @NotNull(message = "坐席状态不能为空")
    private Integer state;

    /**
     * 技能权值配置
     */
    private List<SkillWeightInfo> skillWeightInfos;

    /**
     * 坐席绑定分机模式
     */
    @NotNull(message = "坐席绑定分机模式不能为空")
    private Integer extBindMode;

    /**
     * 自定义绑定的分机号
     */
    private String extId;

    /**
     * 分机注册方式 1、webrtc 2、第三方话机
     */
    private Integer extRegMode;

    /**
     * 自动示忙 0：关闭 1：开启
     * 话务分配给坐席，坐席设置自动应答且应答失败，或坐席设置手动应答且拒接来电，坐席状态是否自动变更为示忙
     */
    private Integer autoShowBusy;

    /**
     * 事后处理 0：关闭 1：开启
     */
    private Integer postProcess;

    /**
     * 事后处理时间 秒
     */
    @Min(value = 0, message = "事后处理时间应为非负数")
    private Integer processTime;

    /**
     * 手机接听离线坐席（0：关闭 1：开启）
     *
    private Integer offlineAgent;

    /**
     * 离线坐席接续的手机
     */
    private String phoneNumber;

//    public void paramCheck() {
//        // 单个添加模式时, 坐席工号必填
//        if (BusinessConstant.ID_ADD_MODE_SINGLE.equals(addMode) && StrUtil.isBlank(agentId)) {
//            throw new ParamCheckException("单个添加模式坐席工号必填");
//        }
//        // 连续添加时, 开始工号和结束工号必填
//        if (BusinessConstant.ID_ADD_MODE_SEGMENT.equals(addMode)) {
//            if (StrUtil.isBlank(startAgentId) || StrUtil.isBlank(endAgentId)) {
//                throw new ParamCheckException("连续添加模式开始工号与结束工号不能为空");
//            }
//            int startId = Convert.toInt(startAgentId);
//            int endId = Convert.toInt(endAgentId);
//            // 结束工号必须大于开始工号
//            if (endId <= startId) {
//                throw new ParamCheckException("结束坐席工号必须大于开始坐席工号");
//            }
//            if (BusinessConstant.AGENT_BIND_EXT_MODE_CUSTOM.equals(extBindMode)) {
//                throw new ParamCheckException("连续添加工号时不允许自定义绑定分机号");
//            }
//        }
//
//        if (BusinessConstant.ID_ADD_MODE_CUSTOM.equals(addMode)) {
//            // 自定义添加时. 自定义工号必填
//            if (customAgentIds.isEmpty()) {
//                throw new ParamCheckException("自定义添加模式自定义坐席工号不能为空");
//            }
//            if (BusinessConstant.AGENT_BIND_EXT_MODE_CUSTOM.equals(extBindMode)) {
//                throw new ParamCheckException("自定义添加工号时不允许自定义绑定分机号");
//            }
//        }
//    }

//    public List<String> makeAgentIds() {
//        int startId = Convert.toInt(startAgentId);
//        int endId = Convert.toInt(endAgentId);
//
//        List<Integer> agentIds = new ArrayList<>();
//        for (int i = startId; i <= endId; i++) {
//            agentIds.add(i);
//        }
//        return agentIds.stream()
//                .map(String::valueOf)
//                .collect(Collectors.toList());
//    }
//
//
//    private AgentInfo toEntity(String agentId, String bindExtId) {
//        AgentInfo agentInfo = new AgentInfo();
//        agentInfo.setAgentId(agentId);
//        agentInfo.setAgentName(agentName);
//        if (StrUtil.isBlank(agentName)) {
//            agentInfo.setAgentName(String.format(BusinessConstant.AGENT_NAME_FORMAT, agentId));
//        }
//        agentInfo.setPassword(password);
//        agentInfo.setExtBindMode(extBindMode);
//        agentInfo.setExtId(bindExtId);
//        agentInfo.setDepartIds(StrUtil.join(StrUtil.COMMA, departIdList));
//        agentInfo.setDataScope(dataScope);
//        agentInfo.setDisplayNumber(displayNumber);
//        agentInfo.setState(state);
//        agentInfo.setExtRegMode(extRegMode);
//        agentInfo.setAutoShowBusy(autoShowBusy);
//        agentInfo.setPostProcess(postProcess);
//        agentInfo.setProcessTime(processTime);
//        agentInfo.setOfflineAgent(offlineAgent);
//        agentInfo.setPhoneNumber(phoneNumber);
//
//        return agentInfo;
//    }
//
//
//    public AgentInfo toEntity() {
//        // 如果是自定义绑定，那么分机号就是指定的分机号，否则就是坐席号
//        if (BusinessConstant.AGENT_BIND_EXT_MODE_CUSTOM.equals(extBindMode)) {
//            return toEntity(agentId, extId);
//        }
//        return toEntity(agentId, agentId);
//    }
//
//    public List<AgentInfo> toEntities() {
//        if (BusinessConstant.ID_ADD_MODE_SINGLE.equals(addMode)) {
//            return Collections.emptyList();
//        }
//
//        List<AgentInfo> agentInfos = new ArrayList<>();
//
//        if (BusinessConstant.ID_ADD_MODE_SEGMENT.equals(addMode)) {
//            List<String> agentIds = makeAgentIds();
//            for (String agentId : agentIds) {
//                AgentInfo agentInfo = toEntity(agentId, agentId);
//                agentInfos.add(agentInfo);
//            }
//        }
//
//        if (BusinessConstant.ID_ADD_MODE_CUSTOM.equals(addMode)) {
//            for (String agentId : customAgentIds) {
//                AgentInfo agentInfo = toEntity(agentId, agentId);
//                agentInfos.add(agentInfo);
//            }
//        }
//        return agentInfos;
//    }

}
