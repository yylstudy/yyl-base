package com.cqt.model.agent.dto;

import cn.hutool.core.util.StrUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-07-04 9:38
 * 呼入客户排队等级队列 zset成员
 * uuid + number + timestamp + skillId + maxRetry
 */
@Data
public class UserQueueLevelMemberDTO implements Serializable {

    private static final long serialVersionUID = 2239252147995815021L;

    private String uuid;

    private String number;

    private Long timestamp;

    private String skillId;

    private Integer maxRetry;

    /**
     * 构建
     */
    public UserQueueLevelMemberDTO build(String member) {
        List<String> list = StrUtil.split(member, StrUtil.C_AT);
        if (list.size() != 5) {
            return null;
        }
        UserQueueLevelMemberDTO dto = new UserQueueLevelMemberDTO();
        dto.setUuid(list.get(0));
        dto.setNumber(list.get(1));
        dto.setTimestamp(Long.parseLong(list.get(2)));
        dto.setSkillId(list.get(3));
        dto.setMaxRetry(Integer.parseInt(list.get(4)));
        return dto;
    }

}
