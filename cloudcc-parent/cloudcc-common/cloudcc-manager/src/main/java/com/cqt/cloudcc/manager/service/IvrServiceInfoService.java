package com.cqt.cloudcc.manager.service;

import com.cqt.model.queue.entity.IvrServiceInfo;

/**
 * @author linshiqiang
 * date:  2023-10-09 14:27
 * @since 7.0.0
 */
public interface IvrServiceInfoService {

    IvrServiceInfo getIvrServiceInfo(String serviceId);

}
