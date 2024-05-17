package com.cqt.hmyc.web.bind.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author dingsh
 * @since 2022-07-06
 */
@Data
@Component
@RequiredArgsConstructor
public class BindStrategyManager {

    public final List<BindStrategy> bindStrategyList;

    public Optional<BindStrategy> getBindStrategy(String supplierId) {
        return bindStrategyList.stream()
                .filter(strategy -> strategy.match(supplierId))
                .findFirst();
    }

}
