import { postRequest, getRequest } from '/src/lib/axios';

export const corpApi = {
  queryList: (param) => {
    return postRequest('/system/corp/query', param);
  },
  add: (param) => {
    return postRequest('/system/corp/add', param);
  },
  update: (param) => {
    return postRequest('/system/corp/edit', param);
  },
  batchDelete: (idList) => {
    return postRequest('/system/corp/batchDelete/', idList);
  },
};
