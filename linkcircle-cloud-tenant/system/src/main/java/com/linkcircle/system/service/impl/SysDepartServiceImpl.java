package com.linkcircle.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.linkcircle.basecom.common.Result;
import com.linkcircle.basecom.exception.BusinessException;
import com.linkcircle.system.common.CommonConstant;
import com.linkcircle.system.common.SysCacheConstant;
import com.linkcircle.system.config.SystemLoginUserInfoHolder;
import com.linkcircle.system.dto.*;
import com.linkcircle.system.dto.SysDepartDTO;
import com.linkcircle.system.dto.SysDepartTreeDTO;
import com.linkcircle.system.entity.SysDepart;
import com.linkcircle.system.entity.SysUser;
import com.linkcircle.system.mapper.SysDepartMapper;
import com.linkcircle.system.mapper.SysUserMapper;
import com.linkcircle.system.mapstruct.SysDepartMapStruct;
import com.linkcircle.system.service.SysDepartService;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author: yang.yonglian
 * @CreateDate: 2024/3/1 20:18
 * @Version: 1.0
 */
@Service
public class SysDepartServiceImpl extends ServiceImpl<SysDepartMapper, SysDepart> implements SysDepartService {

    @Resource
    private SysDepartMapper sysDepartMapper;

    @Resource
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysDepartMapStruct sysDepartMapStruct;

    /**
     * 部门列表
     */
    @Cacheable(value=SysCacheConstant.DEPART_LIST_CACHE,key = "#corpId")
    @Override
    public List<SysDepartDTO> listAll(String corpId) {
        return sysDepartMapper.listAll(corpId);
    }
    /**
     * 获取部门树形结构
     */
    @Cacheable(value=SysCacheConstant.DEPART_TREE_CACHE,key = "#corpId")
    @Override
    public List<SysDepartTreeDTO> departTree(String corpId) {
        List<SysDepartDTO> list = getAllDepartCache(corpId);
        return this.buildTree(list);
    }

    /**
     * 新增添加部门
     *
     */
    @CacheEvict(value = {SysCacheConstant.DEPART_LIST_CACHE, SysCacheConstant.DEPART_MAP_CACHE, SysCacheConstant.DEPART_SELF_CHILDREN_CACHE,
            SysCacheConstant.DEPART_TREE_CACHE, SysCacheConstant.DEPART_PATH_CACHE},key = "#corpId",allEntries = true)
    @Override
    public Result<String> add(String corpId,SysDepartAddDTO dto) {
        SysDepart sysDepart = sysDepartMapStruct.convert(dto);
        sysDepart.setCorpId(corpId);
        SysDepart existsSysDepart =getByDepartName(corpId,dto.getName());
        if(existsSysDepart!=null){
            return Result.error("部门已存在");
        }
        save(sysDepart);
        return Result.ok();
    }

    /**
     * 更新部门信息
     *
     */
    @CacheEvict(value = {SysCacheConstant.DEPART_LIST_CACHE, SysCacheConstant.DEPART_MAP_CACHE, SysCacheConstant.DEPART_SELF_CHILDREN_CACHE,
            SysCacheConstant.DEPART_TREE_CACHE, SysCacheConstant.DEPART_PATH_CACHE,},key = "#dto.corpId", allEntries = true)
    @Override
    public Result<String> edit(SysDepartUpdateDTO dto) {
        if (dto.getParentId() == null) {
            return Result.error("父级部门id不能为空");
        }
        SysDepart existsSysDepart =getByDepartName(dto.getCorpId(),dto.getName());
        if(existsSysDepart!=null && !dto.getId().equals(existsSysDepart.getId())){
            return Result.error("部门已存在");
        }
        SysDepart sysDepart = sysDepartMapStruct.convert(dto);
        updateById(sysDepart);
        return Result.ok();
    }


    /**
     * 根据id删除部门
     * 1、需要判断当前部门是否有子部门,有子部门则不允许删除
     * 2、需要判断当前部门是否有用户，有用户则不能删除
     */
    @CacheEvict(value = {SysCacheConstant.DEPART_LIST_CACHE, SysCacheConstant.DEPART_MAP_CACHE, SysCacheConstant.DEPART_SELF_CHILDREN_CACHE,
            SysCacheConstant.DEPART_TREE_CACHE, SysCacheConstant.DEPART_PATH_CACHE,},key = "#corpId", allEntries = true)
    @Override
    public void deleteDepartById(String corpId,Long id) {
        // 是否有子级部门
        List<SysDepart> subDepartmentNum = getByParentId(id);
        if (!subDepartmentNum.isEmpty()) {
            throw new BusinessException("请先删除子级部门");
        }
        // 是否有未删除用户
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysUser::getDepartId,id);
        wrapper.eq(SysUser::getDeletedFlag,false);
        long sysUserNum = sysUserMapper.selectCount(wrapper);
        if (sysUserNum > 0) {
            throw new BusinessException("请先删除部门用户");
        }
        sysDepartMapper.deleteById(id);
    }
    @Override
    public List<SysDepart> getByParentId(Long parentId) {
        LambdaQueryWrapper<SysDepart> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDepart::getParentId,parentId);
        return list(wrapper);
    }

