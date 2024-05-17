package com.cqt.hmyc.web.blacklist.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqt.hmyc.web.blacklist.model.entity.CallerNumberBlacklist;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2024-02-04 10:12
 */
@Mapper
@RequestMapping("/api/v1/blacklist")
public interface CallerNumberBlacklistMapper extends BaseMapper<CallerNumberBlacklist> {

    void batchInsert(List<CallerNumberBlacklist> list);

}
