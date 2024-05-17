package com.cqt.forward.filter.matchers;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.cqt.common.enums.ErrorCodeEnum;
import com.cqt.forward.cache.CorpBusinessConfigCache;
import com.cqt.forward.filter.BindRequestContext;
import com.cqt.forward.util.GatewayUtil;
import com.cqt.model.common.Result;
import com.cqt.model.common.properties.ForwardProperties;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author linshiqiang
 * date 2022-12-21 10:09:00
 * 接口鉴权匹配
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AuthSignMatcher implements ElementMatcher<BindRequestContext> {

    private final ForwardProperties forwardProperties;

    private final GatewayUtil gatewayUtil;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    @Override
    public boolean matches(BindRequestContext context) {
        ServerWebExchange exchange = context.getExchange();
        TreeMap<String, Object> requestObject = objectMapper.readValue(context.getRequestBody(),
                new TypeReference<TreeMap<String, Object>>() {
                });
        context.setRequestObject(requestObject);
        // 鉴权
        Result authResult = auth(context);
        if (authResult.getCode() != 0) {
            Mono<Void> errorResult = gatewayUtil.responseData(exchange, authResult);
            context.setResult(errorResult);
            return false;
        }

        return true;
    }

    /**
     * 鉴权
     */
    private Result auth(BindRequestContext context) {
        String vccId = context.getVccId();
        String path = context.getPath();

        List<String> ignoreAuthUrlList = forwardProperties.getIgnoreAuthUrlList();
        if (ignoreAuthUrlList.contains(path)) {
            return Result.ok();
        }

        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = CorpBusinessConfigCache.get(vccId);
        if (!businessInfoOptional.isPresent()) {
            log.error("企业id: {} 不存在", vccId);
            return Result.fail(ErrorCodeEnum.AUTH_FAIL.getCode(), "该企业id不存在!");
        }
        PrivateCorpBusinessInfoDTO businessInfoDTO = businessInfoOptional.get();
        // 判断企业是否申请业务模式 BusinessTypeEnum
        String businessTypeRegx = businessInfoDTO.getBusinessType();
        if (StrUtil.isEmpty(businessTypeRegx)) {
            return Result.fail(ErrorCodeEnum.AUTH_FAIL.getCode(), "该企业未配置业务模式!");
        }
        String businessType = context.getBusinessType();
        if (StrUtil.isNotEmpty(businessType)) {
            boolean match = ReUtil.isMatch(businessTypeRegx, businessType.toUpperCase());
            if (!match) {
                return Result.fail(ErrorCodeEnum.AUTH_FAIL.getCode(), "该企业未申请此业务模式!");
            }
        }

        // 配置了有效期 判断是否在有效期内
        Date expireStartTime = businessInfoDTO.getExpireStartTime();
        Date expireEndTime = businessInfoDTO.getExpireEndTime();
        if (ObjectUtil.isNotEmpty(expireStartTime) && ObjectUtil.isNotEmpty(expireEndTime)) {
            boolean in = DateUtil.isIn(DateUtil.date(), expireStartTime, expireEndTime);
            if (!in) {
                return Result.fail(ErrorCodeEnum.AUTH_FAIL.getCode(), "该企业已暂停服务!");
            }
        }

        // 是否需要鉴权
        if (ObjectUtil.isNotEmpty(businessInfoDTO.getAuthFlag())) {
            if (0 == businessInfoDTO.getAuthFlag()) {
                // 设置了不鉴权
                return Result.ok();
            }
        }
        String secretKey = businessInfoDTO.getSecretKey();
        return gatewayUtil.checkSign(vccId, secretKey, context.getRequestObject());
    }
}
