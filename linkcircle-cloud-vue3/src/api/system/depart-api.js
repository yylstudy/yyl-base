import { getRequest, postRequest } from '/src/lib/axios';

export const departApi = {
  /**
   * 查询部门列表 
   */
  queryAllDepart: () => {
    return getRequest('/system/depart/listAll');
  },

  /**
   * 查询部门树形列表 
   */
  queryDepartTree: () => {
    return getRequest('/system/depart/treeList');
  },
  /**
   * 添加部门
   */
  addDepart: (param) => {
    return postRequest('/system/depart/add', param);
  },
  /**
   * 更新部门信息
   */
  updateDepart: (param) => {
    return postRequest('/system/depart/update', param);
  },

  /**
   * 删除
   */
  deleteDepart: (id) => {
    return postRequest(`/system/depart/delete/${id}`);
  },
};
