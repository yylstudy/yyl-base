package com.linkcircle.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.linkcircle.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import com.linkcircle.system.entity.SysRoleMenu;

import java.util.List;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    /**
     * 根据角色ID集合查询选择的菜单权限
     */
    List<SysMenu> selectMenuListByRoleIdList(@Param("roleIdList") List<Long> roleIdList);

    List<SysMenu> getSysMenuListByCorpAdminRole(@Param("corpId") String corpId);

    long deleteByCorpIdAndMenuIdList(@Param("corpId") String corpId,@Param("menuIdList")List<Long> menuIdList);
}
