import { getRequest, postRequest } from '/src/lib/axios';

export const loginApi = {
  /**
   * 登录 
   */
  login: (param) => {
    return postRequest('/system/login', param);
  },

  /**
   * 退出登录 
   */
  logout: () => {
    return postRequest('/system/login/logout');
  },

  /**
   * 获取验证码 
   */
  getCaptcha: () => {
    return getRequest('/system/login/getCaptcha');
  },

  /**
   * 获取登录信息 
   */
  getLoginInfo: () => {
    return getRequest('/system/login/getLoginInfo');
  },
};
