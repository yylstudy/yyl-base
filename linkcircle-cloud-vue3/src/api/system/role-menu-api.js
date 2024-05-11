import { getRequest, postRequest } from '/src/lib/axios';
export const roleMenuApi = {
  /**
   * @description: 获取角色关联菜单权限
   */
  getRoleSelectedMenu: (roleId) => {
    return getRequest(`/system/role/menu/getRoleSelectedMenu/${roleId}`);
  },
  /**
   * @description: 更新角色权限
   */
  updateRoleMenu: (data) => {
    return postRequest('/system/role/menu/updateRoleMenu', data);
  },
};
