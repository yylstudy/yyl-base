package com.cqt.cloudcc.manager.event.concurrency;

import com.cqt.model.company.entity.CompanyInfo;
import com.cqt.model.number.entity.NumberInfo;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * date:  2023-10-17 13:54
 */
@Getter
public class ConcurrencyControlEvent extends ApplicationEvent {

    private final String uuid;

    /**
     * 【0:无语音流， 1:只发送语音流不接收， 2:只接收语音流不发送， 3:发送并接收语音流】默认3
     */
    private final Integer audio;

    /**
     * 【0:无视频流， 1:只发送视频流不接收， 2:只接收视频流不发送， 3:发送并接收视频流】默认0
     */
    private final Integer video;

    private final String companyCode;

    private final String number;

    private final NumberInfo numberInfo;

    private final CompanyInfo companyInfo;

    public ConcurrencyControlEvent(Object source,
                                   String uuid,
                                   Integer audio,
                                   Integer video,
                                   String companyCode,
                                   String number,
                                   NumberInfo numberInfo,
                                   CompanyInfo companyInfo) {
        super(source);
        this.uuid = uuid;
        this.audio = audio;
        this.video = video;
        this.companyCode = companyCode;
        this.number = number;
        this.numberInfo = numberInfo;
        this.companyInfo = companyInfo;
    }
}
