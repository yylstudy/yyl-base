package com.yyl;

import com.linkcircle.basecom.easyexcel.ExcelUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2024/3/25 16:50
 */
@RestController
public class TestController {
    @GetMapping("exportXlsx")
    public void exportXlsx() {
        DownloadData downloadData = new DownloadData();
        downloadData.setSex("1");
        downloadData.setUsername("yyl");
        ExcelUtil.exportXlsx("测试","111", Arrays.asList(downloadData),DownloadData.class);
    }

    @GetMapping("read")
    public void read() throws Exception{
        FileInputStream fis = new FileInputStream(new File("C:\\Users\\yyl\\Desktop\\1.xlsx"));
        List<UploadData> list = ExcelUtil.read(fis,UploadData.class);
        System.out.println(list);
    }

}
