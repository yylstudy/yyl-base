package com.cqt.cdr.cloudccsfaftersales.entity.agent;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Sort;

/**
 * 基础分页查询
 *
 * @author scott
 * @date 2022年07月08日 11:16
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BasePageQuery<T> extends BaseSelection {

    private static final long serialVersionUID = 8428370447879473694L;

    /**
     * 当前分页页码, 默认为1
     */
    private Integer pageNo = 1;

    /**
     * 当前分页显示条数, 默认10条
     */
    private Integer pageSize = 10;

    /**
     * 要排序的字段名称
     */
    private String column;

    /**
     * 排列方向 ASC, DESC
     */
    private String order;

    /**
     * 限制orderDirection的值，避免sql注入！
     *
     * @param order 排列方向
     */
    public void setOrder(String order) {
        if (Sort.Direction.ASC.name().equalsIgnoreCase(StrUtil.trim(order))) {
            this.order = Sort.Direction.ASC.name();
        } else {
            this.order = Sort.Direction.DESC.name();
        }
    }

    /**
     * 获取当前记录查询的offset
     *
     * @return long
     */
    public int getOffset() {
        return pageSize * (pageNo - 1);
    }

    public Page<T> toPage() {
        return new Page<>(this.pageNo, this.pageSize);
    }
}
