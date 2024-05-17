package com.cqt.cloudcc.manager.service;

/**
 * @author linshiqiang
 * date:  2023-11-08 16:43
 */
public interface FileInfoService {

    /**
     * 根据文件id查询文件目录
     *
     * @param companyCode 企业id
     * @param fileId      文件id
     * @return 文件目录
     */
    String getFilePath(String companyCode, String fileId);

}
