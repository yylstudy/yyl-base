/*
 Navicat Premium Data Transfer

 Source Server         : 172.16.252.130
 Source Server Type    : MySQL
 Source Server Version : 50731
 Source Host           : 172.16.252.130:3306
 Source Schema         : system

 Target Server Type    : MySQL
 Target Server Version : 50731
 File Encoding         : 65001

 Date: 02/04/2024 17:14:04
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for corp
-- ----------------------------
DROP TABLE IF EXISTS `corp`;
CREATE TABLE `corp`  (
  `id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '企业ID',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '企业名称',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '上次修改时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_by` bigint(20) NULL DEFAULT NULL,
  `update_by` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of corp
-- ----------------------------
INSERT INTO `corp` VALUES ('0000', '运营级企业', '2024-03-12 19:06:42', '2024-03-12 19:06:03', 1764262899762610176, NULL);

-- ----------------------------
-- Table structure for corp_business
-- ----------------------------
DROP TABLE IF EXISTS `corp_business`;
CREATE TABLE `corp_business`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `corp_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '企业编码',
  `business` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '企业名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业业务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of corp_business
-- ----------------------------

-- ----------------------------
-- Table structure for corp_user
-- ----------------------------
DROP TABLE IF EXISTS `corp_user`;
CREATE TABLE `corp_user`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `corp_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '企业id',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '企业用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of corp_user
-- ----------------------------
INSERT INTO `corp_user` VALUES (1, '0000', 1764262899762610176);

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数名字',
  `key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '参数key',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '上次修改时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_by` bigint(20) NULL DEFAULT NULL,
  `update_by` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_depart
-- ----------------------------
DROP TABLE IF EXISTS `sys_depart`;
CREATE TABLE `sys_depart`  (
  `id` bigint(20) NOT NULL COMMENT '部门主键id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '部门名称',
  `manager_id` bigint(20) NULL DEFAULT NULL COMMENT '部门负责人id',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '部门的父级id',
  `corp_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '企业ID',
  `sort` int(11) NOT NULL COMMENT '部门排序',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_by` bigint(20) NULL DEFAULT NULL,
  `update_by` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '部门' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_depart
-- ----------------------------
INSERT INTO `sys_depart` VALUES (1764260112911843328, '北京承启通', 1764262899762610176, 0, '0000', 1, '2024-03-29 15:23:07', '2022-10-19 20:17:09', NULL, 1764262899762610176);

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint(20) NOT NULL,
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '编码',
  `dict_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `create_by` bigint(20) NULL DEFAULT NULL,
  `update_by` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字段key' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (1764926325552640001, 'business', '业务类型', '', '2024-03-05 16:10:01', '2024-03-05 16:10:01', 1764262899762610176, NULL);
INSERT INTO `sys_dict` VALUES (1771014637816197122, 'menuScope', '菜单范围', '', '2024-03-22 11:22:48', '2024-03-22 11:22:48', 1764262899762610176, NULL);

-- ----------------------------
-- Table structure for sys_dict_item
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item`  (
  `id` bigint(20) NOT NULL,
  `dict_id` bigint(20) NOT NULL,
  `item_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '编码',
  `item_text` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `create_by` bigint(20) NULL DEFAULT NULL,
  `update_by` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '字典的值' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_item
-- ----------------------------
INSERT INTO `sys_dict_item` VALUES (1764926368712028161, 1764926325552640001, '10000', '号码隐藏', 1, '2024-03-05 16:10:11', '2024-03-05 16:10:12', 1764262899762610176, NULL);
INSERT INTO `sys_dict_item` VALUES (1764926398919405570, 1764926325552640001, '11000', '语音通知', 1, '2024-03-05 16:10:19', '2024-03-05 16:10:19', 1764262899762610176, NULL);
INSERT INTO `sys_dict_item` VALUES (1771014684385554434, 1771014637816197122, '1', '公共', 1, '2024-03-22 15:15:26', '2024-03-22 11:22:59', 1764262899762610176, 1764262899762610176);
INSERT INTO `sys_dict_item` VALUES (1771014725674283010, 1771014637816197122, '2', '业务共用', 2, '2024-03-22 15:15:30', '2024-03-22 11:23:09', 1764262899762610176, 1764262899762610176);
INSERT INTO `sys_dict_item` VALUES (1771014763951501313, 1771014637816197122, '3', '普通', 3, '2024-03-22 15:15:33', '2024-03-22 11:23:18', 1764262899762610176, 1764262899762610176);

-- ----------------------------
-- Table structure for sys_login_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `username` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `login_ip` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户ip',
  `login_result` int(11) NOT NULL COMMENT '登录结果：0成功 1失败 2 退出',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `customer_id`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户登录日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_login_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '菜单ID',
  `menu_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `menu_type` int(11) NOT NULL COMMENT '类型',
  `parent_id` bigint(20) NOT NULL COMMENT '父菜单ID',
  `business` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务',
  `menu_scope` tinyint(1) NULL DEFAULT NULL COMMENT '1公共菜单，2业务共用菜单，3普通菜单',
  `sort` int(11) NULL DEFAULT NULL COMMENT '显示顺序',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `api_perms` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '后端权限字符串',
  `web_perms` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '前端权限字符串',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `frame_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为外链',
  `frame_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '外链地址',
  `cache_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否缓存',
  `visible_flag` tinyint(1) NOT NULL DEFAULT 1 COMMENT '显示状态',
  `disabled_flag` tinyint(1) NOT NULL DEFAULT 0 COMMENT '禁用状态',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1764278772456017921, '批量删除', 3, 1764278772456017928, '', 3, NULL, NULL, NULL, 'system:config:delete', 'system:config:delete', NULL, 0, NULL, 1, 1, 0, 1, '2024-03-03 21:16:53', 1764262899762610176, '2024-03-22 17:50:32');
INSERT INTO `sys_menu` VALUES (1764278772456017923, '系统管理', 2, 0, NULL, 1, 1, '/system', NULL, NULL, NULL, 'SettingOutlined', 0, NULL, 0, 1, 0, 1, '2021-08-13 16:41:33', 1764262899762610176, '2024-03-22 14:19:49');
INSERT INTO `sys_menu` VALUES (1764278772456017924, '菜单管理', 2, 1764278772456017923, NULL, 3, 3, '/system/menu/list', '/system/menu/menu-list.vue', NULL, NULL, 'CopyOutlined', 0, NULL, 1, 1, 0, 2, '2021-08-09 15:04:35', 1764262899762610176, '2024-03-22 17:46:27');
INSERT INTO `sys_menu` VALUES (1764278772456017925, '用户管理', 2, 1764278772456017923, NULL, 1, 1, '/system/user/list', '/system/user/depart/index.vue', NULL, NULL, 'AuditOutlined', 0, NULL, 0, 1, 0, 1, '2021-08-12 16:21:50', 1764262899762610176, '2024-04-02 16:23:10');
INSERT INTO `sys_menu` VALUES (1764278772456017926, '角色管理', 2, 1764278772456017923, NULL, 1, 2, '/system/role/list', '/system/role/index.vue', NULL, NULL, 'SlidersOutlined', 0, NULL, 0, 1, 0, 1, '2021-08-26 10:31:00', 1764262899762610176, '2024-03-22 17:17:17');
INSERT INTO `sys_menu` VALUES (1764278772456017928, '参数配置', 2, 1764278772456017923, NULL, 3, 5, '/system/config/list', '/system/config/list.vue', NULL, NULL, 'AntDesignOutlined', 0, NULL, 0, 1, 0, 1, '2022-05-27 13:34:41', 1764262899762610176, '2024-03-22 17:50:55');
INSERT INTO `sys_menu` VALUES (1764278772456017929, '数据字典', 2, 1764278772456017923, NULL, 3, 4, '/system/dict/list', '/system/dict/list.vue', NULL, NULL, 'BarcodeOutlined', 0, NULL, 0, 1, 0, 1, '2022-05-27 17:53:00', 1764262899762610176, '2024-03-22 17:49:56');
INSERT INTO `sys_menu` VALUES (1764278772456017930, '登录日志', 2, 1764278772456017923, NULL, 3, 6, '/system/loginlog/list', '/system/loginLog/list.vue', NULL, NULL, 'LoginOutlined', 0, NULL, 0, 1, 0, 1, '2022-06-28 15:01:38', 1764262899762610176, '2024-03-22 17:51:03');
INSERT INTO `sys_menu` VALUES (1764278772456017931, '删除', 3, 1764278772456017924, '', 3, NULL, NULL, NULL, 'system:menu:batchDelete', 'system:menu:batchDelete', NULL, 0, NULL, 0, 1, 0, 1, '2021-08-12 09:45:56', 1764262899762610176, '2024-03-22 17:48:38');
INSERT INTO `sys_menu` VALUES (1764278772456017932, '添加部门', 3, 1764278772456017925, '', 1, 1, NULL, NULL, 'system:depart:add', 'system:depart:add', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-26 23:33:37', 1764262899762610176, '2024-03-22 17:16:41');
INSERT INTO `sys_menu` VALUES (1764278772456017933, '修改部门', 3, 1764278772456017925, '', 1, 2, NULL, NULL, 'system:depart:update', 'system:depart:update', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-26 23:34:11', 1764262899762610176, '2024-03-22 17:16:45');
INSERT INTO `sys_menu` VALUES (1764278772456017934, '删除部门', 3, 1764278772456017925, '', 1, 3, NULL, NULL, 'system:depart:delete', 'system:depart:delete', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-26 23:34:49', 1764262899762610176, '2024-03-22 17:16:49');
INSERT INTO `sys_menu` VALUES (1764278772456017935, '添加用户', 3, 1764278772456017925, '', 1, NULL, NULL, NULL, 'system:user:add', 'system:user:add', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:11:38', 1764262899762610176, '2024-03-22 17:15:50');
INSERT INTO `sys_menu` VALUES (1764278772456017936, '编辑用户', 3, 1764278772456017925, '', 1, NULL, NULL, NULL, 'system:user:update', 'system:user:update', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:12:10', 1764262899762610176, '2024-03-22 17:15:58');
INSERT INTO `sys_menu` VALUES (1764278772456017937, '禁用启用', 3, 1764278772456017925, '', 1, NULL, NULL, NULL, 'system:user:disabled', 'system:user:disabled', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:12:37', 1764262899762610176, '2024-03-22 17:16:02');
INSERT INTO `sys_menu` VALUES (1764278772456017938, '调整用户部门', 3, 1764278772456017925, '', 1, NULL, NULL, NULL, 'system:user:user:update', 'system:user:user:update', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:12:59', 1764262899762610176, '2024-03-22 17:16:07');
INSERT INTO `sys_menu` VALUES (1764278772456017939, '重置密码', 3, 1764278772456017925, '', 1, NULL, NULL, NULL, 'system:user:password:reset', 'system:user:password:reset', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:13:30', 1764262899762610176, '2024-03-22 17:16:11');
INSERT INTO `sys_menu` VALUES (1764278772456017940, '删除用户', 3, 1764278772456017925, '', 1, NULL, NULL, NULL, 'system:user:delete', 'system:user:delete', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:14:08', 1764262899762610176, '2024-03-29 10:00:34');
INSERT INTO `sys_menu` VALUES (1764278772456017941, '添加角色', 3, 1764278772456017926, '', 1, NULL, NULL, NULL, 'system:role:add', 'system:role:add', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:34:00', 1764262899762610176, '2024-03-22 17:17:30');
INSERT INTO `sys_menu` VALUES (1764278772456017942, '删除角色', 3, 1764278772456017926, '', 1, NULL, NULL, NULL, 'system:role:delete', 'system:role:delete', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:34:19', 1764262899762610176, '2024-03-22 17:17:36');
INSERT INTO `sys_menu` VALUES (1764278772456017943, '编辑角色', 3, 1764278772456017926, '', 1, NULL, NULL, NULL, 'system:role:update', 'system:role:update', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:34:55', 1, '2024-03-22 17:18:04');
INSERT INTO `sys_menu` VALUES (1764278772456017944, '批量移除用户', 3, 1764278772456017926, '', 1, NULL, NULL, NULL, 'system:role:user:batch:delete', 'system:role:user:batch:delete', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:39:05', 1, '2024-03-22 17:18:04');
INSERT INTO `sys_menu` VALUES (1764278772456017945, '移除用户', 3, 1764278772456017926, '', 1, NULL, NULL, NULL, 'system:role:user:delete', 'system:role:user:delete', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:39:21', 1, '2024-03-22 17:18:04');
INSERT INTO `sys_menu` VALUES (1764278772456017946, '添加用户', 3, 1764278772456017926, '', 1, NULL, NULL, NULL, 'system:role:user:add', 'system:role:user:add', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:39:38', 1764262899762610176, '2024-03-22 17:18:04');
INSERT INTO `sys_menu` VALUES (1764278772456017947, '修改权限', 3, 1764278772456017926, '', 1, NULL, NULL, NULL, 'system:role:menu:update', 'system:role:menu:update', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:41:55', 1, '2024-03-22 17:18:04');
INSERT INTO `sys_menu` VALUES (1764278772456017948, '添加', 3, 1764278772456017924, '', 3, NULL, NULL, NULL, 'system:menu:add', 'system:menu:add', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:44:37', 1764262899762610176, '2024-03-22 17:48:42');
INSERT INTO `sys_menu` VALUES (1764278772456017949, '编辑', 3, 1764278772456017924, '', 3, NULL, NULL, NULL, 'system:menu:update', 'system:menu:update', NULL, 0, NULL, 0, 1, 0, 1, '2022-05-27 00:44:59', 1764262899762610176, '2024-03-22 17:48:46');
INSERT INTO `sys_menu` VALUES (1764278772456017951, '新建', 3, 1764278772456017929, '', 3, NULL, NULL, NULL, 'system:dict:add', 'system:dict:add', NULL, 0, NULL, 0, 1, 0, 1, '2022-10-15 23:23:51', 1764262899762610176, '2024-03-22 17:50:11');
INSERT INTO `sys_menu` VALUES (1764278772456017952, '编辑', 3, 1764278772456017929, '', 3, NULL, NULL, NULL, 'system:dict:edit', 'system:dict:edit', NULL, 0, NULL, 0, 1, 0, 1, '2022-10-15 23:24:05', 1764262899762610176, '2024-03-22 17:50:15');
INSERT INTO `sys_menu` VALUES (1764278772456017953, '批量删除', 3, 1764278772456017929, '', 3, NULL, NULL, NULL, 'system:dict:delete', 'system:dict:delete', NULL, 0, NULL, 0, 1, 0, 1, '2022-10-15 23:24:34', 1764262899762610176, '2024-03-22 17:50:20');
INSERT INTO `sys_menu` VALUES (1764278772456017955, '新建', 3, 1764278772456017928, '', 3, NULL, NULL, NULL, 'system:config:add', 'system:config:add', NULL, 0, NULL, 0, 1, 0, 1, '2022-10-15 23:26:56', 1764262899762610176, '2024-03-22 17:50:37');
INSERT INTO `sys_menu` VALUES (1764278772456017956, '编辑', 3, 1764278772456017928, '', 3, NULL, NULL, NULL, 'system:config:update', 'system:config:update', NULL, 0, NULL, 0, 1, 0, 1, '2022-10-15 23:27:07', 1764262899762610176, '2024-03-22 17:50:44');
INSERT INTO `sys_menu` VALUES (1764278772456017957, '查询', 3, 1764278772456017928, '', 3, NULL, NULL, NULL, 'system:config:query', 'system:config:query', NULL, 0, NULL, 0, 1, 0, 1, '2022-10-21 20:45:14', 1764262899762610176, '2024-03-22 17:50:48');
INSERT INTO `sys_menu` VALUES (1764278772456017958, '查询', 3, 1764278772456017930, '', 3, NULL, NULL, NULL, 'system:loginLog:query', 'system:loginLog:query', NULL, 0, NULL, 0, 1, 0, 1, '2022-10-21 21:05:11', 1764262899762610176, '2024-03-22 17:51:12');
INSERT INTO `sys_menu` VALUES (1764818440013312001, '操作日志', 2, 1764278772456017923, NULL, 3, 7, '/system/operatelog/list', '/system/operatelog/list.vue', NULL, NULL, 'AuditOutlined', 0, NULL, 1, 1, 0, 1764262899762610176, '2024-03-05 09:01:20', 1764262899762610176, '2024-03-22 17:51:20');
INSERT INTO `sys_menu` VALUES (1764851452251742210, '查询', 3, 1764818440013312001, '', 3, 1, NULL, NULL, 'system:operateLog:query', 'system:operateLog:query', NULL, 0, NULL, 1, 1, 0, 1764262899762610176, '2024-03-05 11:12:30', 1764262899762610176, '2024-03-22 17:51:24');
INSERT INTO `sys_menu` VALUES (1764851562381582338, '详情', 3, 1764818440013312001, '', 3, 2, NULL, NULL, 'system:operateLog:detail', 'system:operateLog:detail', NULL, 0, NULL, 1, 1, 0, 1764262899762610176, '2024-03-05 11:12:56', 1764262899762610176, '2024-03-22 17:51:29');
INSERT INTO `sys_menu` VALUES (1764936632962461698, '企业管理', 2, 1765179993287909377, '10000', 1, NULL, '/system/corp/list', '/system/corp/list.vue', NULL, NULL, 'AlignCenterOutlined', 0, NULL, 1, 1, 0, 1764262899762610176, '2024-03-05 16:50:59', 1764262899762610176, '2024-03-22 17:18:04');
INSERT INTO `sys_menu` VALUES (1765179993287909377, '基础信息管理', 1, 0, '10000', 1, NULL, NULL, NULL, NULL, NULL, 'AimOutlined', 0, NULL, 1, 1, 0, 1764262899762610176, '2024-03-06 08:58:01', 1764262899762610176, '2024-04-02 16:30:35');

-- ----------------------------
-- Table structure for sys_operate_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_operate_log`;
CREATE TABLE `sys_operate_log`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名称',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作内容',
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求路径',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法',
  `param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `cost_time` int(10) NULL DEFAULT NULL COMMENT '请求耗时',
  `ip` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求ip',
  `success_flag` tinyint(4) NULL DEFAULT NULL COMMENT '请求结果 0失败 1成功',
  `fail_reason` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '失败原因',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_operate_log
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `role_name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色编码',
  `corp_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '企业ID',
  `corp_admin` tinyint(1) NULL DEFAULT NULL COMMENT '是否企业管理员 0否1是',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '角色描述',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_code_uni`(`role_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1763376066984783873, '管理员', 'gly', '0000', 0, NULL, 1, '2024-03-01 09:29:51', NULL, '2024-03-12 19:00:59');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(20) NOT NULL COMMENT '主键id',
  `role_id` bigint(20) NOT NULL COMMENT '角色id',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_menu_id`(`menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色-菜单\r\n' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1765185801757257729, 1763376066984783873, 1764278772456017923);
INSERT INTO `sys_role_menu` VALUES (1765185801757257730, 1763376066984783873, 1764278772456017924);
INSERT INTO `sys_role_menu` VALUES (1765185801757257731, 1763376066984783873, 1764278772456017931);
INSERT INTO `sys_role_menu` VALUES (1765185801757257732, 1763376066984783873, 1764278772456017948);
INSERT INTO `sys_role_menu` VALUES (1765185801757257733, 1763376066984783873, 1764278772456017949);
INSERT INTO `sys_role_menu` VALUES (1765185801757257734, 1763376066984783873, 1764278772456017925);
INSERT INTO `sys_role_menu` VALUES (1765185801757257735, 1763376066984783873, 1764278772456017932);
INSERT INTO `sys_role_menu` VALUES (1765185801757257736, 1763376066984783873, 1764278772456017933);
INSERT INTO `sys_role_menu` VALUES (1765185801757257737, 1763376066984783873, 1764278772456017934);
INSERT INTO `sys_role_menu` VALUES (1765185801757257738, 1763376066984783873, 1764278772456017935);
INSERT INTO `sys_role_menu` VALUES (1765185801757257739, 1763376066984783873, 1764278772456017936);
INSERT INTO `sys_role_menu` VALUES (1765185801757257740, 1763376066984783873, 1764278772456017937);
INSERT INTO `sys_role_menu` VALUES (1765185801757257741, 1763376066984783873, 1764278772456017938);
INSERT INTO `sys_role_menu` VALUES (1765185801757257742, 1763376066984783873, 1764278772456017939);
INSERT INTO `sys_role_menu` VALUES (1765185801757257743, 1763376066984783873, 1764278772456017940);
INSERT INTO `sys_role_menu` VALUES (1765185801757257744, 1763376066984783873, 1764278772456017926);
INSERT INTO `sys_role_menu` VALUES (1765185801757257745, 1763376066984783873, 1764278772456017941);
INSERT INTO `sys_role_menu` VALUES (1765185801757257746, 1763376066984783873, 1764278772456017942);
INSERT INTO `sys_role_menu` VALUES (1765185801757257747, 1763376066984783873, 1764278772456017943);
INSERT INTO `sys_role_menu` VALUES (1765185801757257748, 1763376066984783873, 1764278772456017944);
INSERT INTO `sys_role_menu` VALUES (1765185801757257749, 1763376066984783873, 1764278772456017945);
INSERT INTO `sys_role_menu` VALUES (1765185801757257750, 1763376066984783873, 1764278772456017946);
INSERT INTO `sys_role_menu` VALUES (1765185801757257751, 1763376066984783873, 1764278772456017947);
INSERT INTO `sys_role_menu` VALUES (1765185801757257752, 1763376066984783873, 1764278772456017928);
INSERT INTO `sys_role_menu` VALUES (1765185801757257753, 1763376066984783873, 1764278772456017921);
INSERT INTO `sys_role_menu` VALUES (1765185801757257754, 1763376066984783873, 1764278772456017955);
INSERT INTO `sys_role_menu` VALUES (1765185801757257755, 1763376066984783873, 1764278772456017956);
INSERT INTO `sys_role_menu` VALUES (1765185801757257756, 1763376066984783873, 1764278772456017957);
INSERT INTO `sys_role_menu` VALUES (1765185801757257757, 1763376066984783873, 1764278772456017929);
INSERT INTO `sys_role_menu` VALUES (1765185801757257758, 1763376066984783873, 1764278772456017951);
INSERT INTO `sys_role_menu` VALUES (1765185801757257759, 1763376066984783873, 1764278772456017952);
INSERT INTO `sys_role_menu` VALUES (1765185801757257760, 1763376066984783873, 1764278772456017953);
INSERT INTO `sys_role_menu` VALUES (1765185801757257762, 1763376066984783873, 1764278772456017930);
INSERT INTO `sys_role_menu` VALUES (1765185801757257763, 1763376066984783873, 1764278772456017958);
INSERT INTO `sys_role_menu` VALUES (1770741292021432322, 1763376066984783873, 1765179993287909377);
INSERT INTO `sys_role_menu` VALUES (1770741292021432323, 1763376066984783873, 1764936632962461698);
INSERT INTO `sys_role_menu` VALUES (1770758820688834562, 1763376066984783873, 1764818440013312001);
INSERT INTO `sys_role_menu` VALUES (1770758820701417474, 1763376066984783873, 1764851452251742210);
INSERT INTO `sys_role_menu` VALUES (1770758820701417475, 1763376066984783873, 1764851562381582338);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL COMMENT '主键',
  `password` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '登录密码',
  `username` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '真实姓名',
  `gender` tinyint(1) NOT NULL DEFAULT 0 COMMENT '性别',
  `phone` varchar(15) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '手机号码',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `depart_id` bigint(20) NULL DEFAULT NULL COMMENT '部门id',
  `disabled_flag` tinyint(3) UNSIGNED NOT NULL COMMENT '是否被禁用 0否1是',
  `deleted_flag` tinyint(3) UNSIGNED NOT NULL COMMENT '是否删除0否 1是',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_by` bigint(20) NULL DEFAULT NULL,
  `update_by` bigint(20) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '员工表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1764262899762610176, 'c1be66f9cd091ee9ff5dec9ecd037695', 'admin', 1, '15255178553', '1594818954@qq.com', 1764260112911843328, 0, 0, NULL, '2024-04-02 16:06:41', '2018-05-11 09:38:54', NULL, 1764262899762610176);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL COMMENT '员工id',
  `role_id` bigint(20) NOT NULL COMMENT '角色id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_employee`(`role_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色员工功能表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1775070436712554498, 1764262899762610176, 1763376066984783873);

SET FOREIGN_KEY_CHECKS = 1;
