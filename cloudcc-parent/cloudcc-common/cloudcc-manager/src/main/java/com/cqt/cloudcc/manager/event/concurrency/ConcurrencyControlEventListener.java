package com.cqt.cloudcc.manager.event.concurrency;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.CommonConstant;
import com.cqt.base.enums.MediaStreamEnum;
import com.cqt.base.enums.cdr.HangupCauseEnum;
import com.cqt.base.exception.ConcurrencyLimitException;
import com.cqt.cloudcc.manager.service.CommonDataOperateService;
import com.cqt.cloudcc.manager.service.FreeswitchRequestService;
import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.freeswitch.base.FreeswitchApiBase;
import com.cqt.model.freeswitch.dto.api.HangupDTO;
import com.cqt.model.freeswitch.vo.CompanyConcurrencyVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author linshiqiang
 * date:  2023-10-17 14:08
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConcurrencyControlEventListener {

    private final FreeswitchRequestService freeswitchRequestService;

    private final CommonDataOperateService commonDataOperateService;

    /**
     * 监听
     */
    @EventListener(classes = {ConcurrencyControlEvent.class})
    public void listener(ConcurrencyControlEvent event) {

        // 号码并发
        numberConcurrencyControl(event);

        // 企业并发
        companyConcurrencyControl(event);

    }

    private void numberConcurrencyControl(ConcurrencyControlEvent event) {
        // TODO 号码并发
    }

    private void companyConcurrencyControl(ConcurrencyControlEvent event) {
        CompanyInfo companyInfo = getCompanyInfo(event);
        if (Objects.isNull(companyInfo)) {
            return;
        }
        if (!CommonConstant.ENABLE_Y.equals(companyInfo.getIsControl())) {
            return;
        }
        String companyCode = event.getCompanyCode();
        FreeswitchApiBase apiBase = FreeswitchApiBase.build(companyCode);
        CompanyConcurrencyVO onlineCompanyConcurrency = freeswitchRequestService.getOnlineCompanyConcurrency(apiBase);
        if (!Boolean.TRUE.equals(onlineCompanyConcurrency.getResult())) {
            return;
        }
        Integer audioConcurrency = onlineCompanyConcurrency.getAudioConcurrency();
        Integer videoConcurrency = onlineCompanyConcurrency.getVideoConcurrency();
        if (isVideo(event)) {
            Integer companyVideoConcurrencyMax = companyInfo.getVideoConcurrency();
            if (Objects.isNull(companyVideoConcurrencyMax)) {
                return;
            }
            if (videoConcurrency > companyVideoConcurrencyMax) {
                hangup(event, true);
                throw new ConcurrencyLimitException(StrFormatter.format("企业: {}, 视频呼叫达到最大并发", companyInfo.getCompanyCode()));
            }
            return;
        }
        Integer companyAudioConcurrencyMax = companyInfo.getAudioConcurrency();
        if (Objects.isNull(companyAudioConcurrencyMax)) {
            return;
        }
        if (audioConcurrency > companyAudioConcurrencyMax) {
            hangup(event, false);
            throw new ConcurrencyLimitException(StrFormatter.format("企业: {}, 音频呼叫达到最大并发", companyInfo.getCompanyCode()));
        }
    }

    private void hangup(ConcurrencyControlEvent event, boolean isVideo) {
        String uuid = event.getUuid();
        if (StrUtil.isEmpty(uuid)) {
            return;
        }
        String companyCode = event.getCompanyCode();
        HangupCauseEnum hangupCauseEnum = HangupCauseEnum.AUDIO_CONCURRENCY_LIMIT_MAX;
        if (isVideo) {
            hangupCauseEnum = HangupCauseEnum.VIDEO_CONCURRENCY_LIMIT_MAX;
        }
        HangupDTO hangupDTO = HangupDTO.build(companyCode, uuid, hangupCauseEnum);
        freeswitchRequestService.hangup(hangupDTO);
    }

    private boolean isVideo(ConcurrencyControlEvent event) {
        return MediaStreamEnum.SENDRECV.getCode().equals(event.getVideo());
    }

    private CompanyInfo getCompanyInfo(ConcurrencyControlEvent event) {
        CompanyInfo companyInfo = event.getCompanyInfo();
        if (Objects.isNull(companyInfo)) {
            // 查询企业信息
            String companyCode = event.getCompanyCode();
            return commonDataOperateService.getCompanyInfoDTO(companyCode);
        }
        return companyInfo;
    }
}
