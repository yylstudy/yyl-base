package com.cqt.forward.handler;

import com.cqt.common.util.CopyOnWriteMap;
import com.cqt.forward.cache.CorpSupplierAreaDistributionStrategyConfigCache;
import com.cqt.model.common.ResultVO;
import com.cqt.model.corpinfo.dto.SupplierWeight;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author linshiqiang
 * date:  2023-02-02 14:53
 */
@RestController
public class LocalCacheController {

    @GetMapping("getSupplierWeight")
    public ResultVO<CopyOnWriteMap<String, List<SupplierWeight>>> getSupplierWeight() {

        return ResultVO.ok(CorpSupplierAreaDistributionStrategyConfigCache.all());
    }
}
