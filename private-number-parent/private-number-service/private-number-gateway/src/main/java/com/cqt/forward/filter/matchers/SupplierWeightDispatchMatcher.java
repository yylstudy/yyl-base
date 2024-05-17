package com.cqt.forward.filter.matchers;

import com.cqt.common.constants.GatewayConstant;
import com.cqt.forward.balancer.WeightRandomBalancer;
import com.cqt.forward.cache.CorpSupplierAreaDistributionStrategyConfigCache;
import com.cqt.forward.filter.BindRequestContext;
import com.cqt.forward.util.GatewayUtil;
import com.cqt.model.bind.vo.BindTypeVO;
import com.cqt.model.corpinfo.dto.SupplierWeight;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.ElementMatcher;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Scope;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author linshiqiang
 * date 2022-12-21 10:09:00
 * 根据权重分配供应商
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SupplierWeightDispatchMatcher implements ElementMatcher<BindRequestContext> {

    private final GatewayUtil gatewayUtil;

    @Override
    public boolean matches(BindRequestContext context) {
        GatewayFilterChain chain = context.getChain();
        ServerWebExchange exchange = context.getExchange();
        String path = context.getPath();
        String vccId = context.getVccId();

        // 查询绑定接口
        if (gatewayUtil.isBindQueryApi(path)) {
            context.setResult(chain.filter(exchange));
            return false;
        }
        context.setSupplierId(GatewayConstant.LOCAL);
        TreeMap<String, Object> requestObject = context.getRequestObject();
        Optional<BindTypeVO> bindTypeVoOptional = gatewayUtil.getBindTypeVO(exchange, requestObject, path);
        if (!bindTypeVoOptional.isPresent()) {
            context.setResult(chain.filter(exchange));
            return false;
        }

        // 绑定关系 地市编码
        BindTypeVO bindTypeVO = bindTypeVoOptional.get();
        String areaCode = bindTypeVO.getAreaCode();
        Boolean isBinding = bindTypeVO.getIsBinding();
        context.setAreaCode(areaCode);
        String businessType = context.getBusinessType();
        Optional<List<SupplierWeight>> supplierListOptional = CorpSupplierAreaDistributionStrategyConfigCache.getSupplierList(vccId, areaCode, businessType);
        if (!supplierListOptional.isPresent()) {
            log.info("接口: {}, areaCode: {}, 未找到供应商配置, 默认本地", path, areaCode);
            context.setResult(chain.filter(exchange));
            return true;
        }
        List<SupplierWeight> supplierWeights = supplierListOptional.get();
        // 都是请求本地
        if (onlyLocal(supplierWeights)) {
            log.info("接口: {}, areaCode: {}, 只配置本地号码池: {}", path, areaCode, supplierWeights);
            context.setResult(chain.filter(exchange));
            return true;
        }

        // 只第三方供应商, 且只配置一个
        if (onlyThirdAndOnlyOneSupplier(supplierWeights)) {
            String supplierId = supplierWeights.get(0).getSupplierId();
            if (gatewayUtil.changeThirdRoute(exchange, supplierId)) {
                log.info("接口: {}, areaCode: {}, 只配置一个第三方供应商: {}", path, areaCode, supplierId);
                context.setSupplierId(supplierId);
                context.setResult(chain.filter(setHeader(exchange, supplierId)));
                return false;
            }
        }

        // 设置绑定接口binding 根据权重随机选择供应商, 是否调用第三方供应商接口 private-number-hmyc-third
        if (isBinding) {
            // 分配供应商
            Optional<SupplierWeight> supplierOptional = WeightRandomBalancer.getByRandomWeight(supplierWeights, vccId);
            // 供应商为本地号码池(local)不处理
            if (supplierOptional.isPresent()) {
                SupplierWeight supplierWeight = supplierOptional.get();
                String supplierId = supplierWeight.getSupplierId();
                log.info("绑定接口: {}, areaCode: {}, 请求分配的供应商为: {}", path, areaCode, supplierId);
                if (!GatewayConstant.LOCAL.equals(supplierId) && gatewayUtil.changeThirdRoute(exchange, supplierId)) {
                    context.setSupplierId(supplierId);
                    context.setResult(chain.filter(setHeader(exchange, supplierId)));
                    return false;
                }
            }
            return true;
        }
        // 解绑, 更新, 需要查询bindId所关联的供应商, bindId一定不能为空!
        String bindId = bindTypeVO.getBindId();
        Optional<String> supplierIdOptional = gatewayUtil.getSupplierIdByBindId(supplierWeights, vccId, bindId);
        if (supplierIdOptional.isPresent()) {
            String supplierId = supplierIdOptional.get();
            log.info("解绑/修改接口: {}, areaCode: {}, bindId: {}, 请求分配的供应商为: {}", path, areaCode, bindId, supplierId);
            if (!GatewayConstant.LOCAL.equals(supplierId) && gatewayUtil.changeThirdRoute(exchange, supplierId)) {
                log.info("path: {}, areaCode: {}, 请求第三方供应商: {}", path, areaCode, supplierId);
                context.setSupplierId(supplierId);
                context.setResult(chain.filter(setHeader(exchange, supplierId)));
                return false;
            }
        }
        return true;
    }

    public ServerWebExchange setHeader(ServerWebExchange exchange, String supplierId) {
        // 向headers中放文件，记得build
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header(GatewayConstant.SUPPLIER_ID, supplierId)
                .build();
        // 将现在的request 变成 change对象
        return exchange.mutate().request(request).build();
    }

    /**
     * 只配置了本地
     */
    private Boolean onlyLocal(List<SupplierWeight> supplierWeights) {
        long localCount = supplierWeights.stream()
                .filter(item -> GatewayConstant.LOCAL.equals(item.getSupplierId()))
                .count();
        return localCount == supplierWeights.size();
    }

    /**
     * 只第三方供应商, 且只配置一个
     */
    private Boolean onlyThirdAndOnlyOneSupplier(List<SupplierWeight> supplierWeights) {
        long localCount = supplierWeights.stream()
                .filter(item -> !GatewayConstant.LOCAL.equals(item.getSupplierId()))
                .count();
        return localCount == supplierWeights.size() && localCount == 1;
    }
}
