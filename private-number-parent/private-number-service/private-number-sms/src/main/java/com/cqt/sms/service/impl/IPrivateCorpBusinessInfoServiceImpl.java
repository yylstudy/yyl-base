package com.cqt.sms.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import com.cqt.sms.service.IPrivateCorpBusinessInfoService;
import com.cqt.sms.dao.mapper.PrivateCorpBusinessInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class IPrivateCorpBusinessInfoServiceImpl extends ServiceImpl<PrivateCorpBusinessInfoMapper, PrivateCorpBusinessInfo> implements IPrivateCorpBusinessInfoService {

}