//    /**
//     * 自身以及所有下级的部门id列表
//     *
//     */
//    @Override
//    @Cacheable(SysCacheConstant.DEPART_SELF_CHILDREN_CACHE)
//    public List<Long> selfAndChildrenIdList(Long departId) {
//        List<SysDepartDto> sysDepartDtoList = getAllDepartCache();
//        return this.selfAndChildrenIdList(departId, sysDepartDtoList);
//    }

    /**
     * 通过部门id,获取当前以及下属部门
     */
    public List<Long> selfAndChildrenIdList(Long departId, List<SysDepartDTO> voList) {
        List<Long> selfAndChildrenIdList = new ArrayList<>();
        if (CollectionUtils.isEmpty(voList)) {
            return selfAndChildrenIdList;
        }
        selfAndChildrenIdList.add(departId);
        List<SysDepartTreeDTO> children = this.getChildren(departId, voList);
        if (CollectionUtils.isEmpty(children)) {
            return selfAndChildrenIdList;
        }
        List<Long> childrenIdList = children.stream().map(SysDepartTreeDTO::getId).collect(Collectors.toList());
        selfAndChildrenIdList.addAll(childrenIdList);
        for (Long childId : childrenIdList) {
            this.selfAndChildrenRecursion(selfAndChildrenIdList, childId, voList);
        }
        return selfAndChildrenIdList;
    }

    /**
     * 构建部门树结构
     *
     */
    public List<SysDepartTreeDTO> buildTree(List<SysDepartDTO> allSysDepartDTOList) {
        if (CollectionUtils.isEmpty(allSysDepartDTOList)) {
            return new ArrayList<>();
        }
        List<SysDepartDTO> rootList = allSysDepartDTOList.stream().filter(sysDepart -> sysDepart.getParentId() == null ||
                Objects.equals(sysDepart.getParentId(), CommonConstant.TOP_DEPART_PATENT_ID)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(rootList)) {
            return new ArrayList<>();
        }
        List<SysDepartTreeDTO> treeVOList = sysDepartMapStruct.convert(rootList);
        this.recursiveBuildTree(treeVOList, allSysDepartDTOList);
        return treeVOList;
    }

    /**
     * 构建所有根节点的下级树形结构
     *
     */
    private void recursiveBuildTree(List<SysDepartTreeDTO> nodeList, List<SysDepartDTO> allSysDepartDTOList) {
        int nodeSize = nodeList.size();
        for (int i = 0; i < nodeSize; i++) {
            int preIndex = i - 1;
            int nextIndex = i + 1;
            SysDepartTreeDTO node = nodeList.get(i);
            if (preIndex > -1) {
                node.setPreId(nodeList.get(preIndex).getId());
            }
            if (nextIndex < nodeSize) {
                node.setNextId(nodeList.get(nextIndex).getId());
            }
            List<Long> selfAndAllChildrenIdList = new ArrayList<>();
            selfAndAllChildrenIdList.add(node.getId());
            node.setSelfAndAllChildrenIdList(selfAndAllChildrenIdList);
            List<SysDepartTreeDTO> children = getChildren(node.getId(), allSysDepartDTOList);
            if (!CollectionUtils.isEmpty(children)) {
                node.setChildren(children);
                this.recursiveBuildTree(children, allSysDepartDTOList);
            }
        }
    }

    /**
     * 部门的路径名称
     *
     */
    @Cacheable(value=SysCacheConstant.DEPART_PATH_CACHE,key = "#corpId")
    @Override
    public Map<Long, String> getDepartmentPathMap(String corpId) {
        List<SysDepartDTO> sysDepartDTOList = getAllDepartCache(corpId);
        Map<Long, SysDepartDTO> departmentMap = sysDepartDTOList.stream()
                .collect(Collectors.toMap(SysDepartDTO::getId, Function.identity()));
        Map<Long, String> pathNameMap = new HashMap<>();
        for (SysDepartDTO sysDepartDto : sysDepartDTOList) {
            String pathName = this.buildDepartmentPath(sysDepartDto, departmentMap);
            pathNameMap.put(sysDepartDto.getId(), pathName);
        }
        return pathNameMap;
    }

    /**
     * 获取子元素
     *
     */
    private List<SysDepartTreeDTO> getChildren(Long departId, List<SysDepartDTO> voList) {
        List<SysDepartDTO> childrenList = voList.stream().filter(e ->
                departId.equals(e.getParentId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childrenList)) {
            return new ArrayList<>();
        }
        return sysDepartMapStruct.convert(childrenList);
    }

    private SysDepart getByDepartName(String corpId,String departName){
        LambdaQueryWrapper<SysDepart> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(SysDepart::getName,departName);
        wrapper.eq(SysDepart::getCorpId,corpId);
        return getOne(wrapper);
    }

    /**
     * 递归查询
     */
    public void selfAndChildrenRecursion(List<Long> selfAndChildrenIdList, Long departmentId, List<SysDepartDTO> voList) {
        List<SysDepartTreeDTO> children = this.getChildren(departmentId, voList);
        if (CollectionUtils.isEmpty(children)) {
            return;
        }
        List<Long> childrenIdList = children.stream().map(SysDepartTreeDTO::getId).collect(Collectors.toList());
        selfAndChildrenIdList.addAll(childrenIdList);
        for (Long childId : childrenIdList) {
            this.selfAndChildrenRecursion(selfAndChildrenIdList, childId, voList);
        }
    }


    private String buildDepartmentPath(SysDepartDTO sysDepartDto, Map<Long, SysDepartDTO> departmentMap) {
        if (Objects.equals(sysDepartDto.getParentId(), CommonConstant.TOP_DEPART_PATENT_ID)) {
            return sysDepartDto.getName();
        }
        //父节点
        SysDepartDTO parentDepartment = departmentMap.get(sysDepartDto.getParentId());
        if (parentDepartment == null) {
            return sysDepartDto.getName();
        }
        String pathName = buildDepartmentPath(parentDepartment, departmentMap);
        return pathName + "/" + sysDepartDto.getName();

    }

    private List<SysDepartDTO> getAllDepartCache(String corpId){
        SysDepartService sysDepartService = (SysDepartService)AopContext.currentProxy();
        List<SysDepartDTO> list = sysDepartService.listAll(corpId);
        return list;
    }


}
