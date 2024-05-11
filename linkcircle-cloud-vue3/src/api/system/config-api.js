import { postRequest, getRequest } from '/src/lib/axios';

export const configApi = {
  queryList: (param) => {
    return postRequest('/system/config/query', param);
  },
  addConfig: (param) => {
    return postRequest('/system/config/add', param);
  },
  updateConfig: (param) => {
    return postRequest('/system/config/edit', param);
  },
  batchDelete: (idList) => {
    return postRequest('/system/config/batchDelete/', idList);
  },
};
