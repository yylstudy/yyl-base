package com.cqt.hmyc.web.bind.service.hdh;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.cqt.common.util.PrivateCacheUtil;
import com.cqt.hmyc.config.properties.HdhProperties;
import com.cqt.hmyc.web.corpinfo.mapper.PrivateNumberInfoMapper;
import com.cqt.hmyc.web.model.hdh.push.HdhPushIccpDTO;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.redis.util.RedissonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

/**
 * base
 *
 * @author dingsh
 * date 2022/07/28
 */
@Service
@Slf4j
public class HdhBaseService {

    public static final String CDR_UNBIND_CODE = "0";
    public static final String SDR_UNBIND_CODE = "6";

    public static final String DEFAULT_SUPPLIER_ID = "hdh";

    public final HdhProperties hdhProperties;

    public final RedissonUtil redissonUtil;


    public HdhBaseService(HdhProperties hdhProperties, RedissonUtil redissonUtil) {
        this.hdhProperties = hdhProperties;
        this.redissonUtil = redissonUtil;
    }

    public HdhPushIccpDTO addCqtInfo(HdhPushIccpDTO hdhPushIccpDTO) {
        String bindMapper = PrivateCacheUtil.getBindMapperKey(hdhPushIccpDTO.getBindId());
        String cqtBindMap = redissonUtil.getString(bindMapper);
        if (StringUtils.isBlank(cqtBindMap)) {
            String bindMapperKey = PrivateCacheUtil.getBindMapperKey(DEFAULT_SUPPLIER_ID, hdhPushIccpDTO.getBindId());
            cqtBindMap = redissonUtil.getString(bindMapperKey);
            if (StrUtil.isEmpty(cqtBindMap)) {
                log.info("通过第三方bindId 查询 本平台id为空 ,key : {}", bindMapperKey);
            }
        }
        JSONObject cqtBindJson = JSONObject.parseObject(cqtBindMap);
        String cqtBindID = "";
        String vccId = "";
        String bindTime = "";
        String requestId = "";
        try {
            vccId = cqtBindJson.getString("vccId");
            cqtBindID = cqtBindJson.getString("cqtBindId");
            bindTime = cqtBindJson.getString("bindTime");
            requestId = cqtBindJson.getString("requestId");
        } catch (Exception e) {
            log.error("获取绑定id Exception： ", e);
        }
        //hdhPushIccpDTO.setSupplierId(cqtBindJson.getString("supplierId"));
        hdhPushIccpDTO.setSupplierId("hdh");
        log.info("当前 第三方id:{} 对应的本平台id为：{}", hdhPushIccpDTO.getBindId(), cqtBindID);
        hdhPushIccpDTO.setBindId(cqtBindID);
        hdhPushIccpDTO.setVccId(vccId);
        hdhPushIccpDTO.setBindTime(bindTime);
        hdhPushIccpDTO.setRequestId(requestId);
        return hdhPushIccpDTO;
    }




}
