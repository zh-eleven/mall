/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80044
 Source Host           : localhost:3306
 Source Schema         : mall

 Target Server Type    : MySQL
 Target Server Version : 80044
 File Encoding         : 65001

 Date: 23/08/2026 19:42:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oms_cart_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_cart_item`;
CREATE TABLE `oms_cart_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车项ID',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_id` bigint NOT NULL COMMENT 'SKU ID',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '购买数量',
  `selected` tinyint NOT NULL DEFAULT 1 COMMENT '是否选中：0否，1是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_member_sku`(`member_id` ASC, `sku_id` ASC) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_cart_item
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `member_id` bigint NOT NULL COMMENT '会员ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待支付 1待发货 2已发货 3已完成 4已取消',
  `total_amount` decimal(12, 2) NOT NULL COMMENT '商品总金额',
  `pay_amount` decimal(12, 2) NOT NULL COMMENT '实际支付金额',
  `receiver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收货人电话',
  `receiver_post_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮政编码',
  `receiver_province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '省',
  `receiver_city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '市',
  `receiver_region` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '区县',
  `receiver_detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '详细地址',
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '订单备注',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `delivery_company` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流公司',
  `delivery_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物流单号',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime NULL DEFAULT NULL COMMENT '确认收货时间',
  `cancel_time` datetime NULL DEFAULT NULL COMMENT '取消时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_sn`(`order_sn` ASC) USING BTREE,
  INDEX `idx_member_status_time`(`member_id` ASC, `status` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_oms_order_timeout_scan`(`status` ASC, `create_time` ASC, `id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order
-- ----------------------------
INSERT INTO `oms_order` VALUES (1, 'cbdd4b8a616047f6993a27483b578e56', 4, 4, 19996.00, 19996.00, '下单测试用户', '13800000000', '100000', '北京市', '北京市', '海淀区', '下单测试路1号', 'HTTP测试订单', NULL, NULL, NULL, NULL, NULL, '2026-08-23 11:42:57', '2026-08-23 11:34:13', '2026-08-23 11:42:57');
INSERT INTO `oms_order` VALUES (2, 'a782d2efcfe94b24b3df434d10d91a4a', 4, 4, 4999.00, 4999.00, '下单测试用户', '13800000000', '100000', '北京市', '北京市', '海淀区', '下单测试路1号', 'HTTP测试订单', NULL, NULL, NULL, NULL, NULL, '2026-08-23 11:44:48', '2026-08-23 11:44:37', '2026-08-23 11:44:48');
INSERT INTO `oms_order` VALUES (3, '03d30bf1b0dd476c8de3146386131cdb', 4, 4, 4999.00, 4999.00, '下单测试用户', '13800000000', '100000', '北京市', '北京市', '海淀区', '下单测试路1号', 'HTTP测试订单', NULL, NULL, NULL, NULL, NULL, '2026-08-23 19:21:58', '2026-08-23 11:58:45', '2026-08-23 19:21:58');
INSERT INTO `oms_order` VALUES (4, 'fcf9eaac6f374019bf2b73c7cf771dc9', 4, 4, 4999.00, 4999.00, '下单测试用户', '13800000000', '100000', '北京市', '北京市', '海淀区', '下单测试路1号', 'HTTP测试订单', NULL, NULL, NULL, NULL, NULL, '2026-08-23 12:01:00', '2026-08-23 11:58:54', '2026-08-23 12:01:00');
INSERT INTO `oms_order` VALUES (5, '97f48f11133f44ee822cfd2a0bdb1154', 4, 3, 4999.00, 4999.00, '下单测试用户', '13800000000', '100000', '北京市', '北京市', '海淀区', '下单测试路1号', 'HTTP测试订单', '2026-08-23 19:22:21', '顺丰速运', 'SF1787484150', '2026-08-23 19:22:30', '2026-08-23 19:22:31', NULL, '2026-08-23 19:22:16', '2026-08-23 19:22:31');

-- ----------------------------
-- Table structure for oms_order_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_id` bigint NOT NULL COMMENT 'SKUID',
  `sku_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SKU编码快照',
  `product_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称快照',
  `product_pic` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片快照',
  `spec_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '规格快照',
  `product_price` decimal(12, 2) NOT NULL COMMENT '成交单价',
  `quantity` int NOT NULL COMMENT '购买数量',
  `subtotal` decimal(12, 2) NOT NULL COMMENT '商品小计',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id` ASC) USING BTREE,
  INDEX `idx_order_sn`(`order_sn` ASC) USING BTREE,
  INDEX `idx_sku_id`(`sku_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单商品项' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_item
-- ----------------------------
INSERT INTO `oms_order_item` VALUES (1, 1, 'cbdd4b8a616047f6993a27483b578e56', 1, 1, 'HUAWEI-1-BLACK', '华为测试手机', NULL, '[{\"name\": \"颜色\", \"value\": \"黑色\", \"attributeId\": 3}]', 4999.00, 4, 19996.00, '2026-08-23 11:34:13');
INSERT INTO `oms_order_item` VALUES (2, 2, 'a782d2efcfe94b24b3df434d10d91a4a', 1, 1, 'HUAWEI-1-BLACK', '华为测试手机', NULL, '[{\"name\": \"颜色\", \"value\": \"黑色\", \"attributeId\": 3}]', 4999.00, 1, 4999.00, '2026-08-23 11:44:37');
INSERT INTO `oms_order_item` VALUES (3, 3, '03d30bf1b0dd476c8de3146386131cdb', 1, 1, 'HUAWEI-1-BLACK', '华为测试手机', NULL, '[{\"name\": \"颜色\", \"value\": \"黑色\", \"attributeId\": 3}]', 4999.00, 1, 4999.00, '2026-08-23 11:58:45');
INSERT INTO `oms_order_item` VALUES (4, 4, 'fcf9eaac6f374019bf2b73c7cf771dc9', 1, 1, 'HUAWEI-1-BLACK', '华为测试手机', NULL, '[{\"name\": \"颜色\", \"value\": \"黑色\", \"attributeId\": 3}]', 4999.00, 1, 4999.00, '2026-08-23 11:58:54');
INSERT INTO `oms_order_item` VALUES (5, 5, '97f48f11133f44ee822cfd2a0bdb1154', 1, 1, 'HUAWEI-1-BLACK', '华为测试手机', NULL, '[{\"name\": \"颜色\", \"value\": \"黑色\", \"attributeId\": 3}]', 4999.00, 1, 4999.00, '2026-08-23 19:22:16');

-- ----------------------------
-- Table structure for pms_brand
-- ----------------------------
DROP TABLE IF EXISTS `pms_brand`;
CREATE TABLE `pms_brand`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `first_letter` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort` int NOT NULL DEFAULT 0,
  `factory_status` tinyint NOT NULL DEFAULT 0,
  `show_status` tinyint NOT NULL DEFAULT 1,
  `product_count` int NOT NULL DEFAULT 0,
  `product_comment_count` int NOT NULL DEFAULT 0,
  `logo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `big_pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `brand_story` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pms_brand_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_brand
-- ----------------------------
INSERT INTO `pms_brand` VALUES (2, '三星', 'S', 100, 1, 1, 100, 100, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20200607/57201b47N7bf15715.jpg', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221108/sanxing_banner_01.png', '三星集团是韩国的跨国企业集团，业务涉及电子、金融、机械、化学等领域。', '2026-08-18 20:48:41', '2026-08-18 20:48:41');
INSERT INTO `pms_brand` VALUES (3, '华为', 'H', 100, 1, 1, 100, 100, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20200607/5abf6f26N31658aa2.jpg', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221108/huawei_banner_01.png', '华为是一家信息与通信技术基础设施和智能终端提供商。', '2026-08-18 20:48:41', '2026-08-18 20:48:41');
INSERT INTO `pms_brand` VALUES (6, '小米', 'M', 500, 1, 1, 100, 100, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20200607/5565f5a2N0b8169ae.jpg', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221108/xiaomi_banner_01.png', '小米公司成立于2010年，主要提供智能手机、智能硬件及相关互联网服务。', '2026-08-18 20:48:41', '2026-08-18 20:48:41');
INSERT INTO `pms_brand` VALUES (21, 'OPPO', 'O', 0, 1, 1, 88, 500, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20180607/timg(6).jpg', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221108/oppo_banner_01.png', 'OPPO 是一家智能终端与移动互联网服务提供商。', '2026-08-18 20:48:41', '2026-08-18 20:48:41');
INSERT INTO `pms_brand` VALUES (51, '苹果', 'A', 200, 1, 1, 55, 200, 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20200607/49b30bb0377030d1.jpg', 'http://macro-oss.oss-cn-shenzhen.aliyuncs.com/mall/images/20221108/apple_banner_01.png', '苹果公司是一家总部位于美国加利福尼亚州的科技公司。', '2026-08-18 20:48:41', '2026-08-18 20:48:41');

-- ----------------------------
-- Table structure for pms_product
-- ----------------------------
DROP TABLE IF EXISTS `pms_product`;
CREATE TABLE `pms_product`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `brand_id` bigint NULL DEFAULT NULL COMMENT '品牌ID',
  `product_category_id` bigint NOT NULL COMMENT '二级商品分类ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `sub_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '副标题',
  `product_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品货号',
  `price` decimal(10, 2) NOT NULL COMMENT '销售价格',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '市场价格',
  `stock` int NOT NULL DEFAULT 0 COMMENT '总库存',
  `low_stock` int NOT NULL DEFAULT 0 COMMENT '库存预警值',
  `unit` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位',
  `weight` decimal(10, 2) NULL DEFAULT NULL COMMENT '重量，单位克',
  `publish_status` tinyint NOT NULL DEFAULT 0 COMMENT '上架状态：0下架，1上架',
  `new_status` tinyint NOT NULL DEFAULT 0 COMMENT '新品状态：0否，1是',
  `recommend_status` tinyint NOT NULL DEFAULT 0 COMMENT '推荐状态：0否，1是',
  `verify_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态：0未审核，1已审核',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品主图',
  `album_pics` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品相册，逗号分隔',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品描述',
  `detail_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详情标题',
  `detail_desc` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详情描述',
  `detail_html` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '商品详情HTML',
  `delete_status` tinyint NOT NULL DEFAULT 0 COMMENT '删除状态：0正常，1删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_sn`(`product_sn` ASC) USING BTREE,
  INDEX `idx_product_brand_id`(`brand_id` ASC) USING BTREE,
  INDEX `idx_product_category_id`(`product_category_id` ASC) USING BTREE,
  INDEX `idx_product_publish_status`(`publish_status` ASC) USING BTREE,
  CONSTRAINT `fk_product_brand` FOREIGN KEY (`brand_id`) REFERENCES `pms_brand` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_product_category` FOREIGN KEY (`product_category_id`) REFERENCES `pms_product_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_product
-- ----------------------------
INSERT INTO `pms_product` VALUES (1, 3, 3, '华为测试手机', 'SKU功能测试商品', 'HUAWEI-94B38F57-8B54-49B9-A357-41DCE0640154', 4999.00, 5999.00, 18, 2, '部', NULL, 1, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 0, '2026-08-20 16:14:13', '2026-08-22 19:52:01');
INSERT INTO `pms_product` VALUES (2, 3, 3, '无SKU上架测试商品', '用于测试无SKU商品不能上架', 'NO-SKU-TEST-1787226905996', 1999.00, 2299.00, 0, 0, '部', NULL, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-08-20 19:55:06', '2026-08-20 19:55:06');
INSERT INTO `pms_product` VALUES (3, 3, 3, '无SKU上架测试商品', '用于测试无SKU商品不能上架', 'NO-SKU-TEST-1787227580931', 1999.00, 2299.00, 0, 0, '部', NULL, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-08-20 20:06:20', '2026-08-20 20:06:21');
INSERT INTO `pms_product` VALUES (4, 3, 3, '无SKU上架测试商品', '用于测试无SKU商品不能上架', 'NO-SKU-TEST-1787227870018', 1999.00, 2299.00, 0, 0, '部', NULL, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-08-20 20:11:10', '2026-08-20 20:11:10');
INSERT INTO `pms_product` VALUES (5, 3, 3, '商品一致性测试', NULL, 'CONSISTENCY-TEST-1787227871201', 100.00, 300.00, 0, 0, '部', NULL, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-08-20 20:11:11', '2026-08-20 20:11:11');
INSERT INTO `pms_product` VALUES (6, 3, 3, '无SKU上架测试商品', '用于测试无SKU商品不能上架', 'NO-SKU-TEST-1787228129375', 1999.00, 2299.00, 0, 0, '部', NULL, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-08-20 20:15:29', '2026-08-20 20:15:29');
INSERT INTO `pms_product` VALUES (7, 3, 4, '商品一致性测试', NULL, 'CONSISTENCY-TEST-1787228130505', 199.00, 300.00, 10, 0, '部', NULL, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-08-20 20:15:30', '2026-08-20 20:15:30');
INSERT INTO `pms_product` VALUES (8, 3, 4, '商品一致性测试', NULL, 'CONSISTENCY-TEST-1787228215405', 199.00, 300.00, 10, 0, '部', NULL, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-08-20 20:16:55', '2026-08-20 20:16:55');
INSERT INTO `pms_product` VALUES (9, 3, 4, '商品一致性测试', NULL, 'CONSISTENCY-TEST-1787228289948', 199.00, 300.00, 10, 0, '部', NULL, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-08-20 20:18:09', '2026-08-20 20:18:10');
INSERT INTO `pms_product` VALUES (10, 3, 4, '商品一致性测试', NULL, 'CONSISTENCY-TEST-1787228321574', 199.00, 300.00, 10, 0, '部', NULL, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-08-20 20:18:41', '2026-08-20 20:18:41');
INSERT INTO `pms_product` VALUES (11, 3, 4, '商品一致性测试', NULL, 'CONSISTENCY-TEST-1787228339403', 199.00, 300.00, 10, 0, '部', NULL, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-08-20 20:18:59', '2026-08-20 20:18:59');
INSERT INTO `pms_product` VALUES (12, 3, 3, '无SKU上架测试商品', '用于测试无SKU商品不能上架', 'NO-SKU-TEST-1787399520921', 1999.00, 2299.00, 0, 0, '部', NULL, 0, 0, 0, 0, 0, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-08-22 19:52:00', '2026-08-22 19:52:01');

-- ----------------------------
-- Table structure for pms_product_attribute
-- ----------------------------
DROP TABLE IF EXISTS `pms_product_attribute`;
CREATE TABLE `pms_product_attribute`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_attribute_category_id` bigint NOT NULL COMMENT '属性分类ID',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '属性名称',
  `select_type` tinyint NOT NULL DEFAULT 0 COMMENT '选择类型：0唯一，1单选，2多选',
  `input_type` tinyint NOT NULL DEFAULT 0 COMMENT '录入方式：0手工录入，1从列表选择',
  `input_list` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '可选值，使用英文逗号分隔',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序值',
  `filter_type` tinyint NOT NULL DEFAULT 0 COMMENT '筛选类型：0普通，1颜色',
  `search_type` tinyint NOT NULL DEFAULT 0 COMMENT '搜索类型：0不搜索，1关键字，2范围',
  `related_status` tinyint NOT NULL DEFAULT 0 COMMENT '是否关联：0否，1是',
  `hand_add_status` tinyint NOT NULL DEFAULT 0 COMMENT '是否允许手工新增：0否，1是',
  `type` tinyint NOT NULL COMMENT '属性类型：0规格，1参数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_attribute_category_name_type`(`product_attribute_category_id` ASC, `name` ASC, `type` ASC) USING BTREE,
  INDEX `idx_attribute_category_id`(`product_attribute_category_id` ASC) USING BTREE,
  CONSTRAINT `fk_attribute_category` FOREIGN KEY (`product_attribute_category_id`) REFERENCES `pms_product_attribute_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品属性表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_product_attribute
-- ----------------------------
INSERT INTO `pms_product_attribute` VALUES (2, 1, '处理器型号', 0, 0, NULL, 90, 0, 0, 0, 1, 1);
INSERT INTO `pms_product_attribute` VALUES (3, 1, '颜色', 2, 1, '黑色,白色,蓝色', 100, 1, 1, 0, 0, 0);

-- ----------------------------
-- Table structure for pms_product_attribute_category
-- ----------------------------
DROP TABLE IF EXISTS `pms_product_attribute_category`;
CREATE TABLE `pms_product_attribute_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '属性分类名称',
  `attribute_count` int NOT NULL DEFAULT 0 COMMENT '规格数量',
  `param_count` int NOT NULL DEFAULT 0 COMMENT '参数数量',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_attribute_category_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品属性分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_product_attribute_category
-- ----------------------------
INSERT INTO `pms_product_attribute_category` VALUES (1, '手机属性', 1, 1);
INSERT INTO `pms_product_attribute_category` VALUES (2, '颜色', 0, 0);

-- ----------------------------
-- Table structure for pms_product_attribute_value
-- ----------------------------
DROP TABLE IF EXISTS `pms_product_attribute_value`;
CREATE TABLE `pms_product_attribute_value`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_attribute_id` bigint NOT NULL COMMENT '商品属性ID',
  `value` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '属性值；多规格值可用逗号分隔',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_product_attribute`(`product_id` ASC, `product_attribute_id` ASC) USING BTREE,
  INDEX `idx_attribute_id`(`product_attribute_id` ASC) USING BTREE,
  CONSTRAINT `fk_pav_attribute` FOREIGN KEY (`product_attribute_id`) REFERENCES `pms_product_attribute` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_pav_product` FOREIGN KEY (`product_id`) REFERENCES `pms_product` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品属性值表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_product_attribute_value
-- ----------------------------
INSERT INTO `pms_product_attribute_value` VALUES (4, 1, 2, '麒麟9000S', '2026-08-20 16:14:14', '2026-08-20 16:14:14');
INSERT INTO `pms_product_attribute_value` VALUES (5, 1, 3, '黑色,白色', '2026-08-20 16:14:14', '2026-08-20 16:14:14');

-- ----------------------------
-- Table structure for pms_product_category
-- ----------------------------
DROP TABLE IF EXISTS `pms_product_category`;
CREATE TABLE `pms_product_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `level` int NOT NULL COMMENT '0一级，1二级',
  `product_count` int NOT NULL DEFAULT 0,
  `product_unit` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `nav_status` tinyint NOT NULL DEFAULT 0,
  `show_status` tinyint NOT NULL DEFAULT 1,
  `sort` int NOT NULL DEFAULT 0,
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `keywords` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_parent_name`(`parent_id` ASC, `name` ASC) USING BTREE,
  INDEX `idx_category_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_product_category
-- ----------------------------
INSERT INTO `pms_product_category` VALUES (1, 0, '手机数码', 0, 0, '件', 1, 1, 100, NULL, NULL, NULL, '2026-08-18 21:22:40', '2026-08-18 21:22:40');
INSERT INTO `pms_product_category` VALUES (2, 0, '家用电器', 0, 0, '件', 1, 1, 90, NULL, NULL, NULL, '2026-08-18 21:22:40', '2026-08-18 21:22:40');
INSERT INTO `pms_product_category` VALUES (3, 1, '手机通讯', 1, 0, '部', 1, 1, 100, NULL, NULL, NULL, '2026-08-18 21:22:40', '2026-08-18 21:22:40');
INSERT INTO `pms_product_category` VALUES (4, 1, '手机配件', 1, 0, '件', 1, 1, 90, NULL, NULL, NULL, '2026-08-18 21:22:40', '2026-08-18 21:22:40');
INSERT INTO `pms_product_category` VALUES (5, 0, '游戏笔记本', 0, 0, '件', 1, 1, 200, NULL, '电脑,办公', '电脑及办公设备', '2026-08-18 21:31:40', '2026-08-18 21:31:40');
INSERT INTO `pms_product_category` VALUES (6, 5, '笔记本电脑', 1, 0, '台', 1, 1, 100, NULL, NULL, NULL, '2026-08-18 21:32:10', '2026-08-18 21:32:10');

-- ----------------------------
-- Table structure for pms_product_category_attribute_relation
-- ----------------------------
DROP TABLE IF EXISTS `pms_product_category_attribute_relation`;
CREATE TABLE `pms_product_category_attribute_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_category_id` bigint NOT NULL COMMENT '商品分类ID',
  `product_attribute_id` bigint NOT NULL COMMENT '商品属性ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_category_attribute`(`product_category_id` ASC, `product_attribute_id` ASC) USING BTREE,
  INDEX `idx_relation_category_id`(`product_category_id` ASC) USING BTREE,
  INDEX `idx_relation_attribute_id`(`product_attribute_id` ASC) USING BTREE,
  CONSTRAINT `fk_relation_product_attribute` FOREIGN KEY (`product_attribute_id`) REFERENCES `pms_product_attribute` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_relation_product_category` FOREIGN KEY (`product_category_id`) REFERENCES `pms_product_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类与商品属性关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_product_category_attribute_relation
-- ----------------------------
INSERT INTO `pms_product_category_attribute_relation` VALUES (2, 3, 2);
INSERT INTO `pms_product_category_attribute_relation` VALUES (1, 3, 3);

-- ----------------------------
-- Table structure for pms_sku_stock
-- ----------------------------
DROP TABLE IF EXISTS `pms_sku_stock`;
CREATE TABLE `pms_sku_stock`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'SKU ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SKU编码',
  `price` decimal(10, 2) NOT NULL COMMENT '销售价格',
  `stock` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '总库存',
  `locked_stock` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '锁定库存',
  `low_stock` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '库存预警值',
  `pic` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SKU图片',
  `spec_key` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标准化规格组合，用于判重',
  `spec_data` json NOT NULL COMMENT '规格组合JSON',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sku_code`(`sku_code` ASC) USING BTREE,
  UNIQUE INDEX `uk_product_spec`(`product_id` ASC, `spec_key` ASC) USING BTREE,
  INDEX `idx_product_id`(`product_id` ASC) USING BTREE,
  CONSTRAINT `fk_sku_product` FOREIGN KEY (`product_id`) REFERENCES `pms_product` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_sku_locked_stock` CHECK (`locked_stock` <= `stock`),
  CONSTRAINT `chk_sku_price` CHECK (`price` >= 0)
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品SKU库存表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of pms_sku_stock
-- ----------------------------
INSERT INTO `pms_sku_stock` VALUES (1, 1, 'HUAWEI-1-BLACK', 4999.00, 9, 0, 2, NULL, '3=黑色', '[{\"name\": \"颜色\", \"value\": \"黑色\", \"attributeId\": 3}]', '2026-08-20 16:14:14', '2026-08-23 19:22:21');
INSERT INTO `pms_sku_stock` VALUES (2, 1, 'HUAWEI-1-WHITE', 5099.00, 8, 0, 2, NULL, '3=白色', '[{\"name\": \"颜色\", \"value\": \"白色\", \"attributeId\": 3}]', '2026-08-20 16:14:14', '2026-08-20 16:14:14');

-- ----------------------------
-- Table structure for ums_admin
-- ----------------------------
DROP TABLE IF EXISTS `ums_admin`;
CREATE TABLE `ums_admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0-禁用，1-启用',
  `login_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_admin_email`(`email` ASC) USING BTREE,
  INDEX `idx_admin_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '后台管理员' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_admin
-- ----------------------------
INSERT INTO `ums_admin` VALUES (1, 'admin', '$2a$10$5UZqFIfzvkd/afNJbzeVKe1.dbb3bMvcBUlivlEY1B.dxoqwj2NwK', '超级管理员', NULL, NULL, NULL, 1, '2026-08-23 19:22:31', '2026-08-18 19:38:03', '2026-08-23 19:22:30');

-- ----------------------------
-- Table structure for ums_admin_role_relation
-- ----------------------------
DROP TABLE IF EXISTS `ums_admin_role_relation`;
CREATE TABLE `ums_admin_role_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_role`(`admin_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_admin_role_role_id`(`role_id` ASC) USING BTREE,
  CONSTRAINT `fk_admin_role_admin` FOREIGN KEY (`admin_id`) REFERENCES `ums_admin` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_admin_role_role` FOREIGN KEY (`role_id`) REFERENCES `ums_role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员角色关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_admin_role_relation
-- ----------------------------
INSERT INTO `ums_admin_role_relation` VALUES (1, 1, 1, '2026-08-18 19:38:03');

-- ----------------------------
-- Table structure for ums_member
-- ----------------------------
DROP TABLE IF EXISTS `ums_member`;
CREATE TABLE `ums_member`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `gender` tinyint NOT NULL DEFAULT 0 COMMENT '性别：0未知 1男 2女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商城会员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member
-- ----------------------------
INSERT INTO `ums_member` VALUES (2, 'test002', '$2a$10$Ic6qVCDhw0bw04jsPreiVehz05R32jL6Y1YiED6M1TZCuYkArmD1m', NULL, '13300138000', 'test002@example.com', NULL, 0, NULL, 1, '2026-08-18 16:59:22', '2026-08-18 16:59:22');
INSERT INTO `ums_member` VALUES (3, 'test001', '$2a$10$8A9wN/p3ProEAo.QpJUtXujYjvw6dgRdKboie1181sEWcxLov9CL6', '小林', '13800138000', 'xiaoming@example.com', 'https://example.com/avatar.png', 1, '2005-06-18', 1, '2026-08-18 17:44:18', '2026-08-18 17:51:44');
INSERT INTO `ums_member` VALUES (4, 'test003', '$2a$10$HDmIbtukHT09L95F9XOyP.eJqemxO3rOtmz8gmrPJF.oVIlwU.CFO', NULL, NULL, NULL, NULL, 0, NULL, 1, '2026-08-22 20:25:15', '2026-08-22 20:25:15');
INSERT INTO `ums_member` VALUES (5, 'test004', '$2a$10$cKq8O9a./D/SolB5IYpujOwzL2wle.rffp.DBQYOxdkBtKIQIsh7W', NULL, NULL, NULL, NULL, 0, NULL, 1, '2026-08-22 20:41:04', '2026-08-22 20:41:04');

-- ----------------------------
-- Table structure for ums_member_receive_address
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_receive_address`;
CREATE TABLE `ums_member_receive_address`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `member_id` bigint NOT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone_number` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `default_status` tinyint NOT NULL DEFAULT 0,
  `post_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `province` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `region` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_receive_address
-- ----------------------------
INSERT INTO `ums_member_receive_address` VALUES (1, 3, '张三', '13800138000', 0, '100000', '北京市', '北京市', '海淀区', '中关村大街1号', '2026-08-18 18:43:55', '2026-08-18 18:45:21');
INSERT INTO `ums_member_receive_address` VALUES (3, 4, '订单测试用户', '13800000000', 0, '100000', '北京市', '北京市', '海淀区', '测试路1号', '2026-08-23 11:08:51', '2026-08-23 11:08:51');
INSERT INTO `ums_member_receive_address` VALUES (4, 4, '下单测试用户', '13800000000', 0, '100000', '北京市', '北京市', '海淀区', '下单测试路1号', '2026-08-23 11:34:13', '2026-08-23 11:34:13');
INSERT INTO `ums_member_receive_address` VALUES (5, 4, '下单测试用户', '13800000000', 0, '100000', '北京市', '北京市', '海淀区', '下单测试路1号', '2026-08-23 11:44:37', '2026-08-23 11:44:37');
INSERT INTO `ums_member_receive_address` VALUES (6, 4, '下单测试用户', '13800000000', 0, '100000', '北京市', '北京市', '海淀区', '下单测试路1号', '2026-08-23 11:58:44', '2026-08-23 11:58:44');
INSERT INTO `ums_member_receive_address` VALUES (7, 4, '下单测试用户', '13800000000', 0, '100000', '北京市', '北京市', '海淀区', '下单测试路1号', '2026-08-23 11:58:54', '2026-08-23 11:58:54');
INSERT INTO `ums_member_receive_address` VALUES (8, 4, '下单测试用户', '13800000000', 0, '100000', '北京市', '北京市', '海淀区', '下单测试路1号', '2026-08-23 19:22:16', '2026-08-23 19:22:16');

-- ----------------------------
-- Table structure for ums_resource
-- ----------------------------
DROP TABLE IF EXISTS `ums_resource`;
CREATE TABLE `ums_resource`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限标识，例如 product-category:write',
  `url_pattern` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '接口路径，例如 /api/admin/product-categories/**',
  `http_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'GET、POST、PATCH、DELETE或ALL',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0-禁用，1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_resource_code`(`code` ASC) USING BTREE,
  INDEX `idx_resource_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '后台接口资源' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_resource
-- ----------------------------
INSERT INTO `ums_resource` VALUES (1, '查看管理员', 'admin:read', '/api/admin/users/**', 'GET', NULL, 1, '2026-08-18 19:05:03', '2026-08-18 19:05:03');
INSERT INTO `ums_resource` VALUES (2, '管理管理员', 'admin:write', '/api/admin/users/**', 'ALL', NULL, 1, '2026-08-18 19:05:03', '2026-08-18 19:05:03');
INSERT INTO `ums_resource` VALUES (3, '查看角色', 'role:read', '/api/admin/roles/**', 'GET', NULL, 1, '2026-08-18 19:05:03', '2026-08-18 19:05:03');
INSERT INTO `ums_resource` VALUES (4, '管理角色', 'role:write', '/api/admin/roles/**', 'ALL', NULL, 1, '2026-08-18 19:05:03', '2026-08-18 19:05:03');
INSERT INTO `ums_resource` VALUES (5, '查看资源', 'resource:read', '/api/admin/resources/**', 'GET', NULL, 1, '2026-08-18 19:05:03', '2026-08-18 19:05:03');
INSERT INTO `ums_resource` VALUES (6, '管理资源', 'resource:write', '/api/admin/resources/**', 'ALL', NULL, 1, '2026-08-18 19:05:03', '2026-08-18 19:05:03');
INSERT INTO `ums_resource` VALUES (8, '品牌查询', 'brand:read', '/api/admin/brands', 'GET', '查询商品品牌', 1, '2026-08-18 21:00:03', '2026-08-18 21:00:03');
INSERT INTO `ums_resource` VALUES (9, '品牌编辑', 'brand:write', '/api/admin/brands', 'POST', '新增商品品牌', 1, '2026-08-18 21:05:09', '2026-08-18 21:05:09');
INSERT INTO `ums_resource` VALUES (10, '商品分类查询', 'category:read', '/api/admin/product-categories/**', 'GET', '查询商品分类', 1, '2026-08-18 21:26:41', '2026-08-18 21:26:41');
INSERT INTO `ums_resource` VALUES (11, '商品分类编辑', 'category:write', '/api/admin/product-categories', 'POST', '新增和编辑商品分类', 1, '2026-08-18 21:30:31', '2026-08-18 21:30:31');
INSERT INTO `ums_resource` VALUES (12, '商品属性查看', 'attribute:read', '/api/admin/product-attribute-categories/**', 'GET', '查看商品属性分类', 1, '2026-08-19 19:52:34', '2026-08-19 19:52:34');
INSERT INTO `ums_resource` VALUES (13, '商品属性编辑', 'attribute:write', '/api/admin/product-attribute-categories/**', 'ALL', '新增、修改和删除商品属性分类', 1, '2026-08-19 19:52:34', '2026-08-19 19:52:34');
INSERT INTO `ums_resource` VALUES (14, '商品查看', 'product:read', '/api/admin/products/**', 'GET', '查看商品列表和详情', 1, '2026-08-19 20:44:46', '2026-08-19 20:44:46');
INSERT INTO `ums_resource` VALUES (15, '商品编辑', 'product:write', '/api/admin/products/**', 'ALL', '新增、修改和删除商品', 1, '2026-08-19 20:44:46', '2026-08-19 20:44:46');
INSERT INTO `ums_resource` VALUES (16, '订单查询', 'order:read', '/api/admin/orders/**', 'GET', '后台订单分页和详情查询', 1, '2026-08-23 13:44:37', '2026-08-23 13:48:45');
INSERT INTO `ums_resource` VALUES (17, '订单操作', 'order:write', '/api/admin/orders/**', 'PATCH', '后台订单发货操作', 1, '2026-08-23 13:44:37', '2026-08-23 13:48:45');

-- ----------------------------
-- Table structure for ums_role
-- ----------------------------
DROP TABLE IF EXISTS `ums_role`;
CREATE TABLE `ums_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '0-禁用，1-启用',
  `sort` int NOT NULL DEFAULT 0,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`code` ASC) USING BTREE,
  INDEX `idx_role_status_sort`(`status` ASC, `sort` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '后台角色' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_role
-- ----------------------------
INSERT INTO `ums_role` VALUES (1, '超级管理员', 'SUPER_ADMIN', '拥有全部后台权限', 1, 0, '2026-08-18 19:05:03', '2026-08-18 19:05:03');
INSERT INTO `ums_role` VALUES (2, '后台只读管理员', 'READ_ONLY', '只能查看后台数据', 1, 20, '2026-08-18 19:55:57', '2026-08-18 19:56:15');

-- ----------------------------
-- Table structure for ums_role_resource_relation
-- ----------------------------
DROP TABLE IF EXISTS `ums_role_resource_relation`;
CREATE TABLE `ums_role_resource_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `resource_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_resource`(`role_id` ASC, `resource_id` ASC) USING BTREE,
  INDEX `idx_role_resource_resource_id`(`resource_id` ASC) USING BTREE,
  CONSTRAINT `fk_role_resource_resource` FOREIGN KEY (`resource_id`) REFERENCES `ums_resource` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_role_resource_role` FOREIGN KEY (`role_id`) REFERENCES `ums_role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色资源关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_role_resource_relation
-- ----------------------------
INSERT INTO `ums_role_resource_relation` VALUES (1, 1, 1, '2026-08-18 19:05:03');
INSERT INTO `ums_role_resource_relation` VALUES (2, 1, 2, '2026-08-18 19:05:03');
INSERT INTO `ums_role_resource_relation` VALUES (3, 1, 3, '2026-08-18 19:05:03');
INSERT INTO `ums_role_resource_relation` VALUES (4, 1, 4, '2026-08-18 19:05:03');
INSERT INTO `ums_role_resource_relation` VALUES (5, 1, 5, '2026-08-18 19:05:03');
INSERT INTO `ums_role_resource_relation` VALUES (6, 1, 6, '2026-08-18 19:05:03');
INSERT INTO `ums_role_resource_relation` VALUES (8, 1, 8, '2026-08-18 21:00:03');
INSERT INTO `ums_role_resource_relation` VALUES (9, 1, 9, '2026-08-18 21:07:11');
INSERT INTO `ums_role_resource_relation` VALUES (10, 1, 10, '2026-08-18 21:26:41');
INSERT INTO `ums_role_resource_relation` VALUES (11, 1, 11, '2026-08-18 21:30:31');
INSERT INTO `ums_role_resource_relation` VALUES (12, 1, 12, '2026-08-19 19:52:34');
INSERT INTO `ums_role_resource_relation` VALUES (13, 1, 13, '2026-08-19 19:52:34');
INSERT INTO `ums_role_resource_relation` VALUES (15, 1, 14, '2026-08-19 20:44:46');
INSERT INTO `ums_role_resource_relation` VALUES (16, 1, 15, '2026-08-19 20:44:46');
INSERT INTO `ums_role_resource_relation` VALUES (17, 1, 16, '2026-08-23 13:48:45');
INSERT INTO `ums_role_resource_relation` VALUES (18, 1, 17, '2026-08-23 13:48:45');

SET FOREIGN_KEY_CHECKS = 1;
