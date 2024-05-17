package com.cqt.forward.handler;

import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.forward.util.GatewayUtil;
import com.cqt.model.common.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Created with IntelliJ IDEA.
 * ClassName: GlobalExceptionHandler
 *
 * @author linshiqiang@linkcycle.cn
 * Date: 2021-09-29 20:17
 * Description:
 */
@Slf4j
@Order(-1)
@RequiredArgsConstructor
@Component
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    private final GatewayUtil gatewayUtil;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("url: {}, 请求异常: ", gatewayUtil.getRequestPath(exchange), ex);
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return gatewayUtil.responseData(exchange, Result.fail(ErrorCodeEnum.OTHER_ERROR.getCode(),
                    ErrorCodeEnum.OTHER_ERROR.getMessage()));
        }
        // header set
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        if (ex instanceof ResponseStatusException) {
            response.setStatusCode(((ResponseStatusException) ex).getStatus());
            if (HttpStatus.NOT_FOUND.equals(response.getStatusCode())) {
                return gatewayUtil.responseData(exchange, Result.fail(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase()));
            }
        }

        if (ex instanceof JsonProcessingException) {
            return gatewayUtil.responseData(exchange, Result.fail(ErrorCodeEnum.REQUEST_BODY_ERROR.getCode(),
                    ErrorCodeEnum.REQUEST_BODY_ERROR.getMessage()));
        }

        if (ex instanceof HttpServerErrorException) {
            response.setStatusCode(((HttpServerErrorException) ex).getStatusCode());
        }

        if (response.getStatusCode() == null) {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                HttpStatus httpStatus = response.getStatusCode();
                Result fail = Result.fail(httpStatus.value(), httpStatus.getReasonPhrase());
                if (httpStatus == HttpStatus.OK) {
                    fail = Result.fail(ErrorCodeEnum.OTHER_ERROR.getCode(), ErrorCodeEnum.OTHER_ERROR.getMessage());
                }
                return bufferFactory.wrap(objectMapper.writeValueAsBytes(fail));
            } catch (Exception e) {
                log.error("Error writing response", ex);
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }

}
