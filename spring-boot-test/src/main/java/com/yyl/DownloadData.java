package com.yyl;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.linkcircle.basecom.annotation.Dict;
import com.linkcircle.basecom.easyexcel.DictConverter;
import lombok.Data;
import org.apache.poi.ss.usermodel.Font;

import java.util.Date;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/25 16:48
 */
@Data
public class DownloadData {
    @ColumnWidth(30)
    @ExcelProperty("姓名")
    private String username;
    @ExcelProperty(value = "性别",converter = DictConverter.class)
    @ColumnWidth(20)
    @Dict(dictCode = "sex")
    private String sex;
}
