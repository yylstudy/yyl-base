package com.cqt.unicom.service;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.ControlOperateEnum;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.common.ResultVO;
import com.cqt.model.unicom.dto.NumberBindingQueryDTO;
import com.cqt.model.unicom.entity.UnicomCommonEnum;
import com.cqt.model.unicom.vo.DgtsEventInfo;
import com.cqt.model.unicom.vo.GeneralMessageVO;
import com.cqt.model.unicom.vo.NumberBindingQueryVO;
import com.cqt.unicom.config.cache.UnicomLocalCacheService;
import com.cqt.unicom.config.nacos.NacosConfig;
import com.cqt.unicom.properties.DigitEventInfoProperties;
import com.cqt.unicom.util.UnicomUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author zhengsuhao
 * @date 2022/12/5
 */
@Api(tags = "联通集团总部(江苏)能力:查询绑定关系服务实现")
@Slf4j
@Service
@RequiredArgsConstructor
public class UnicomNumberBindServiceImpl implements UnicomNumberBindService {

    private final NacosConfig nacosConfig;

    private final ObjectMapper objectMapper;

    private final DigitEventInfoProperties digitEventInfoProperties;

    /**
     * 查询号码绑定关系服务实现
     *
     * @param numberBindingQueryDTO 联通集团总部(江苏)号码绑定查询入参
     * @return NumberBindingQueryVO
     */
    @Override
    public GeneralMessageVO getNumberBindingQuery(NumberBindingQueryDTO numberBindingQueryDTO) throws JsonProcessingException {
        if (log.isInfoEnabled()) {
            log.info("江苏联通查询绑定关系入参：{}", JSON.toJSONString(numberBindingQueryDTO));
        }
        String callId = numberBindingQueryDTO.getCallId();
        String phoneNumberA = numberBindingQueryDTO.getPhoneNumberA();
        String phoneNumberX = numberBindingQueryDTO.getPhoneNumberX();
        String bindInfoApiQueryString = JSON.toJSONString(numberBindingQueryDTO.buildBindInfoApiQuery());
        String bindInfoData;
        try {
            bindInfoData = HttpUtil.post(nacosConfig.getBindnumerUrl(), bindInfoApiQueryString, 5000);
        } catch (Exception e) {
            // 接口调用失败
            log.error("调用内部查询绑定关系接口: {}, 异常: ", nacosConfig.getBindnumerUrl(), e);
            return GeneralMessageVO.fail("500", NumberBindingQueryVO.notBind(phoneNumberA, phoneNumberX, ""));
        }
        ResultVO<BindInfoApiVO> bindInfoApiVOResultVO = objectMapper.readValue(bindInfoData, new TypeReference<ResultVO<BindInfoApiVO>>() {
        });
        log.info("callId: {}, 内部接口查询绑定关系结果返回: {}", callId, JSON.toJSONString(bindInfoApiVOResultVO));
        BindInfoApiVO bindInfoApiVO = bindInfoApiVOResultVO.getData();
        String controlOperate = bindInfoApiVO.getControlOperate();
        // 通话拦截
        if (ControlOperateEnum.REJECT.name().equals(controlOperate)) {
            NumberBindingQueryVO notBind = NumberBindingQueryVO.notBind(phoneNumberA, phoneNumberX, FileNameUtil.mainName(bindInfoApiVO.getCallerIvr()));
            log.info("callId: {}, 无绑定关系结果返回：{}", callId, JSON.toJSONString(notBind));
            return GeneralMessageVO.fail("500", notBind);
        }

        NumberBindingQueryVO numberBindingQueryVO = buildBaseNumberBindingQueryVO(phoneNumberA, phoneNumberX, bindInfoApiVO);

        // 收号参数
        if (ControlOperateEnum.IVR.name().equals(controlOperate)) {
            buildDigitEventInfo(numberBindingQueryVO);
            numberBindingQueryVO.setAudioCode(UnicomLocalCacheService.getIvrCode(bindInfoApiVO.getCallerIvr()) + "@,0,0");
        }
        log.info("bind_id:{}", bindInfoApiVO.getBindId());
        log.info("callId: {}, 查询绑定关系结果返回：{}", callId, JSON.toJSONString(numberBindingQueryVO));
        return GeneralMessageVO.ok("成功", numberBindingQueryVO);
    }

