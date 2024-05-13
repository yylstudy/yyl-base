/*
 Navicat Premium Data Transfer

 Source Server         : 10.100.243.5
 Source Server Type    : MySQL
 Source Server Version : 50731
 Source Host           : 10.100.243.5:3306
 Source Schema         : test1

 Target Server Type    : MySQL
 Target Server Version : 50731
 File Encoding         : 65001

 Date: 08/12/2022 13:47:46
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for acr_record_transfer_0
-- ----------------------------
DROP TABLE IF EXISTS `acr_record_transfer_0`;
CREATE TABLE `acr_record_transfer_0`  (
  `streamnumber` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `servicekey` int(11) NULL DEFAULT NULL,
  `callcost` int(11) NULL DEFAULT NULL,
  `calledpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callingpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `chargemode` smallint(6) NULL DEFAULT NULL,
  `specificchargedpar` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `translatednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `startdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stopdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `duration` int(11) NULL DEFAULT NULL,
  `chargeclass` int(11) NULL DEFAULT NULL,
  `transparentparamet` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calltype` int(11) NULL DEFAULT NULL,
  `callersubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleesubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrcallid` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallingnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callerpnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleepnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reroute` int(11) NULL DEFAULT NULL,
  `groupnumber` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callcategory` int(11) NULL DEFAULT NULL,
  `chargetype` int(11) NULL DEFAULT NULL,
  `userpin` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrtype` int(11) NULL DEFAULT NULL,
  `videocallflag` int(11) NULL DEFAULT NULL,
  `serviceid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `forwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `extforwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `srfmsgid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `msserver` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `begintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `releasecause` int(11) NULL DEFAULT NULL,
  `releasereason` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `areanumber` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calledareacode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `localorlong` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` int(11) NULL DEFAULT 0,
  `dtmfkey` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `operator` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`streamnumber`) USING BTREE,
  INDEX `index_callingpartynumber`(`callingpartynumber`) USING BTREE,
  INDEX `index_calledpartynumber`(`calledpartynumber`) USING BTREE,
  INDEX `index_specificchargedpar`(`specificchargedpar`) USING BTREE,
  INDEX `index_translatednumber`(`translatednumber`) USING BTREE,
  INDEX `index_servicekey`(`servicekey`) USING BTREE,
  INDEX `index_stopdateandtime`(`stopdateandtime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for acr_record_transfer_1
-- ----------------------------
DROP TABLE IF EXISTS `acr_record_transfer_1`;
CREATE TABLE `acr_record_transfer_1`  (
  `streamnumber` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `servicekey` int(11) NULL DEFAULT NULL,
  `callcost` int(11) NULL DEFAULT NULL,
  `calledpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callingpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `chargemode` smallint(6) NULL DEFAULT NULL,
  `specificchargedpar` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `translatednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `startdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stopdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `duration` int(11) NULL DEFAULT NULL,
  `chargeclass` int(11) NULL DEFAULT NULL,
  `transparentparamet` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calltype` int(11) NULL DEFAULT NULL,
  `callersubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleesubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrcallid` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallingnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callerpnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleepnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reroute` int(11) NULL DEFAULT NULL,
  `groupnumber` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callcategory` int(11) NULL DEFAULT NULL,
  `chargetype` int(11) NULL DEFAULT NULL,
  `userpin` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrtype` int(11) NULL DEFAULT NULL,
  `videocallflag` int(11) NULL DEFAULT NULL,
  `serviceid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `forwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `extforwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `srfmsgid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `msserver` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `begintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `releasecause` int(11) NULL DEFAULT NULL,
  `releasereason` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `areanumber` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calledareacode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `localorlong` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` int(11) NULL DEFAULT 0,
  `dtmfkey` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `operator` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`streamnumber`) USING BTREE,
  INDEX `index_callingpartynumber`(`callingpartynumber`) USING BTREE,
  INDEX `index_calledpartynumber`(`calledpartynumber`) USING BTREE,
  INDEX `index_specificchargedpar`(`specificchargedpar`) USING BTREE,
  INDEX `index_translatednumber`(`translatednumber`) USING BTREE,
  INDEX `index_servicekey`(`servicekey`) USING BTREE,
  INDEX `index_stopdateandtime`(`stopdateandtime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for acr_record_transfer_2
-- ----------------------------
DROP TABLE IF EXISTS `acr_record_transfer_2`;
CREATE TABLE `acr_record_transfer_2`  (
  `streamnumber` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `servicekey` int(11) NULL DEFAULT NULL,
  `callcost` int(11) NULL DEFAULT NULL,
  `calledpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callingpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `chargemode` smallint(6) NULL DEFAULT NULL,
  `specificchargedpar` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `translatednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `startdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stopdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `duration` int(11) NULL DEFAULT NULL,
  `chargeclass` int(11) NULL DEFAULT NULL,
  `transparentparamet` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calltype` int(11) NULL DEFAULT NULL,
  `callersubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleesubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrcallid` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallingnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callerpnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleepnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reroute` int(11) NULL DEFAULT NULL,
  `groupnumber` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callcategory` int(11) NULL DEFAULT NULL,
  `chargetype` int(11) NULL DEFAULT NULL,
  `userpin` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrtype` int(11) NULL DEFAULT NULL,
  `videocallflag` int(11) NULL DEFAULT NULL,
  `serviceid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `forwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `extforwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `srfmsgid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `msserver` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `begintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `releasecause` int(11) NULL DEFAULT NULL,
  `releasereason` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `areanumber` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calledareacode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `localorlong` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` int(11) NULL DEFAULT 0,
  `dtmfkey` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `operator` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`streamnumber`) USING BTREE,
  INDEX `index_callingpartynumber`(`callingpartynumber`) USING BTREE,
  INDEX `index_calledpartynumber`(`calledpartynumber`) USING BTREE,
  INDEX `index_specificchargedpar`(`specificchargedpar`) USING BTREE,
  INDEX `index_translatednumber`(`translatednumber`) USING BTREE,
  INDEX `index_servicekey`(`servicekey`) USING BTREE,
  INDEX `index_stopdateandtime`(`stopdateandtime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for acr_record_transfer_3
-- ----------------------------
DROP TABLE IF EXISTS `acr_record_transfer_3`;
CREATE TABLE `acr_record_transfer_3`  (
  `streamnumber` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `servicekey` int(11) NULL DEFAULT NULL,
  `callcost` int(11) NULL DEFAULT NULL,
  `calledpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callingpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `chargemode` smallint(6) NULL DEFAULT NULL,
  `specificchargedpar` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `translatednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `startdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stopdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `duration` int(11) NULL DEFAULT NULL,
  `chargeclass` int(11) NULL DEFAULT NULL,
  `transparentparamet` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calltype` int(11) NULL DEFAULT NULL,
  `callersubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleesubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrcallid` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallingnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callerpnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleepnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reroute` int(11) NULL DEFAULT NULL,
  `groupnumber` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callcategory` int(11) NULL DEFAULT NULL,
  `chargetype` int(11) NULL DEFAULT NULL,
  `userpin` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrtype` int(11) NULL DEFAULT NULL,
  `videocallflag` int(11) NULL DEFAULT NULL,
  `serviceid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `forwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `extforwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `srfmsgid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `msserver` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `begintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `releasecause` int(11) NULL DEFAULT NULL,
  `releasereason` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `areanumber` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calledareacode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `localorlong` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` int(11) NULL DEFAULT 0,
  `dtmfkey` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `operator` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`streamnumber`) USING BTREE,
  INDEX `index_callingpartynumber`(`callingpartynumber`) USING BTREE,
  INDEX `index_calledpartynumber`(`calledpartynumber`) USING BTREE,
  INDEX `index_specificchargedpar`(`specificchargedpar`) USING BTREE,
  INDEX `index_translatednumber`(`translatednumber`) USING BTREE,
  INDEX `index_servicekey`(`servicekey`) USING BTREE,
  INDEX `index_stopdateandtime`(`stopdateandtime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for acr_record_transfer_4
-- ----------------------------
DROP TABLE IF EXISTS `acr_record_transfer_4`;
CREATE TABLE `acr_record_transfer_4`  (
  `streamnumber` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `servicekey` int(11) NULL DEFAULT NULL,
  `callcost` int(11) NULL DEFAULT NULL,
  `calledpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callingpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `chargemode` smallint(6) NULL DEFAULT NULL,
  `specificchargedpar` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `translatednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `startdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stopdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `duration` int(11) NULL DEFAULT NULL,
  `chargeclass` int(11) NULL DEFAULT NULL,
  `transparentparamet` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calltype` int(11) NULL DEFAULT NULL,
  `callersubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleesubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrcallid` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallingnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callerpnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleepnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reroute` int(11) NULL DEFAULT NULL,
  `groupnumber` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callcategory` int(11) NULL DEFAULT NULL,
  `chargetype` int(11) NULL DEFAULT NULL,
  `userpin` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrtype` int(11) NULL DEFAULT NULL,
  `videocallflag` int(11) NULL DEFAULT NULL,
  `serviceid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `forwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `extforwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `srfmsgid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `msserver` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `begintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `releasecause` int(11) NULL DEFAULT NULL,
  `releasereason` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `areanumber` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calledareacode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `localorlong` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` int(11) NULL DEFAULT 0,
  `dtmfkey` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `operator` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`streamnumber`) USING BTREE,
  INDEX `index_callingpartynumber`(`callingpartynumber`) USING BTREE,
  INDEX `index_calledpartynumber`(`calledpartynumber`) USING BTREE,
  INDEX `index_specificchargedpar`(`specificchargedpar`) USING BTREE,
  INDEX `index_translatednumber`(`translatednumber`) USING BTREE,
  INDEX `index_servicekey`(`servicekey`) USING BTREE,
  INDEX `index_stopdateandtime`(`stopdateandtime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for acr_record_transfer_5
-- ----------------------------
DROP TABLE IF EXISTS `acr_record_transfer_5`;
CREATE TABLE `acr_record_transfer_5`  (
  `streamnumber` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `servicekey` int(11) NULL DEFAULT NULL,
  `callcost` int(11) NULL DEFAULT NULL,
  `calledpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callingpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `chargemode` smallint(6) NULL DEFAULT NULL,
  `specificchargedpar` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `translatednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `startdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stopdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `duration` int(11) NULL DEFAULT NULL,
  `chargeclass` int(11) NULL DEFAULT NULL,
  `transparentparamet` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calltype` int(11) NULL DEFAULT NULL,
  `callersubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleesubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrcallid` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallingnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callerpnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleepnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reroute` int(11) NULL DEFAULT NULL,
  `groupnumber` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callcategory` int(11) NULL DEFAULT NULL,
  `chargetype` int(11) NULL DEFAULT NULL,
  `userpin` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrtype` int(11) NULL DEFAULT NULL,
  `videocallflag` int(11) NULL DEFAULT NULL,
  `serviceid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `forwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `extforwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `srfmsgid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `msserver` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `begintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `releasecause` int(11) NULL DEFAULT NULL,
  `releasereason` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `areanumber` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calledareacode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `localorlong` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` int(11) NULL DEFAULT 0,
  `dtmfkey` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `operator` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`streamnumber`) USING BTREE,
  INDEX `index_callingpartynumber`(`callingpartynumber`) USING BTREE,
  INDEX `index_calledpartynumber`(`calledpartynumber`) USING BTREE,
  INDEX `index_specificchargedpar`(`specificchargedpar`) USING BTREE,
  INDEX `index_translatednumber`(`translatednumber`) USING BTREE,
  INDEX `index_servicekey`(`servicekey`) USING BTREE,
  INDEX `index_stopdateandtime`(`stopdateandtime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for acr_record_transfer_6
-- ----------------------------
DROP TABLE IF EXISTS `acr_record_transfer_6`;
CREATE TABLE `acr_record_transfer_6`  (
  `streamnumber` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `servicekey` int(11) NULL DEFAULT NULL,
  `callcost` int(11) NULL DEFAULT NULL,
  `calledpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callingpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `chargemode` smallint(6) NULL DEFAULT NULL,
  `specificchargedpar` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `translatednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `startdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stopdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `duration` int(11) NULL DEFAULT NULL,
  `chargeclass` int(11) NULL DEFAULT NULL,
  `transparentparamet` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calltype` int(11) NULL DEFAULT NULL,
  `callersubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleesubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrcallid` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallingnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callerpnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleepnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reroute` int(11) NULL DEFAULT NULL,
  `groupnumber` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callcategory` int(11) NULL DEFAULT NULL,
  `chargetype` int(11) NULL DEFAULT NULL,
  `userpin` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrtype` int(11) NULL DEFAULT NULL,
  `videocallflag` int(11) NULL DEFAULT NULL,
  `serviceid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `forwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `extforwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `srfmsgid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `msserver` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `begintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `releasecause` int(11) NULL DEFAULT NULL,
  `releasereason` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `areanumber` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calledareacode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `localorlong` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` int(11) NULL DEFAULT 0,
  `dtmfkey` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `operator` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`streamnumber`) USING BTREE,
  INDEX `index_callingpartynumber`(`callingpartynumber`) USING BTREE,
  INDEX `index_calledpartynumber`(`calledpartynumber`) USING BTREE,
  INDEX `index_specificchargedpar`(`specificchargedpar`) USING BTREE,
  INDEX `index_translatednumber`(`translatednumber`) USING BTREE,
  INDEX `index_servicekey`(`servicekey`) USING BTREE,
  INDEX `index_stopdateandtime`(`stopdateandtime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for acr_record_transfer_7
-- ----------------------------
DROP TABLE IF EXISTS `acr_record_transfer_7`;
CREATE TABLE `acr_record_transfer_7`  (
  `streamnumber` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `servicekey` int(11) NULL DEFAULT NULL,
  `callcost` int(11) NULL DEFAULT NULL,
  `calledpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callingpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `chargemode` smallint(6) NULL DEFAULT NULL,
  `specificchargedpar` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `translatednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `startdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stopdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `duration` int(11) NULL DEFAULT NULL,
  `chargeclass` int(11) NULL DEFAULT NULL,
  `transparentparamet` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calltype` int(11) NULL DEFAULT NULL,
  `callersubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleesubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrcallid` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallingnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callerpnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleepnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reroute` int(11) NULL DEFAULT NULL,
  `groupnumber` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callcategory` int(11) NULL DEFAULT NULL,
  `chargetype` int(11) NULL DEFAULT NULL,
  `userpin` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrtype` int(11) NULL DEFAULT NULL,
  `videocallflag` int(11) NULL DEFAULT NULL,
  `serviceid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `forwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `extforwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `srfmsgid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `msserver` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `begintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `releasecause` int(11) NULL DEFAULT NULL,
  `releasereason` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `areanumber` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calledareacode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `localorlong` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` int(11) NULL DEFAULT 0,
  `dtmfkey` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `operator` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`streamnumber`) USING BTREE,
  INDEX `index_callingpartynumber`(`callingpartynumber`) USING BTREE,
  INDEX `index_calledpartynumber`(`calledpartynumber`) USING BTREE,
  INDEX `index_specificchargedpar`(`specificchargedpar`) USING BTREE,
  INDEX `index_translatednumber`(`translatednumber`) USING BTREE,
  INDEX `index_servicekey`(`servicekey`) USING BTREE,
  INDEX `index_stopdateandtime`(`stopdateandtime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for acr_record_transfer_8
-- ----------------------------
DROP TABLE IF EXISTS `acr_record_transfer_8`;
CREATE TABLE `acr_record_transfer_8`  (
  `streamnumber` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `servicekey` int(11) NULL DEFAULT NULL,
  `callcost` int(11) NULL DEFAULT NULL,
  `calledpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callingpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `chargemode` smallint(6) NULL DEFAULT NULL,
  `specificchargedpar` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `translatednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `startdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stopdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `duration` int(11) NULL DEFAULT NULL,
  `chargeclass` int(11) NULL DEFAULT NULL,
  `transparentparamet` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calltype` int(11) NULL DEFAULT NULL,
  `callersubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleesubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrcallid` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallingnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callerpnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleepnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reroute` int(11) NULL DEFAULT NULL,
  `groupnumber` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callcategory` int(11) NULL DEFAULT NULL,
  `chargetype` int(11) NULL DEFAULT NULL,
  `userpin` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrtype` int(11) NULL DEFAULT NULL,
  `videocallflag` int(11) NULL DEFAULT NULL,
  `serviceid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `forwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `extforwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `srfmsgid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `msserver` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `begintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `releasecause` int(11) NULL DEFAULT NULL,
  `releasereason` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `areanumber` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calledareacode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `localorlong` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` int(11) NULL DEFAULT 0,
  `dtmfkey` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `operator` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`streamnumber`) USING BTREE,
  INDEX `index_callingpartynumber`(`callingpartynumber`) USING BTREE,
  INDEX `index_calledpartynumber`(`calledpartynumber`) USING BTREE,
  INDEX `index_specificchargedpar`(`specificchargedpar`) USING BTREE,
  INDEX `index_translatednumber`(`translatednumber`) USING BTREE,
  INDEX `index_servicekey`(`servicekey`) USING BTREE,
  INDEX `index_stopdateandtime`(`stopdateandtime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for acr_record_transfer_9
-- ----------------------------
DROP TABLE IF EXISTS `acr_record_transfer_9`;
CREATE TABLE `acr_record_transfer_9`  (
  `streamnumber` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `servicekey` int(11) NULL DEFAULT NULL,
  `callcost` int(11) NULL DEFAULT NULL,
  `calledpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callingpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `chargemode` smallint(6) NULL DEFAULT NULL,
  `specificchargedpar` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `translatednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `startdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stopdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `duration` int(11) NULL DEFAULT NULL,
  `chargeclass` int(11) NULL DEFAULT NULL,
  `transparentparamet` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calltype` int(11) NULL DEFAULT NULL,
  `callersubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleesubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrcallid` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallingnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callerpnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleepnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reroute` int(11) NULL DEFAULT NULL,
  `groupnumber` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callcategory` int(11) NULL DEFAULT NULL,
  `chargetype` int(11) NULL DEFAULT NULL,
  `userpin` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrtype` int(11) NULL DEFAULT NULL,
  `videocallflag` int(11) NULL DEFAULT NULL,
  `serviceid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `forwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `extforwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `srfmsgid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `msserver` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `begintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `releasecause` int(11) NULL DEFAULT NULL,
  `releasereason` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `areanumber` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calledareacode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `localorlong` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` int(11) NULL DEFAULT 0,
  `dtmfkey` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `operator` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`streamnumber`) USING BTREE,
  INDEX `index_callingpartynumber`(`callingpartynumber`) USING BTREE,
  INDEX `index_calledpartynumber`(`calledpartynumber`) USING BTREE,
  INDEX `index_specificchargedpar`(`specificchargedpar`) USING BTREE,
  INDEX `index_translatednumber`(`translatednumber`) USING BTREE,
  INDEX `index_servicekey`(`servicekey`) USING BTREE,
  INDEX `index_stopdateandtime`(`stopdateandtime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for acr_record_transfes
-- ----------------------------
DROP TABLE IF EXISTS `acr_record_transfes`;
CREATE TABLE `acr_record_transfes`  (
  `streamnumber` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `servicekey` int(11) NULL DEFAULT NULL,
  `callcost` int(11) NULL DEFAULT NULL,
  `calledpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callingpartynumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `chargemode` smallint(6) NULL DEFAULT NULL,
  `specificchargedpar` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `translatednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `startdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `stopdateandtime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `duration` int(11) NULL DEFAULT NULL,
  `chargeclass` int(11) NULL DEFAULT NULL,
  `transparentparamet` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calltype` int(11) NULL DEFAULT NULL,
  `callersubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleesubgroup` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrcallid` varchar(40) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallednumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `oricallingnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callerpnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calleepnp` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `reroute` int(11) NULL DEFAULT NULL,
  `groupnumber` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callcategory` int(11) NULL DEFAULT NULL,
  `chargetype` int(11) NULL DEFAULT NULL,
  `userpin` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `acrtype` int(11) NULL DEFAULT NULL,
  `videocallflag` int(11) NULL DEFAULT NULL,
  `serviceid` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `forwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `extforwardnumber` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `srfmsgid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `msserver` varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `begintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `releasecause` int(11) NULL DEFAULT NULL,
  `releasereason` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `areanumber` varchar(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `calledareacode` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `localorlong` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `id` int(11) NULL DEFAULT 0,
  `dtmfkey` varchar(64) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `callintime` varchar(24) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `operator` int(11) NULL DEFAULT 0,
  PRIMARY KEY (`streamnumber`) USING BTREE,
  INDEX `index_callingpartynumber`(`callingpartynumber`) USING BTREE,
  INDEX `index_calledpartynumber`(`calledpartynumber`) USING BTREE,
  INDEX `index_specificchargedpar`(`specificchargedpar`) USING BTREE,
  INDEX `index_translatednumber`(`translatednumber`) USING BTREE,
  INDEX `index_servicekey`(`servicekey`) USING BTREE,
  INDEX `index_stopdateandtime`(`stopdateandtime`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for test_202210
-- ----------------------------
DROP TABLE IF EXISTS `test_202210`;
CREATE TABLE `test_202210`  (
  `id` bigint(20) NOT NULL,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for test_202211
-- ----------------------------
DROP TABLE IF EXISTS `test_202211`;
CREATE TABLE `test_202211`  (
  `id` bigint(20) NOT NULL,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for test_202212_0
-- ----------------------------
DROP TABLE IF EXISTS `test_202212_0`;
CREATE TABLE `test_202212_0`  (
  `id` bigint(20) NOT NULL,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for test_202212_1
-- ----------------------------
DROP TABLE IF EXISTS `test_202212_1`;
CREATE TABLE `test_202212_1`  (
  `id` bigint(20) NOT NULL,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for test_202212_2
-- ----------------------------
DROP TABLE IF EXISTS `test_202212_2`;
CREATE TABLE `test_202212_2`  (
  `id` bigint(20) NOT NULL,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
