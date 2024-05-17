package com.cqt.recycle.web.numpool.manager;

import com.cqt.model.numpool.dto.NumberPoolQueryDTO;
import com.cqt.model.numpool.entity.PrivateNumberInfo;
import com.cqt.model.numpool.vo.NumberPoolVO;
import com.cqt.recycle.web.numpool.mapper.PrivateCorpNumberPoolMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PrivateCorpNumberPoolService
 *
 * @author Xienx
 * @date 2023年04月04日 13:55
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivateCorpNumberPoolService {

    private final PrivateCorpNumberPoolMapper privateCorpNumberPoolMapper;

    /**
     * 查询号码池号码信息
     *
     * @param vccId    企业vccId
     * @param queryDTO 查询参数
     * @return List
     */
    public List<NumberPoolVO> queryPoolNums(String vccId, NumberPoolQueryDTO queryDTO) {
        log.info("[{}] 请求号码池查询, 参数: {}", vccId, queryDTO);

        // 查询出各区号对应的号码数量
        List<NumberPoolVO> records = privateCorpNumberPoolMapper.queryNumberAreaTotal(vccId, queryDTO);
        if (!records.isEmpty()) {
            List<PrivateNumberInfo> numberInfos = privateCorpNumberPoolMapper.queryNumberPool(vccId, queryDTO);
            Map<String, List<String>> areaNumGroup = numberInfos.stream()
                    .collect(Collectors.groupingBy(PrivateNumberInfo::getAreaCode,
                            Collectors.mapping(PrivateNumberInfo::getNumber, Collectors.toList())));

            for (NumberPoolVO record : records) {
                List<String> numList = areaNumGroup.getOrDefault(record.getAreaCode(), Collections.emptyList());
                record.setNumList(numList);
            }
        }
        return records;
    }
}