    /**
     * ivr收号参数配置
     */
    private void buildDigitEventInfo(NumberBindingQueryVO numberBindingQueryVO) {
        DgtsEventInfo dgtsEventInfo = new DgtsEventInfo();
        dgtsEventInfo.setControl(Integer.parseInt(digitEventInfoProperties.getControl(), 2));
        dgtsEventInfo.setMinCollect(digitEventInfoProperties.getMinCollect());
        dgtsEventInfo.setMaxCollect(digitEventInfoProperties.getMaxCollect());
        dgtsEventInfo.setMaxInteractTime(digitEventInfoProperties.getMaxInteractTime());
        dgtsEventInfo.setInitInterDgtTime(digitEventInfoProperties.getInitInterDgtTime());
        dgtsEventInfo.setNormInterDgtTime(digitEventInfoProperties.getNormInterDgtTime());
        dgtsEventInfo.setEnterDgtMask(digitEventInfoProperties.getEnterDgtMask());
        dgtsEventInfo.setDigitCollectionType(digitEventInfoProperties.getDigitCollectionType());
        numberBindingQueryVO.setDgtsEventInfo(dgtsEventInfo);
    }

    private NumberBindingQueryVO buildBaseNumberBindingQueryVO(String phoneNumberA, String phoneNumberX, BindInfoApiVO bindInfoApiVO) {
        NumberBindingQueryVO numberBindingQueryVO = new NumberBindingQueryVO();
        // 插入主叫号码
        numberBindingQueryVO.setPhoneNumberA(phoneNumberA);
        // 插入小号号码
        numberBindingQueryVO.setPhoneNumberX(phoneNumberX);
        // 插入对端显示小号号码
        numberBindingQueryVO.setPhoneNumberY(bindInfoApiVO.getDisplayNum());
        // 插入对端号码
        numberBindingQueryVO.setPhoneNumberB(bindInfoApiVO.getCalledNum());
        // 插入放音编码
        numberBindingQueryVO.setAudioCode(UnicomUtil.playbackCompiler(StringUtil.isBlank(bindInfoApiVO.getCallerIvr()) ? "" : bindInfoApiVO.getCallerIvr(),
                StringUtil.isBlank(bindInfoApiVO.getCalledIvr()) ? "" : bindInfoApiVO.getCalledIvr(),
                StringUtil.isBlank(bindInfoApiVO.getCallerIvrBefore()) ? "" : bindInfoApiVO.getCallerIvrBefore()));
        // 插入录音控制
        if (bindInfoApiVO.getEnableRecord() == null) {
            numberBindingQueryVO.setCallRecording("0");
        } else {
            numberBindingQueryVO.setCallRecording(String.valueOf(bindInfoApiVO.getEnableRecord()));
        }
        // 插入录音文件格式
        if (String.valueOf(UnicomCommonEnum.MP3.getValue()).equals(bindInfoApiVO.getRecordFileFormat())) {
            numberBindingQueryVO.setCallRecordingFileFormat(0);
        } else if (String.valueOf(UnicomCommonEnum.WAV.getValue()).equals(bindInfoApiVO.getRecordFileFormat())) {
            numberBindingQueryVO.setCallRecordingFileFormat(1);
        }
        // 插入来显控制，固定为0,0
        numberBindingQueryVO.setCallDisplay("0,0");
        // 插入录音模式
//        if (bindInfoApiVO.getRecordMode() != null) {
//            if (UnicomCommonEnum.ZERO.getValue().equals(String.valueOf(bindInfoApiVO.getRecordMode()))) {
//                numberBindingQueryVO.setCallRecordingMode("0");
//            } else {
//                if (UnicomCommonEnum.ZERO.getValue().equals(String.valueOf(bindInfoApiVO.getDualRecordMode()))) {
//                    numberBindingQueryVO.setCallRecordingMode("4");
//                } else if (UnicomCommonEnum.ONE.getValue().equals(String.valueOf(bindInfoApiVO.getDualRecordMode()))) {
//                    numberBindingQueryVO.setCallRecordingMode("5");
//                }
//            }
//        }
        // 插入附加数据
        HashMap<Object, Object> additionalData = new HashMap<>();
        additionalData.put("bind_id", bindInfoApiVO.getBindId());
        additionalData.put("vccId", bindInfoApiVO.getVccId());
        additionalData.put("userData", bindInfoApiVO.getUserData());
        numberBindingQueryVO.setAdditionalData(JSON.toJSONString(additionalData));
        return numberBindingQueryVO;
    }


}
