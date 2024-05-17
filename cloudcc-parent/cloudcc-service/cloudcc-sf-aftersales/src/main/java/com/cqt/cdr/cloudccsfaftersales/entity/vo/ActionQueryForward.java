package com.cqt.cdr.cloudccsfaftersales.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActionQueryForward extends ActionForward {
    //    private String resultno;
//    private String resultmsg;
    private String seat_name;
    private String seat_id;
    private String seat_group;

//    public static ActionQueryForward getActionForward(String message) {
//        ActionQueryForward actionForward = new ActionQueryForward();
//        actionForward.setResultmsg(message);
//        actionForward.setResultno("-1");
//        return actionForward;
//    }
}
