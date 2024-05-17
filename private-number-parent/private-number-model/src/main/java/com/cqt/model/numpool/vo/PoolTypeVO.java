package com.cqt.model.numpool.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author hlx
 * @date 2021-12-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoolTypeVO {

    /**
     * 企业id
     */
    private String vccId;

    /**
     * 地市编码
     */
    private String areaCode;

    /**
     * 全部池子
     */
    private Object all;

    /**
     * 主池号码
     */
    private Object master;

    /**
     * 副池号码
     */
    private Object slave;

}
