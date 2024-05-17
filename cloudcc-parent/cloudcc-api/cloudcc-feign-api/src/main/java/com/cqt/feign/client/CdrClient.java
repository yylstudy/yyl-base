package com.cqt.feign.client;

import com.cqt.model.cdr.dto.RemoteCdrDTO;
import com.cqt.model.cdr.vo.RemoteCdrVO;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@RefreshScope
@FeignClient(name = "AUTH-SERVICE", url = "http://172.16.250.218/jeecgboot/aftersale/")
public interface CdrClient {
    /**
     * 发送外部话单
     */
    @PostMapping()
    RemoteCdrVO sendCdr(URI baseUri, @RequestBody RemoteCdrDTO remoteCdrDTO);


    /**
     * 发送质检话单
     */
    @PostMapping
    <T>RemoteCdrVO sendQualityCdr(URI baseUri, @RequestBody T pushInfo);

}