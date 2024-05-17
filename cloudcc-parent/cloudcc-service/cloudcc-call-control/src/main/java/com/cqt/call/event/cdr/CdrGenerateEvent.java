package com.cqt.call.event.cdr;

import com.cqt.model.cdr.dto.CdrGenerateDTO;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author linshiqiang
 * date:  2023-07-13 19:11
 */
@Getter
public class CdrGenerateEvent extends ApplicationEvent {

    private final CdrGenerateDTO cdrGenerateDTO;

    public CdrGenerateEvent(Object source, CdrGenerateDTO cdrGenerateDTO) {
        super(source);
        this.cdrGenerateDTO = cdrGenerateDTO;
    }
}
