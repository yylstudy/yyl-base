package com.cqt.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 号码拨测常量定义
 *
 * @author Xienx
 * @date 2022-07-08
 */
public interface HmbcConstants {

    /**
     * 拨测号码范围 1 - 指定号码
     */
    Integer NUMBER_RANGE_CUSTOMIZE = 1;

    /**
     * 拨测任务执行状态 0 - 进行中
     */
    Integer DIAL_TEST_TASK_EXECUTION_STATE_EXECUTING = 0;

    /**
     * 拨测任务执行状态 1 - 已完成
     */
    Integer DIAL_TEST_TASK_EXECUTION_STATE_FINISH = 1;

    /**
     * 拨测类型常量定义
     */
    interface DialTestType {
        /**
         * 1 - 隐私号拨测
         */
        Integer PRIVACY_NUMBER_DIAL_TEST = 1;

        /**
         * 2 - 位置更新拨测
         */
        Integer LOCATION_UPDATE_DIAL_TEST = 2;
    }

    /**
     * 拨测状态常量定义
     */
    @Getter
    @AllArgsConstructor
    enum DialTestState {

        /**
         * 0 - 失败
         */
        FAILED(0, "失败"),

        /**
         * 1 - 成功
         */
        SUCCESS(1, "成功"),

        /**
         * 2 - 恢复
         */
        RECOVERY(2, "恢复");

        private final Integer code;
        private final String text;
    }


    /**
     * 企业拨测结果推送类型常量定义
     */
    @Getter
    @AllArgsConstructor
    enum DialTestPushType {

        /**
         * -1 - 不推送拨测结果
         */
        NOTHING_PUSH(-1, "不推送拨测结果"),

        /**
         * 0 - 仅推送异常结果
         */
        EXCEPTION_RESULT_PUSH(0, "仅推送异常结果"),

        /**
         * 1 - 推送全部结果
         */
        ALL_RESULT_PUSH(1, "推送全部结果");

        private final Integer code;
        private final String text;
        /**
         * 枚举缓存
         */
        private static final Map<Integer, String> ENUM_MAPPING;

        static {
            ENUM_MAPPING = new HashMap<>();
            for (DialTestPushType item : DialTestPushType.values()) {
                if (ENUM_MAPPING.put(item.getCode(), item.getText()) != null) {
                    throw new IllegalArgumentException("duplicate code:" + item.getCode());
                }
            }
        }

        /**
         * 根据code获取枚举值
         *
         * @param code 枚举code
         * @return 枚举描述
         */
        public static String getByCode(Integer code) {
            return ENUM_MAPPING.get(code);
        }

    }
}
