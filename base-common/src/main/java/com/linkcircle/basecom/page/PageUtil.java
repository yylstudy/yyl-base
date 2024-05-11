package com.linkcircle.basecom.page;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlInjectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.linkcircle.basecom.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Slf4j
public class PageUtil {

    /**
     * 转换为查询参数
     */
    public static Page<?> convert2PageQuery(PageParam pageParam) {
        Page<?> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
        List<PageParam.SortItem> sortItemList = pageParam.getSortItemList();
        if (CollectionUtils.isEmpty(sortItemList)) {
            return page;
        }
        // 设置排序字段并检测是否含有sql注入
        List<OrderItem> orderItemList = new ArrayList<>();
        for (PageParam.SortItem sortItem : sortItemList) {
            if (StringUtils.isEmpty(sortItem.getColumn())) {
                continue;
            }
            if (SqlInjectionUtils.check(sortItem.getColumn())) {
                log.error("《存在SQL注入：》 : {}", sortItem.getColumn());
                throw new BusinessException("存在SQL注入风险，请联系技术工作人员！");
            }
            orderItemList.add(new OrderItem(sortItem.getColumn(), sortItem.getIsAsc()));
        }
        page.setOrders(orderItemList);
        return page;
    }

    /**
     * 转换为 PageResult 对象
     */
    public static <E> PageResult<E> convert2PageResult(Page<?> page, List<E> sourceList) {
        PageResult<E> pageResult = new PageResult<>();
        pageResult.setPageNum(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setTotal(page.getTotal());
        pageResult.setPages(page.getPages());
        pageResult.setList(sourceList);
        pageResult.setEmptyFlag(sourceList.isEmpty());
        return pageResult;
    }
}
