package com.cqt.push.mapper;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cqt.model.corpinfo.entity.PrivateCorpBusinessInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
public class IPrivateCorpBusinessInfoServiceImpl extends ServiceImpl<PrivateCorpBusinessInfoMapper, PrivateCorpBusinessInfo> implements IPrivateCorpBusinessInfoService {

}
