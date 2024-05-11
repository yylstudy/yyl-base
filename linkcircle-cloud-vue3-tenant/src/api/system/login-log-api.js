import { postRequest, getRequest } from '/src/lib/axios';

export const loginLogApi = {
  // 分页查询
  queryList: (param) => {
    return postRequest('/system/loginLog/page/query', param);
  },
};
