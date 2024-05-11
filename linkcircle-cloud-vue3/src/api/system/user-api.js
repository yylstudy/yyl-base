import { getRequest, postRequest } from '/src/lib/axios';

export const userApi = {
  /**
   * 查询所有员工 
   */
  queryAll: () => {
    return getRequest('/system/user/queryAll');
  },
  /**
   * 员工管理查询
   */
  queryUser: (params) => {
    return postRequest('/system/user/query', params);
  },
  /**
   * 查询不在角色下的用户
   */
  queryNotInRoleUserByRoleId: (params) => {
    return postRequest('/system/user/queryNotInRoleUserByRoleId', params);
  },

  /**
   * 添加员工
   */
  add: (params) => {
    return postRequest('/system/user/add', params);
  },
  /**
   * 更新员工信息
   */
  edit: (params) => {
    return postRequest('/system/user/update', params);
  },
  /**
   * 删除员工
   */
  deleteEmployee: (employeeId) => {
    return getRequest(`/system/user/delete/${employeeId}`);
  },
  /**
   * 批量删除员工
   */
  batchDeleteEmployee: (userIdList) => {
    return postRequest('/system/user/update/batch/delete', userIdList);
  },
  /**
   * 批量调整员工部门
   */
  batchUpdateDepartmentEmployee: (updateParam) => {
    return postRequest('/system/user/update/batch/depart', updateParam);
  },
  /**
   * 重置员工密码
   */
  resetPassword: (id) => {
    return postRequest(`/system/user/resetPassword/${id}`);
  },
  /**
   * 修改面面
   */
  updatePwd: (param) => {
    return postRequest('/system/user/update/password', param);
  },
  /**
   * 更新员工禁用状态
   */
  updateDisabled: (id) => {
    return getRequest(`/system/user/update/disabled/${id}`);
  },

};
