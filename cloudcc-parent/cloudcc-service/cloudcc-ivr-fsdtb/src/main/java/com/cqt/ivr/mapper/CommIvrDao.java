package com.cqt.ivr.mapper;


import com.cqt.ivr.entity.IvrInfo;
import com.cqt.ivr.entity.QueueAgentInfo;
import com.cqt.ivr.entity.dto.BuryingPointReq;
import com.cqt.ivr.entity.dto.CommIvrReq;
import com.cqt.ivr.entity.dto.QueueStatusReq;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface CommIvrDao {

    List<IvrInfo> getIvrInfoList(CommIvrReq commIvrReq);

    void superManagerSelect(String str);

    public String getQueueNameBySysQueueId(QueueStatusReq req);

}
