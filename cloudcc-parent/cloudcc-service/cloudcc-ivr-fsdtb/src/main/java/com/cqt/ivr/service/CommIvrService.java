package com.cqt.ivr.service;


import com.cqt.ivr.entity.QueueAgentInfo;
import com.cqt.ivr.entity.dto.BuryingPointReq;
import com.cqt.ivr.entity.dto.CommIvrReq;
import com.cqt.ivr.entity.dto.QueueStatusReq;
import com.cqt.ivr.entity.vo.TableResult;

import java.util.List;

public interface CommIvrService {

    TableResult getIvrInfoList(CommIvrReq commIvrReq, String LOG_TAG);

    void insertAllsByMonth(BuryingPointReq req, String LOG_TAG);

    void superManagerSelect(String str);

    public List<BuryingPointReq> getBuryingPointInfo(BuryingPointReq req);

    public String getQueueNameBySysQueueId(QueueStatusReq req);


}
