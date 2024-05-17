package com.cqt.rpc.queue;

/**
 * @author linshiqiang
 * date:  2023-11-23 16:44
 */
public interface CallTaskRemoteService {

    void answerIvrNotice(String companyCode, String taskId, String member);

    void answerPredictNotice(String companyCode, String taskId, String member);

}
