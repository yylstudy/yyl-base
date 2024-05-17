package com.cqt.model.queue.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-07-19 10:17
 * <p>
 *  2.1 传入的技能id与空闲坐席列表比较, 返回拥有该技能id的空闲坐席列表
 * <p>
 *  2.2 查询空闲坐席, 对应技能id的坐席权值哪个最小
 * <p>
 *  2.3 找到技能的坐席权值最高的坐席
 */
@Data
public class MatchAgentWeightVO implements Serializable {

    private static final long serialVersionUID = 2837716360217764704L;

    /**
     * 匹配的坐席-坐席权值最高的坐席
     */
    private List<MatchAgent> matchAgentList;

    /**
     * 存在最高
     */
    private Boolean existMax;

    /**
     * 是否有匹配的坐席
     */
    private Boolean existMatchAgent;

    /**
     * 匹配的坐席id
     */
    private String matchAgentId;

    private String matchPhoneNumber;

    /**
     * 没有匹配的
     */
    public static MatchAgentWeightVO notMatch() {
        MatchAgentWeightVO matchAgentWeightVO = new MatchAgentWeightVO();
        matchAgentWeightVO.setExistMatchAgent(false);
        matchAgentWeightVO.setExistMax(false);
        return matchAgentWeightVO;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MatchAgent implements Serializable {

        private static final long serialVersionUID = 7341707942701541850L;

        /**
         * 坐席id
         */
        private String agentId;

        private String phoneNumber;

        /**
         * 坐席权值
         */
        private Integer agentWeight;

        /**
         * 进入排队时间戳
         * zset的score
         */
        private Long freeTimestamp;
    }
}
