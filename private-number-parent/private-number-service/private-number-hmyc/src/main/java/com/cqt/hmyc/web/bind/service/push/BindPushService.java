package com.cqt.hmyc.web.bind.service.push;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.cloud.api.push.BindPushFeignClient;
import com.cqt.common.enums.NumberTypeEnum;
import com.cqt.hmyc.web.bind.service.axb.AxbBindConverter;
import com.cqt.hmyc.web.corpinfo.service.CorpBusinessService;
import com.cqt.model.bind.ax.entity.PrivateBindInfoAx;
import com.cqt.model.bind.axb.entity.PrivateBindInfoAxb;
import com.cqt.model.bind.axe.entity.PrivateBindInfoAxe;
import com.cqt.model.bind.bo.MqBindInfoBO;
import com.cqt.model.common.Result;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.cqt.model.push.dto.AybBindPushDTO;
import com.cqt.model.push.dto.UnbindPushDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2022/2/28 10:21
 */
@Service
@Slf4j
public class BindPushService {

    @Resource(name = "bindExecutor")
    private ThreadPoolTaskExecutor bindExecutor;

    private final CorpBusinessService corpBusinessService;

    private final AxbBindConverter axbBindConverter;

    private final BindPushFeignClient bindPushFeignClient;

    public BindPushService(CorpBusinessService corpBusinessService, AxbBindConverter axbBindConverter, BindPushFeignClient bindPushFeignClient) {
        this.corpBusinessService = corpBusinessService;
        this.axbBindConverter = axbBindConverter;
        this.bindPushFeignClient = bindPushFeignClient;
    }

    /**
     * Ayb 绑定通知
     */
    public void pushAybBind(Result result, PrivateBindInfoAxe bindInfoAxe) {
        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = corpBusinessService.getCorpBusinessInfo(bindInfoAxe.getVccId());
        if (!businessInfoOptional.isPresent()) {
            return;
        }
        PrivateCorpBusinessInfoDTO businessInfoDTO = businessInfoOptional.get();
        String aybBindPushUrl = businessInfoDTO.getAybBindPushUrl();
        if (StrUtil.isEmpty(aybBindPushUrl)) {
            return;
        }

        bindExecutor.execute(() -> {
            //  http
            Object data = result.getData();
            if (!(data instanceof PrivateBindInfoAxb)) {
                return;
            }
            PrivateBindInfoAxb privateBindInfoAxb = (PrivateBindInfoAxb) data;
            AybBindPushDTO aybBindPushDTO = axbBindConverter.bindInfoAxb2AybBindPushDTO(privateBindInfoAxb);
            // TODO 改为AXE_AYB类型?
            aybBindPushDTO.setType(NumberTypeEnum.AXEYB_AYB.name());
            Date bindTime = ObjectUtil.isNull(privateBindInfoAxb.getCreateTime()) ? DateUtil.date() : privateBindInfoAxb.getCreateTime();
            long epochSecond = bindTime.toInstant().getEpochSecond();
            aybBindPushDTO.setBindTime(epochSecond);
            Result resultAyb = bindPushFeignClient.pushAybBind(aybBindPushDTO);
            log.info("bind ayb push : {}", resultAyb);
        });
    }

    /**
     * 解绑推送
     */
    public void pushUnBind(MqBindInfoBO mqBindInfoBO) {

        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = corpBusinessService.getCorpBusinessInfo(mqBindInfoBO.getVccId());
        if (!businessInfoOptional.isPresent()) {
            return;
        }
        PrivateCorpBusinessInfoDTO businessInfoDTO = businessInfoOptional.get();
        // 解绑推送配置不推送则不推送
        if (ObjectUtil.isNotEmpty(businessInfoDTO.getUnBindPushFlag())) {
            if (1 != businessInfoDTO.getUnBindPushFlag()) {
                return;
            }
        }
        
        //  http
        bindExecutor.execute(() -> {
            UnbindPushDTO unbindPushDTO = new UnbindPushDTO();
            NumberTypeEnum numberTypeEnum = NumberTypeEnum.valueOf(mqBindInfoBO.getNumType());
            switch (numberTypeEnum) {
                case AXE:
                    PrivateBindInfoAxe bindInfoAxe = mqBindInfoBO.getPrivateBindInfoAxe();
                    if (ObjectUtil.isNull(bindInfoAxe)) {
                        return;
                    }
                    unbindPushDTO.setBindId(bindInfoAxe.getBindId());
                    unbindPushDTO.setRequestId(bindInfoAxe.getRequestId());
                    break;
                case AX:
                    PrivateBindInfoAx privateBindInfoAx = mqBindInfoBO.getPrivateBindInfoAx();
                    if (ObjectUtil.isNull(privateBindInfoAx)) {
                        return;
                    }
                    unbindPushDTO.setBindId(privateBindInfoAx.getBindId());
                    unbindPushDTO.setRequestId(privateBindInfoAx.getRequestId());
                    break;
                case AXB:
                    PrivateBindInfoAxb bindInfoAxb = mqBindInfoBO.getPrivateBindInfoAxb();
                    if (ObjectUtil.isNull(bindInfoAxb)) {
                        return;
                    }
                    String bindId = bindInfoAxb.getBindId();
                    // TODO axe-ayb解绑通知推送
                    unbindPushDTO = axbBindConverter.bindInfoAxb2UnbindPushDTO(bindInfoAxb);

                    break;
                default:
                    return;
            }
            unbindPushDTO.setVccId(mqBindInfoBO.getVccId());
            unbindPushDTO.setType(mqBindInfoBO.getNumType());
            unbindPushDTO.setNumberType(mqBindInfoBO.getNumType());
            unbindPushDTO.setUnbindTime(Instant.now().getEpochSecond());
            Result result = bindPushFeignClient.pushUnBind(unbindPushDTO);
            log.info("unbind push : {}", result);
        });
    }
}
