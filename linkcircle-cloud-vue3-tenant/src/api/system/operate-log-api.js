import { postRequest, getRequest } from '/src/lib/axios';

export const operateLogApi = {
  // 分页查询
  queryList: (param) => {
    return postRequest('/system/operateLog/page/query', param);
  },
  // 详情
  detail: (id) => {
    return getRequest(`/system/operateLog/detail/${id}`);
  },
};
