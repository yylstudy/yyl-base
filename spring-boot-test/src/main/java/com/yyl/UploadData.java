package com.yyl;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.linkcircle.basecom.annotation.Dict;
import com.linkcircle.basecom.easyexcel.DictConverter;
import lombok.Data;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/4/7 16:48
 */
@Data
public class UploadData {
    private String username;
    private String sex;
}
