import menu from './system/menu-const';
import { FLAG_NUMBER_ENUM, GENDER_ENUM, USER_TYPE_ENUM } from './common-const';
import { LAYOUT_ENUM } from './layout-const';
import loginLog from './support/login-log-const';

export default {
  FLAG_NUMBER_ENUM,
  GENDER_ENUM,
  USER_TYPE_ENUM,
  LAYOUT_ENUM,
  ...loginLog,
  ...menu,
};
