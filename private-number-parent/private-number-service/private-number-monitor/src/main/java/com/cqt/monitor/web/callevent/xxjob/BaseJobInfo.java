package com.cqt.monitor.web.callevent.xxjob;

import lombok.Data;

import java.io.Serializable;

/**
 * xxl-job 任务操作
 *
 * @author scott
 * @date 2022年07月06日 14:40
 */
@Data
public class BaseJobInfo implements Serializable {

    private static final long serialVersionUID = 1110948979586000968L;

    /**
     * 任务ID
     */
    private Integer id;
}
