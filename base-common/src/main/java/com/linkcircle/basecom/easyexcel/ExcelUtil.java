package com.linkcircle.basecom.easyexcel;

import cn.hutool.json.JSON;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description excel工具类
 * @createTime 2024/3/25 16:53
 */
@Slf4j
public class ExcelUtil {
    /**
     * 导出Xls文件
     * @param fileName
     * @param sheetName
     * @param list
     * @param clazz
     */
    public static <T> void exportXls(String fileName,String sheetName, List<T> list,Class<T> clazz){
        exportExcel(fileName,sheetName,list,clazz,"xls");
    }

    /**
     * 导出Xlsx文件
     * @param fileName
     * @param sheetName
     * @param list
     * @param clazz
     */
    public static <T> void exportXlsx(String fileName,String sheetName, List<T> list,Class<T> clazz){
        exportExcel(fileName,sheetName,list,clazz,"xlsx");
    }

    /**
     * 从流中读取文件
     * @param inputStream
     * @param clazz
     * @return
     * @throws IOException
     */
    public static <T> List<T> read(InputStream inputStream,Class<T> clazz) {
        List<T> list = new ArrayList<>();
        EasyExcel.read(inputStream, clazz, new PageReadListener<T>(dataList -> {
            list.addAll(dataList);
        })).sheet().doRead();
        return list;
    }



    private static <T> void exportExcel(String fileName,String sheetName, List<T> list,Class<T> clazz,String fileNameSuffix) {
        ServletRequestAttributes servletRequestAttributes =  (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        try{
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            //防止中文乱码
            fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + "."+fileNameSuffix);
            EasyExcel.write(response.getOutputStream(),clazz)
                    .autoCloseStream(false).sheet(sheetName).doWrite(list);
        }catch (Exception e){
            log.error("导出失败",e);
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Result result = Result.error("导出失败，原因："+e.getMessage());
            try {
                response.getWriter().println(JsonUtil.toJSONString(result));
            } catch (IOException ioException) {
                log.error("ioException",ioException);
            }
        }
    }
}
