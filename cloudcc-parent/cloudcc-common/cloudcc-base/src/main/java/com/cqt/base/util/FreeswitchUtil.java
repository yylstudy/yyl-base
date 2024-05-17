package com.cqt.base.util;

import cn.hutool.core.util.StrUtil;
import com.cqt.base.contants.BaseErrorMsgConstant;

/**
 * @author linshiqiang
 * date:  2023-08-18 16:48
 */
public class FreeswitchUtil {

    /**
     * 通话id不存在
     *
     * @param msg fs消息
     * @return true/false
     */
    public static boolean isInvalidUuid(String msg) {
        if (StrUtil.isEmpty(msg)) {
            return true;
        }

        return msg.contains(BaseErrorMsgConstant.CALL_NOT_EXIST)
                || msg.contains(BaseErrorMsgConstant.NOT_FIND_UUID);
    }
}
