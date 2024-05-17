package com.cqt.cdr.cloudccsfaftersales.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActionForward {
    private String resultno;
    private String resultmsg;

    public static ActionForward getActionForward(String message) {
        ActionForward actionForward = new ActionForward();
        actionForward.setResultmsg(message);
        actionForward.setResultno("-1");
        return actionForward;
    }
}
