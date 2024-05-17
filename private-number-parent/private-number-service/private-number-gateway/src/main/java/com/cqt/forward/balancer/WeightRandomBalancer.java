package com.cqt.forward.balancer;

import com.alibaba.nacos.client.naming.utils.Pair;
import com.cqt.model.corpinfo.dto.SupplierWeight;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author linshiqiang
 * date:  2022-12-20 16:26
 * 根据权重随机分配
 * cory from
 * @see com.alibaba.nacos.client.naming.core.Balancer
 */
@Slf4j
public class WeightRandomBalancer {

    /**
     * 供应商
     *
     * @param weights 权重信息
     * @param vccId   企业id
     * @return 具体供应商
     */
    public static Optional<SupplierWeight> getByRandomWeight(List<SupplierWeight> weights, String vccId) {
        if (weights == null || weights.size() == 0) {
            log.debug("weights == null || weights.size() == 0");
            return Optional.empty();
        }
        List<Pair<SupplierWeight>> hostsWithWeight = new ArrayList<>();
        for (SupplierWeight target : weights) {
            hostsWithWeight.add(new Pair<>(target, target.getWeight()));
        }
        SupplierChooser<String, SupplierWeight> vipChooser = new SupplierChooser<>(vccId);
        vipChooser.refresh(hostsWithWeight);
        return Optional.ofNullable(vipChooser.randomWithWeight());
    }
}
