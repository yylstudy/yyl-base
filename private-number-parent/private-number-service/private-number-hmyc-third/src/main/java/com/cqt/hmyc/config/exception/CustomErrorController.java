package com.cqt.hmyc.config.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author linshiqiang
 * @date 2022/3/15 15:10
 */
@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
@Slf4j
public class CustomErrorController extends BasicErrorController {

    public CustomErrorController(ServerProperties serverProperties) {
        super(new DefaultErrorAttributes(), serverProperties.getError());
    }

    /**
     * 覆盖默认的JSON响应
     */
    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>(2);
        body.put("code", 404);
        body.put("message", "Not Found");
        String uri = request.getRequestURI();
        log.error("requestURI: {}, not exist", uri);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    /**
     * 覆盖默认的HTML响应
     */
    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        // 请求的状态
        HttpStatus status = getStatus(request);
        Map<String, Object> model =
                Collections.unmodifiableMap(getErrorAttributes(request, isIncludeStackTrace(request, MediaType.TEXT_HTML)));
        response.setStatus(status.value());
        ModelAndView modelAndView = resolveErrorView(request, response, status, model);
        // 指定自定义的视图
        return (modelAndView != null) ? modelAndView : new ModelAndView("error", model);
    }

}
