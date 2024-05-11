package com.alibaba.otter.manager.deployer;

import com.alibaba.citrus.webx.servlet.FilterBean;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.entity.ContentType;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description TODO
 * @createTime 2022/3/31 9:41
 */

public class LinkcircleMonitorFilter extends FilterBean {
    private LinkcircleMonitor linkcircleMonitor = new LinkcircleMonitor();

    @Override
    protected void doFilter(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws IOException, ServletException {
        ServletOutputStream os = null;
        JSONObject obj = new JSONObject();
        httpServletResponse.setContentType(ContentType.APPLICATION_JSON.toString());
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        try{
            os = httpServletResponse.getOutputStream();
            String monitorStr = linkcircleMonitor.getMonitorStr();
            if(StringUtils.isEmpty(monitorStr)){
                obj.put("code","0");
                obj.put("message","otter正常");
            }else{
                obj.put("code","-1");
                obj.put("message",monitorStr);
            }
            os.write(obj.toJSONString().getBytes("UTF-8"));
            os.flush();
        }catch (Exception e){
            obj.put("code","-1");
            log.error("get otter monitor error");
            obj.put("message",e.getMessage());
            os.write(obj.toJSONString().getBytes("UTF-8"));
            os.flush();
        }finally {
            if(os!=null){
                os.close();
            }
        }
    }
}
