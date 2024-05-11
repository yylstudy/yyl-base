package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.page.PageResult;
import com.linkcircle.basecom.page.PageUtil;
import com.linkcircle.system.common.CommonConstant;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.entity.SysUser;
import com.linkcircle.system.entity.SysUserRole;
import com.linkcircle.system.mapper.SysUserMapper;
import com.linkcircle.system.mapper.SysUserRoleMapper;
import com.linkcircle.system.mapstruct.SysUserMapStruct;
import com.linkcircle.system.service.SysUserRoleService;
import com.linkcircle.system.service.SysUserService;
import com.linkcircle.system.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    @Autowired
    private SysUserMapStruct sysUserMapStruct;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysUserRoleMapper sysUserRoleMapper;
    @Resource
    private SysUserRoleService sysUserRoleService;

    /**
     * 查询用户列表
     */
    @Override
    public Result<PageResult<SysUserDTO>> query(SysUserQueryReqDTO sysUserQueryReqDto) {
        sysUserQueryReqDto.setDeletedFlag(false);
        Page pageParam = PageUtil.convert2PageQuery(sysUserQueryReqDto);
        List<Long> departIdList = new ArrayList<>();
//        if (sysUserQueryReqDto.getDepartId() != null) {
//            departIdList.addAll(sysDepartService.selfAndChildrenIdList(sysUserQueryReqDto.getDepartId()));
//        }
        if(sysUserQueryReqDto.getDepartId()!=null){
            departIdList.add(sysUserQueryReqDto.getDepartId());
        }
        List<SysUserDTO> sysUserDTOS = sysUserMapper.querySysUser(pageParam, sysUserQueryReqDto, departIdList);
        if (CollectionUtils.isEmpty(sysUserDTOS)) {
            PageResult<SysUserDTO> pageResult = PageUtil.convert2PageResult(pageParam, sysUserDTOS);
            return Result.ok(pageResult);
        }
        List<Long> idList = sysUserDTOS.stream().map(SysUserDTO::getId).collect(Collectors.toList());
        // 查询用户角色
        List<SysUserRoleDTO> userRoleList = sysUserRoleMapper.selectRoleByUserIdList(idList);
        Map<Long, List<SysUserRoleDTO>> userRoleIdListMap = userRoleList.stream().collect(Collectors.groupingBy(SysUserRoleDTO::getUserId,
                Collectors.mapping(s->s, Collectors.toList())));
        for(SysUserDTO sysUser: sysUserDTOS){
            List<SysUserRoleDTO> sysUserRoleDTOS = userRoleIdListMap.get(sysUser.getId());
            if(sysUserRoleDTOS ==null){
                continue;
            }
            List<Long> roleIdList = new ArrayList<>();
            List<String> roleNameList = new ArrayList<>();
            for(SysUserRoleDTO sysUserRoleDto: sysUserRoleDTOS){
                roleIdList.add(sysUserRoleDto.getRoleId());
                roleNameList.add(sysUserRoleDto.getRoleName());
            }
            sysUser.setRoleNameList(roleNameList);
            sysUser.setRoleIdList(roleIdList);
//            sysDepartService.getDepartmentPathMap().get(sysUser.getDepartId());
        }
        PageResult<SysUserDTO> pageResult = PageUtil.convert2PageResult(pageParam, sysUserDTOS);
        return Result.ok(pageResult);
    }

    @Override
    public Result<PageResult<SysUserDTO>> queryNotInRoleUserByRoleId(SysRoleUserQueryReqDTO dto) {
        Page pageParam = PageUtil.convert2PageQuery(dto);
        List<SysUserDTO> sysUserDTOS = sysUserMapper.queryNotInRoleUserByRoleId(pageParam, dto);
        PageResult<SysUserDTO> pageResult = PageUtil.convert2PageResult(pageParam, sysUserDTOS);
        return Result.ok(pageResult);
    }

    /**
     * 新增用户
     *
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> add(SysUserAddDTO sysUserAddDto) {
        // 校验登录名是否重复
        SysUser sysUser = getByUsername(sysUserAddDto.getUsername());
        if (sysUser != null) {
            return Result.error("登录名重复");
        }
        // 校验电话是否存在
        sysUser = getByPhone(sysUserAddDto.getPhone());
        if (sysUser != null) {
            return Result.error("手机号已存在");
        }
        sysUser = getByEmail(sysUserAddDto.getEmail());
        if (sysUser != null) {
            return Result.error("邮箱已存在");
        }
        SysUser addSysUser = sysUserMapStruct.convert(sysUserAddDto);
        String password = PasswordUtil.getEncryptPwd(sysUserAddDto.getPassword());
        addSysUser.setPassword(password);
        // 保存用户
        addSysUser.setDeletedFlag(Boolean.FALSE);
        sysUserMapper.insert(addSysUser);
        if (!CollectionUtils.isEmpty(sysUserAddDto.getRoleIdList())) {
            List<SysUserRole> sysUserRoles = sysUserAddDto.getRoleIdList().stream().map(e -> new SysUserRole(e,
                    addSysUser.getId())).collect(Collectors.toList());
            sysUserRoleService.saveBatch(sysUserRoles);
        }
        return Result.ok();
    }
    /**
     * 更新
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> edit(SysUserUpdateDTO sysUserUpdateDto) {
        Long id = sysUserUpdateDto.getId();
        SysUser existSysUser = getByPhone(sysUserUpdateDto.getPhone());
        if (null != existSysUser && !Objects.equals(existSysUser.getId(), id)) {
            return Result.error("手机号已存在");
        }
        existSysUser = getByEmail(sysUserUpdateDto.getEmail());
        if (existSysUser != null&& !Objects.equals(existSysUser.getId(), id)) {
            return Result.error("邮箱已存在");
        }
        SysUser updateSysUser = sysUserMapStruct.convert(sysUserUpdateDto);
        updateById(updateSysUser);
        List<Long> dbRoleIds = sysUserRoleService.selectRoleIdsByUserId(id);
        List<Long> roleIdList = sysUserUpdateDto.getRoleIdList();
        //获取需要删除的菜单
        List<Long> deleteRoleIdList = dbRoleIds.stream().filter(roleId->!roleIdList.contains(roleId))
                .collect(Collectors.toList());
        //获取需要添加的菜单
        List<SysUserRole> addSysUserRoleList = roleIdList.stream().filter(roleId->!dbRoleIds.contains(roleId))
                .map(roleId->{
                    SysUserRole sysUserRole = new SysUserRole(roleId,id);
                    return sysUserRole;
                }).collect(Collectors.toList());
        if(!addSysUserRoleList.isEmpty()){
            sysUserRoleService.saveBatch(addSysUserRoleList);
        }
        if(!deleteRoleIdList.isEmpty()){
            sysUserRoleService.deleteByRoleIdsAndUserId(id,deleteRoleIdList);
        }
        return Result.ok();
    }

    /**
     * 更新禁用/启用状态
     */
    @Override
    public Result<String> updateDisableFlag(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if (sysUser == null) {
            return Result.error("用户不存在");
        }
        sysUser.setDisabledFlag(!sysUser.getDisabledFlag());
        this.updateById(sysUser);
        return Result.ok();
    }
    /**
     * 批量删除用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchUpdateDeleteFlag(List<Long> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Result.ok();
        }
        List<SysUser> list = this.listByIds(idList);
        for (SysUser sysUser : list) {
            sysUser.setDeletedFlag(true);
        }
        updateBatchById(list);
        return Result.ok();
    }
    /**
     * 批量更新部门
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchUpdateDepart(SysUserBatchUpdateDepartDTO batchUpdateDepartmentForm) {
        List<Long> sysUserIdList = batchUpdateDepartmentForm.getUserIdList();
        // 更新
        List<SysUser> updateList = sysUserIdList.stream().map(userId -> {
            SysUser updateSysUser = new SysUser();
            updateSysUser.setId(userId);
            updateSysUser.setDepartId(batchUpdateDepartmentForm.getDepartId());
            return updateSysUser;
        }).collect(Collectors.toList());
        updateBatchById(updateList);
        return Result.ok();
    }
    /**
     * 更新密码
     *
     */
    @Override
    public Result<String> updatePassword(SysUserUpdatePasswordDTO updatePasswordDto) {
        Long userId = updatePasswordDto.getUserId();
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            return Result.error("用户不存在");
        }
        // 校验原始密码
        String rawPassword =  PasswordUtil.getEncryptPwd(updatePasswordDto.getOldPassword());
        if (!Objects.equals(rawPassword, sysUser.getPassword())) {
            return Result.error("原密码有误，请重新输入");
        }
        // 新旧密码相同
        String newPassword = updatePasswordDto.getNewPassword();
        if (Objects.equals(updatePasswordDto.getOldPassword(), newPassword)) {
            return Result.error("新密码与原始密码相同，请重新输入");
        }
        // 更新密码
        sysUser.setPassword(PasswordUtil.getEncryptPwd(newPassword));
        updateById(sysUser);
        return Result.ok();
    }

    /**
     * 重置密码
     * @param id
     * @return
     */
    @Override
    public Result<String> resetPassword(Long id) {
        SysUser sysUser = this.getById(id);
        // 更新密码
        sysUser.setPassword(PasswordUtil.getEncryptPwd(CommonConstant.DEFAULT_PASSWORD));
        updateById(sysUser);
        return Result.ok(CommonConstant.DEFAULT_PASSWORD);
    }

    /**
     * 查询全部用户
     *
     */
    @Override
    public Result<List<SysUserDTO>> queryAllUser(Boolean disabledFlag) {
        List<SysUserDTO> list = sysUserMapper.selectUserByDisabledAndDeleted(disabledFlag, Boolean.FALSE);
        return Result.ok(list);
    }
    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUser::getUsername,username);
        return this.getOne(wrapper);
    }
    @Override
    public SysUser getByPhone(String phone) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUser::getPhone,phone);
        return this.getOne(wrapper);
    }
    @Override
    public SysUser getByEmail(String email) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUser::getEmail,email);
        return this.getOne(wrapper);
    }


}
