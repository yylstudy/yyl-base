package com.cqt.unicom.service;

import com.cqt.model.unicom.dto.NumberBindingQueryDTO;
import com.cqt.model.unicom.vo.GeneralMessageVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.Api;

/**
 * @author zhengsuhao
 * @date 2022/12/5
 */
@Api(tags = "联通集团总部(江苏)能力:查询绑定关系服务")
public interface UnicomNumberBindService {


    /**
     * 查询绑定关系
     *
     * @param numberBindingQueryDTO
     * @return NumberBindingQueryVO
     */
    GeneralMessageVO getNumberBindingQuery(NumberBindingQueryDTO numberBindingQueryDTO) throws JsonProcessingException;

}
