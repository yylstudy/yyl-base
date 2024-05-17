package com.cqt.broadnet.common.constants;

/**
 * 广电请求头的定义
 *
 * @author Xienx
 * @date 2023-05-25 16:43:16:43
 */
public interface BroadNetHeaderConstant {

    /**
     * 广电中台能力平台给App分配的AppKey
     */
    String X_APP_ID = "X-APP-ID";

    /**
     * 第三方APP内部自己定义的业务Id，固定不变；AXB APP发给第三方APP的消息中固定携带此Id
     */
    String X_APP_bizId = "X-APP-bizId";

    /**
     * 第三方APP内部自己定义的消息头。
     */
    String X_AXYB_PROVIDER_HINT = "X-Axyb-Provider-Hint";

    /**
     * 第三方APP内部分配给AXB APP的唯一Id。
     */
    String X_PROVIDER_ID = "X-Provider-Id";
}
