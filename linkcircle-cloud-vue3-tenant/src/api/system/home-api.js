import { getRequest } from '/src/lib/axios';

export const homeApi = {
  /**
   * @description: 首页-待办信息
   */
  homeWaitHandle: () => {
    return getRequest('home/wait/handle');
  },
};
