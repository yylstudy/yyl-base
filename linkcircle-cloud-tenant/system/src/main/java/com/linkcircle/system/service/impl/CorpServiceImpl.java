package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.basecom.page.PageUtil;
import com.linkcircle.system.common.CommonConstant;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.entity.*;
import com.linkcircle.system.mapper.CorpMapper;
import com.linkcircle.system.mapstruct.CorpMapStruct;
import com.linkcircle.system.service.*;
import com.linkcircle.system.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Slf4j
@Service
public class CorpServiceImpl extends ServiceImpl<CorpMapper, Corp> implements CorpService {
    @Autowired
    private CorpMapStruct corpMapStruct;
    @Autowired
    private CorpMapper corpMapper;
    @Autowired
    private CorpBusinessService corpBusinessService;
    @Autowired
    private SysUserService userService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private CorpUserService corpUserService;
    @Autowired
    private SysUserRoleService sysUserRoleService;
    /**
     * 分页查询系统配置
     */
    @Override
    public Result<PageResult<CorpResDTO>> query(CorpQueryDTO queryForm) {
        Page<?> page = PageUtil.convert2PageQuery(queryForm);
        List<CorpResDTO> corpResDTOS = corpMapper.queryByPage(page, queryForm);
        for(CorpResDTO corpResDto: corpResDTOS){
            String businessStr = corpResDto.getBusinessStr();
            if(StringUtils.hasText(businessStr)){
                corpResDto.setBusinessList(Arrays.asList(businessStr.split(",")));
            }
        }
        PageResult<CorpResDTO> pageResult = PageUtil.convert2PageResult(page, corpResDTOS);
        return Result.ok(pageResult);
    }
    /**
     * 添加系统配置
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> add(CorpAddDTO dto) {
        Corp existsCorp = getById(dto.getName());
        if (existsCorp !=null) {
            return Result.error("企业已存在");
        }
        Corp corp = corpMapStruct.convert(dto);
        save(corp);
        List<BusinessMenuDTO> businessMenuDTOS = dto.getBusinessMenus();
        //增加企业业务关联关系
        List<CorpBusiness> corpBusinessList = businessMenuDTOS.stream().map(businessMenu->{
            CorpBusiness corpBusiness = new CorpBusiness();
            corpBusiness.setCorpId(corp.getId());
            corpBusiness.setBusiness(businessMenu.getBusiness());
            return corpBusiness;
        }).collect(Collectors.toList());
        //创建企业管理员角色
        SysRole sysRole = new SysRole();
        sysRole.setCorpId(corp.getId());
        sysRole.setRoleName(corp.getId()+"企业管理员");
        sysRole.setRoleCode(dto.getId()+"admin");
        sysRole.setCorpAdmin(true);
        sysRoleService.save(sysRole);
        //创建企业管理员
        SysUser sysUser = new SysUser();
        sysUser.setUsername(dto.getId()+"admin");
        sysUser.setDisabledFlag(false);
        sysUser.setDeletedFlag(false);
        sysUser.setPhone(dto.getPhone());
        sysUser.setEmail(dto.getEmail());
        sysUser.setPassword(PasswordUtil.getEncryptPwd(CommonConstant.DEFAULT_PASSWORD));
        userService.save(sysUser);
        //增加企业管理员和企业管理员角色关联关系
        SysUserRole sysUserRole = new SysUserRole(sysRole.getId(),sysUser.getId());
        sysUserRoleService.save(sysUserRole);
        //增加企业用户关联关系
        CorpUser corpUser = new CorpUser();
        corpUser.setCorpId(corp.getId());
        corpUser.setUserId(sysUser.getId());
        corpUserService.save(corpUser);
        if(!corpBusinessList.isEmpty()){
            corpBusinessService.saveBatch(corpBusinessList);
        }
        return Result.ok();
    }
    /**
     * 更新系统配置
     */
    @Override
    public Result<String> edit(CorpUpdateDTO dto) {
        Corp alreadyEntity = getById(dto.getId());
        if (alreadyEntity != null && !Objects.equals(dto.getId(), alreadyEntity.getId())) {
            return Result.error("key已存在");
        }
        Corp corp = corpMapStruct.convert(dto);
        updateById(corp);
        return Result.ok();
    }

    @Override
    public Result<String> delete(String id) {
        removeById(id);
        return Result.ok();
    }

}
