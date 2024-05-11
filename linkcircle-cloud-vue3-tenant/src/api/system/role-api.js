import { getRequest, postRequest } from '/src/lib/axios';

export const roleApi = {
  /**
   * @description: 获取所有角色
   */
  queryAll: () => {
    return getRequest('/system/role/getAll');
  },
  /**
   * 获取企业下的所有角色
   * @returns {Promise<AxiosResponse<any>>}
   */
  getCorpRole: () => {
    return getRequest('/system/role/getCorpRole');
  },

  /**
   * @description:添加角色
   */
  add: (data) => {
    return postRequest('/system/role/add', data);
  },
  /**
   * @description:更新角色
   */
  edit: (data) => {
    return postRequest('/system/role/update', data);
  },
  /**
   * @description: 删除角色
   */
  delete: (roleId) => {
    return postRequest(`/system/role/delete/${roleId}`);
  },
  /**
   * @description: 获取角色成员-员工列表
   */
  queryUserByRole: (params) => {
    return postRequest('/system/role/user/queryUserByRole', params);
  },
  /**
   * @description: 从角色成员列表中移除员工
   */
  removeSysUserRole: (userId, roleId) => {
    return postRequest('/system/role/user/removeSysUserRole?userId=' + userId + '&roleId=' + roleId);
  },
  /**
   * @description: 从角色成员列表中批量移除员工
   */
  batchDeleteSysUserRole: (data) => {
    return postRequest('/system/role/user/batchDeleteSysUserRole', data);
  },
  /**
   * @description: 角色成员列表中批量添加员工
   */
  batchAddSysUserRole: (data) => {
    return postRequest('/system/role/user/batchAddSysUserRole', data);
  },
};
