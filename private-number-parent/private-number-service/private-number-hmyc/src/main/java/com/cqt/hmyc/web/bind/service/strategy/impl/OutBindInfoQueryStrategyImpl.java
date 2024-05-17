package com.cqt.hmyc.web.bind.service.strategy.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.cqt.common.enums.BusinessTypeEnum;
import com.cqt.hmyc.web.bind.service.strategy.BindInfoQueryStrategy;
import com.cqt.model.bind.query.BindInfoQuery;
import com.cqt.model.bind.vo.BindInfoVO;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author linshiqiang
 * @date 2021/10/25 11:19
 * 绑定关系在企业侧 绑定关系查询
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OutBindInfoQueryStrategyImpl implements BindInfoQueryStrategy {

    public static final String BUSINESS_TYPE = BusinessTypeEnum.X.name();

    private final HideProperties hideProperties;

    @Override
    public Optional<BindInfoVO> query(BindInfoQuery bindInfoQuery, Optional<PrivateCorpBusinessInfoDTO> corpBusinessInfoOptional) {
        if (!corpBusinessInfoOptional.isPresent()) {

            return Optional.empty();
        }
        PrivateCorpBusinessInfoDTO businessInfoDTO = corpBusinessInfoOptional.get();
        String bindQueryUrl = businessInfoDTO.getBindQueryUrl();
        try (HttpResponse httpResponse = HttpRequest.post(bindQueryUrl)
                .timeout(hideProperties.getHttpTimeout())
                .body(JSON.toJSONString(bindInfoQuery))
                .execute()) {
            String body = httpResponse.body();
            log.info("X模式外部查询绑定接口: {}, 返回值: {}", bindQueryUrl, body);
            if (httpResponse.isOk()) {
                BindInfoVO bindInfoVO = JSON.parseObject(body, BindInfoVO.class);
                return Optional.of(bindInfoVO);
            }
        } catch (Exception e) {
            log.error("X模式外部接口: {}, 调用失败: {}", bindQueryUrl, e);
        }

        return Optional.empty();
    }

    @Override
    public String getBusinessType() {
        return BUSINESS_TYPE;
    }

}
