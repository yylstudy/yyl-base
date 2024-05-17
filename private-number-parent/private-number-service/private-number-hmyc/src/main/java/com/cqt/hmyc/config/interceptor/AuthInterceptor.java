package com.cqt.hmyc.config.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.cqt.common.constants.GatewayConstant;
import com.cqt.hmyc.config.exception.AuthException;
import com.cqt.hmyc.web.corpinfo.service.CorpBusinessService;
import com.cqt.model.common.properties.HideProperties;
import com.cqt.model.corpinfo.dto.PrivateCorpBusinessInfoDTO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author linshiqiang
 * @since 2022/2/22 16:25
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final HideProperties hideProperties;

    private final CorpBusinessService corpBusinessService;

    @SuppressWarnings("all")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String vccId = getVccId(request);

        String json = IoUtil.readUtf8(request.getInputStream());

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.hasMethodAnnotation(ApiOperation.class)) {
                ApiOperation apiOperation = handlerMethod.getMethodAnnotation(ApiOperation.class);
                log.info("{}: vccId: {}, params: {}", apiOperation.value(), vccId, json);
            }
        }

        // hmyc 隐藏服务, AOP 是否开启鉴权, 默认true
        if (!hideProperties.getSwitchs().getAuthFlag()) {
            return true;
        }

        if (StrUtil.isEmpty(vccId)) {
            throw new AuthException("appkey 为空!");
        }

        // 企业信息表改造
        Optional<PrivateCorpBusinessInfoDTO> businessInfoOptional = corpBusinessService.getCorpBusinessInfo(vccId);
        if (!businessInfoOptional.isPresent()) {
            throw new AuthException("appkey 不存在!");
        }
        PrivateCorpBusinessInfoDTO businessInfoDTO = businessInfoOptional.get();

        // 判断企业是否申请业务模式 BusinessTypeEnum
        String businessType = businessInfoDTO.getBusinessType();
        if (StrUtil.isEmpty(businessType)) {
            throw new AuthException("该企业未配置业务模式!");
        }

        Optional<String> businessTypeOptional = getBusinessType(request);
        if (businessTypeOptional.isPresent()) {
            boolean match = ReUtil.isMatch(businessType, businessTypeOptional.get().toUpperCase());
            if (!match) {
                throw new AuthException("该企业未申请此业务模式!");
            }
        }

        // 配置了有效期 判断是否在有效期内
        if (ObjectUtil.isNotEmpty(businessInfoDTO.getExpireStartTime()) && ObjectUtil.isNotEmpty(businessInfoDTO.getExpireEndTime())) {
            boolean in = DateUtil.isIn(DateUtil.date(), businessInfoDTO.getExpireStartTime(), businessInfoDTO.getExpireEndTime());
            if (!in) {
                throw new AuthException("该企业已暂停服务!");
            }
        }

        // 是否需要鉴权
        if (ObjectUtil.isNotEmpty(businessInfoDTO.getAuthFlag())) {
            if (0 == businessInfoDTO.getAuthFlag()) {
                return true;
            }
        }

        String secretKey = businessInfoDTO.getSecretKey();

        TreeMap paramsMap = JSON.parseObject(json, TreeMap.class);

        return checkSign(vccId, secretKey, paramsMap);
    }

    /**
     * 获取接口业务模式
     *
     * @param request http请求
     * @return 业务模式
     */
    private Optional<String> getBusinessType(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.contains(GatewayConstant.BIND_INFO_API)) {
            return Optional.empty();
        }
        try {
            List<String> list = StrUtil.split(uri, "/");
            if (list.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(list.get(5));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 获取vccId
     *
     * @param request http请求
     * @return vccId
     */
    @SuppressWarnings("all")
    private String getVccId(HttpServletRequest request) {
        // org.springframework.web.servlet.HandlerMapping.uriTemplateVariables
        String templateVariablesAttribute = HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE;
        LinkedHashMap<String, String> pathVariableMap = null;
        try {
            pathVariableMap = (LinkedHashMap<String, String>) request.getAttribute(templateVariablesAttribute);
            if (CollUtil.isEmpty(pathVariableMap)) {
                return "";
            }
        } catch (Exception e) {
            log.error("getVccId失败", e);
            return "";
        }
        return Convert.toStr(pathVariableMap.get("vccId"), "");
    }

    /**
     * 校验签名
     *
     * @param vccId     企业id
     * @param secretKey 秘钥
     * @param paramsMap 参数
     * @return
     */
    @SuppressWarnings("all")
    public boolean checkSign(String vccId, String secretKey, TreeMap paramsMap) {
        if (ObjectUtil.isEmpty(paramsMap.get("ts"))) {
            throw new AuthException("ts 为空!");
        }
        // 前后5分钟
        long ts = Convert.toLong(paramsMap.get("ts"));
        DateTime dateTime = DateUtil.date(ts);
        long between = DateUtil.between(dateTime, DateUtil.date(), DateUnit.MINUTE, true);

        if (between > 5) {
            throw new AuthException("sign 已过期!");
        }

        // 验签
        String userSign = Convert.toStr(paramsMap.get("sign"));
        if (StrUtil.isEmpty(userSign)) {
            throw new AuthException("sign 不存在!");
        }
        String createSign = createSign(paramsMap, secretKey);
        boolean equals = userSign.equals(createSign);
        if (!equals) {
            throw new AuthException("sign 验证不通过!");
        }
        return true;
    }

    /**
     * 生成签名
     *
     * @param params    参数
     * @param secretKey 秘钥
     * @return 签名
     */
    public static String createSign(Map<String, Object> params, String secretKey) {
        params.remove("appkey");
        params.remove("sign");
        params.remove("vcc_id");
        List<String> paramList = new ArrayList<>();
        params.forEach((key, value) -> {
            if (ObjectUtil.isNotEmpty(value)) {
                paramList.add(key + "=" + StrUtil.removeAll(value.toString(), CharUtil.CR, CharUtil.LF, CharUtil.SPACE, CharUtil.TAB));
            }
        });
        paramList.add("secret_key=" + secretKey);
        return SecureUtil.md5(String.join("&", paramList)).toUpperCase();
    }

}
