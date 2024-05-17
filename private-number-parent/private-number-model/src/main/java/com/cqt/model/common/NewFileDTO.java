package com.cqt.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author linshiqiang
 * @since 2022/7/28 14:03
 * 新建文件参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewFileDTO {

    private String filePath;

    private String xml;
}
