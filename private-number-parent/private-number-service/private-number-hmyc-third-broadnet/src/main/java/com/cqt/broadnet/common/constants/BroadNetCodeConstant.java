package com.cqt.broadnet.common.constants;

/**
 * 广电接口状态码定义
 *
 * @author Xienx
 * @date 2023-05-26 14:36:14:36
 */
public interface BroadNetCodeConstant {

    /**
     * 200 - Success
     */
    Integer SUCCESS = 200;

    /**
     * 400 - Bad Request
     */
    Integer BAD_REQUEST = 400;

    /**
     * 401 - Unauthorized
     */
    Integer UNAUTHORIZED = 401;

    /**
     * 404 - Not Found
     */
    Integer NOT_FOUND = 404;

    /**
     * 405 - Method Not Allowed
     */
    Integer NOT_ALLOWED = 405;

    /**
     * 409 - X isn't find
     */
    Integer X_IS_NOT_FIND = 409;

    /**
     * 415 - Unsupported Media Type
     */
    Integer UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * 503 - Internal Server Error
     */
    Integer INTERNAL_SERVER_ERROR = 503;

    /**
     * 601 - X NOT EXIST
     */
    Integer X_NOT_EXIST = 601;

    /**
     * 602 - X is abnormal
     */
    Integer X_ABNORMAL = 602;

    /**
     * 603 - X has been used by A or B
     */
    Integer X_HAS_BEEN_USED = 603;

    /**
     * 611 - Failed to modify number
     */
    Integer FAILED_MODIFY_NUMBER = 611;

    /**
     * 612 - Failed to modify expiration
     */
    Integer FAILED_MODIFY_EXPIRATION = 612;

    /**
     * 621 - X is abnormal and assign a new X
     */
    Integer X_ABNORMAL_AND_ASSIGN_A_NEW_X = 621;

    /**
     * 622 - X is abnormal and failed to assign new X
     */
    Integer X_ABNORMAL_AND_FAILED_ASSGING_NEW_X = 622;


    /**
     * 13001 - The binding already exists
     */
    Integer BIND_ALREADY_EXIST = 13001;
}
