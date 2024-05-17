package com.cqt.ivr.mapper;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.cqt.ivr.entity.IvrInfo;
import com.cqt.ivr.entity.dto.BuryingPointReq;
import com.cqt.ivr.entity.dto.CommIvrReq;
import com.cqt.ivr.entity.dto.QueueStatusReq;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@DS("cdr")
@Mapper
public interface BuryingPointDao {

    void insertAllsByMonth(BuryingPointReq req);

    public List<BuryingPointReq> getBuryingPointInfo(BuryingPointReq req);

    void superManagerSelect(String str);

}
