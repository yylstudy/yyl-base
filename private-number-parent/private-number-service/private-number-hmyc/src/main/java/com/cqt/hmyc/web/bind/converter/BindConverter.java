package com.cqt.hmyc.web.bind.converter;

import com.cqt.model.bind.query.BindInfoApiQuery;
import com.cqt.model.bind.query.BindInfoQuery;
import com.cqt.model.bind.vo.BindInfoApiVO;
import com.cqt.model.bind.vo.BindInfoVO;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

/**
 * @author linshiqiang
 * @since 2022-11-16 10:11
 */
@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface BindConverter {

    /**
     * api 2 query
     *
     * @param bindInfoApiQuery api
     * @return query
     */
    BindInfoQuery bindInfoApiQuery2BindInfoQuery(BindInfoApiQuery bindInfoApiQuery);

    /**
     * vo 2 apiVo
     *
     * @param bindInfoVO vo
     * @return apiVo
     */
    BindInfoApiVO bindInfoVo2BindInfoApiVO(BindInfoVO bindInfoVO);

}
