import { postRequest, getRequest } from '/src/lib/axios';

export const dictApi = {
  // 分页查询数据字典KEY
  dictQuery: (param) => {
    return postRequest('/system/dict/query', param);
  },
  // 查询全部字典key
  dictQueryAll: () => {
    return getRequest('/system/dict/queryAll');
  },
  /**
   * 分页查询数据字典value  
   */
  itemQuery: (param) => {
    return postRequest('/system/dict/item/query', param);
  },
  // 数据字典KEY-添加
  add: (param) => {
    return postRequest('/system/dict/add', param);
  },
  // 分页查询数据字典value
  itemAdd: (param) => {
    return postRequest('/system/dict/itemAdd', param);
  },
  // 数据字典key-更新
  edit: (param) => {
    return postRequest('/system/dict/edit', param);
  },
  // 数据字典Value-更新
  itemEdit: (param) => {
    return postRequest('/system/dict/itemEdit', param);
  },
  // 数据字典key-删除
  delete: (idList) => {
    return postRequest('/system/dict/delete', idList);
  },
  // 数据字典Value-删除
  itemDelete: (dictId,itemIdList) => {
    return postRequest('/system/dict/itemDelete', itemIdList);
  },
  // 缓存刷新
  cacheRefresh: () => {
    return getRequest('/system/dict/cache/refresh');
  },
  // 数据字典-值列表
  itemList: (dictCode) => {
    return getRequest(`/system/dict/item/list/${dictCode}`);
  },
  // 数据字典-值列表
  getItemByDictCode: (dictCode) => {
    return getRequest(`/system/dict/getItemByDictCode?dictCode=${dictCode}`);
  },

};
