package com.linkcircle.gateway.config;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.HtmlUtils;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Map;

/**
 * @author yang.yonglian
 * @version 1.0.0
 * @Description 自定义全局异常处理器，主要是重写getHttpStatus，因为这边定义的json返回没有status字段
 *              所以使用DefaultErrorWebExceptionHandler时会报错，这个是要需要使用GlobalErrorAttributes中已经定义的字段
 *              这边是code,还有timestamp原先是Date类型，GlobalErrorAttributes中被我们修改为long，所以这里要重写renderDefaultErrorView 方法
 * @createTime 2023/4/26 11:03
 */

public class GlobalErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {
    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources,
                                          ErrorProperties errorProperties, ApplicationContext applicationContext) {
        super(errorAttributes, resources,errorProperties, applicationContext);
    }
    @Override
    protected int getHttpStatus(Map<String, Object> errorAttributes) {
        return (int) errorAttributes.get("code");
    }

    @Override
    protected Mono<ServerResponse> renderDefaultErrorView(ServerResponse.BodyBuilder responseBody, Map<String, Object> error) {
        StringBuilder builder = new StringBuilder();
        Date timestamp = new Date((long) error.get("timestamp"));
        Object message = error.get("message");
        Object trace = error.get("trace");
        Object requestId = error.get("requestId");
        builder.append("<html><body><h1>Whitelabel Error Page</h1>")
                .append("<p>This application has no configured error view, so you are seeing this as a fallback.</p>")
                .append("<div id='created'>").append(timestamp).append("</div>").append("<div>[")
//                .append(requestId)
                .append("] There was an unexpected error (type=")
//                .append(htmlEscape(error.get("error")))
                .append(", status=").append(htmlEscape(error.get("code"))).append(").</div>");
        if (message != null) {
            builder.append("<div>").append(htmlEscape(message)).append("</div>");
        }
        if (trace != null) {
            builder.append("<div style='white-space:pre-wrap;'>").append(htmlEscape(trace)).append("</div>");
        }
        builder.append("</body></html>");
        return responseBody.bodyValue(builder.toString());
    }

    private String htmlEscape(Object input) {
        return (input != null) ? HtmlUtils.htmlEscape(input.toString()) : null;
    }
}
