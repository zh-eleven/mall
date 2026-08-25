-- 商城演示数据（MySQL 8）
-- 参考 macrozheng/mall 的数据覆盖范围，按本项目 schema.sql 重新设计。
-- 内容：18 个品牌、5 个一级分类、15 个二级分类、6 个属性分类、
--       12 个属性、40 个商品、80 个 SKU、5 个会员、购物车及多状态订单。
-- 特性：可重复执行；不删除、不清空、不覆盖非 DEMO 数据；不依赖固定自增 ID。
-- 前置：先执行 schema.sql，再执行 rbac-data.sql（RBAC 与本脚本互不依赖）。

SET NAMES utf8mb4 COLLATE utf8mb4_0900_ai_ci;
SET collation_connection = 'utf8mb4_0900_ai_ci';
USE mall;
START TRANSACTION;

-- 1. 品牌
INSERT IGNORE INTO pms_brand
(name, first_letter, sort, factory_status, show_status, logo, big_pic, brand_story)
VALUES
('华为', 'H', 1000, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '华为演示品牌数据，仅用于本地开发与接口展示。'),
('小米', 'M', 990, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '小米演示品牌数据，仅用于本地开发与接口展示。'),
('苹果', 'A', 980, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '苹果演示品牌数据，仅用于本地开发与接口展示。'),
('三星', 'S', 970, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '三星演示品牌数据，仅用于本地开发与接口展示。'),
('OPPO', 'O', 960, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', 'OPPO演示品牌数据，仅用于本地开发与接口展示。'),
('vivo', 'V', 950, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', 'vivo演示品牌数据，仅用于本地开发与接口展示。'),
('联想', 'L', 940, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '联想演示品牌数据，仅用于本地开发与接口展示。'),
('戴尔', 'D', 930, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '戴尔演示品牌数据，仅用于本地开发与接口展示。'),
('美的', 'M', 920, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '美的演示品牌数据，仅用于本地开发与接口展示。'),
('海尔', 'H', 910, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '海尔演示品牌数据，仅用于本地开发与接口展示。'),
('耐克', 'N', 900, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '耐克演示品牌数据，仅用于本地开发与接口展示。'),
('阿迪达斯', 'A', 890, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '阿迪达斯演示品牌数据，仅用于本地开发与接口展示。'),
('伊利', 'Y', 880, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '伊利演示品牌数据，仅用于本地开发与接口展示。'),
('良品铺子', 'L', 870, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '良品铺子演示品牌数据，仅用于本地开发与接口展示。'),
('宜家', 'I', 860, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '宜家演示品牌数据，仅用于本地开发与接口展示。'),
('金龙鱼', 'J', 850, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '金龙鱼演示品牌数据，仅用于本地开发与接口展示。'),
('鲜丰水果', 'X', 840, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '鲜丰水果演示品牌数据，仅用于本地开发与接口展示。'),
('蓝月亮', 'L', 830, 1, 1, 'https://placehold.co/240x120/png?text=BRAND', 'https://placehold.co/1200x400/png?text=MALL', '蓝月亮演示品牌数据，仅用于本地开发与接口展示。');

-- 2. 商品分类
INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
VALUES
(0, '手机数码', 0, '件', 1, 1, 100, '手机数码', '数码产品与智能设备'),
(0, '家用电器', 0, '件', 1, 1, 90, '家用电器', '电视、空调与厨房电器'),
(0, '服饰鞋包', 0, '件', 1, 1, 80, '服饰鞋包', '服装与运动鞋'),
(0, '食品生鲜', 0, '件', 1, 1, 70, '食品生鲜', '零食、饮料与生鲜食品'),
(0, '家居生活', 0, '件', 1, 1, 60, '家居生活', '清洁、床品与家具');

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '手机通讯', 1, '部', 1, 1, 100, '手机通讯', '手机通讯演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '手机数码'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '手机配件', 1, '件', 1, 1, 90, '手机配件', '手机配件演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '手机数码'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '电脑整机', 1, '台', 1, 1, 80, '电脑整机', '电脑整机演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '手机数码'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '电视', 1, '台', 1, 1, 100, '电视', '电视演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '家用电器'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '空调', 1, '台', 1, 1, 90, '空调', '空调演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '家用电器'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '厨房电器', 1, '台', 1, 1, 80, '厨房电器', '厨房电器演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '家用电器'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '男装', 1, '件', 1, 1, 100, '男装', '男装演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '服饰鞋包'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '女装', 1, '件', 1, 1, 90, '女装', '女装演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '服饰鞋包'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '运动鞋', 1, '双', 1, 1, 80, '运动鞋', '运动鞋演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '服饰鞋包'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '休闲零食', 1, '份', 1, 1, 100, '休闲零食', '休闲零食演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '食品生鲜'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '乳品饮料', 1, '箱', 1, 1, 90, '乳品饮料', '乳品饮料演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '食品生鲜'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '粮油生鲜', 1, '份', 1, 1, 80, '粮油生鲜', '粮油生鲜演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '食品生鲜'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '清洁用品', 1, '件', 1, 1, 100, '清洁用品', '清洁用品演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '家居生活'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '床上用品', 1, '套', 1, 1, 90, '床上用品', '床上用品演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '家居生活'
LIMIT 1;

INSERT IGNORE INTO pms_product_category
(parent_id, name, level, product_unit, nav_status, show_status, sort, keywords, description)
SELECT parent_data.id, '家具', 1, '件', 1, 1, 80, '家具', '家具演示分类'
FROM pms_product_category parent_data
WHERE parent_data.parent_id = 0 AND parent_data.name = '家居生活'
LIMIT 1;

-- 3. 属性分类与属性
INSERT IGNORE INTO pms_product_attribute_category (name, attribute_count, param_count)
VALUES
('手机数码属性', 0, 0),
('电脑属性', 0, 0),
('家电属性', 0, 0),
('服饰属性', 0, 0),
('食品属性', 0, 0),
('家居属性', 0, 0);

INSERT IGNORE INTO pms_product_attribute
(product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
SELECT ac.id, '颜色', 2, 1, '黑色,白色,蓝色,金色', 100, 1, 1, 0, 0, 0
FROM pms_product_attribute_category ac
WHERE ac.name = '手机数码属性'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute
(product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
SELECT ac.id, '核心配置', 0, 0, NULL, 90, 0, 0, 0, 1, 1
FROM pms_product_attribute_category ac
WHERE ac.name = '手机数码属性'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute
(product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
SELECT ac.id, '颜色', 2, 1, '深空灰,银色,黑色', 100, 1, 1, 0, 0, 0
FROM pms_product_attribute_category ac
WHERE ac.name = '电脑属性'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute
(product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
SELECT ac.id, '处理器', 0, 0, NULL, 90, 0, 0, 0, 1, 1
FROM pms_product_attribute_category ac
WHERE ac.name = '电脑属性'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute
(product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
SELECT ac.id, '颜色', 2, 1, '白色,银色,黑色', 100, 1, 1, 0, 0, 0
FROM pms_product_attribute_category ac
WHERE ac.name = '家电属性'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute
(product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
SELECT ac.id, '能效等级', 0, 0, NULL, 90, 0, 0, 0, 1, 1
FROM pms_product_attribute_category ac
WHERE ac.name = '家电属性'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute
(product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
SELECT ac.id, '颜色', 2, 1, '黑色,白色,灰色,蓝色', 100, 1, 1, 0, 0, 0
FROM pms_product_attribute_category ac
WHERE ac.name = '服饰属性'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute
(product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
SELECT ac.id, '材质', 0, 0, NULL, 90, 0, 0, 0, 1, 1
FROM pms_product_attribute_category ac
WHERE ac.name = '服饰属性'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute
(product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
SELECT ac.id, '包装规格', 2, 1, '单份,组合装,整箱', 100, 1, 1, 0, 0, 0
FROM pms_product_attribute_category ac
WHERE ac.name = '食品属性'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute
(product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
SELECT ac.id, '保质期', 0, 0, NULL, 90, 0, 0, 0, 1, 1
FROM pms_product_attribute_category ac
WHERE ac.name = '食品属性'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute
(product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
SELECT ac.id, '颜色', 2, 1, '原木色,白色,灰色', 100, 1, 1, 0, 0, 0
FROM pms_product_attribute_category ac
WHERE ac.name = '家居属性'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute
(product_attribute_category_id, name, select_type, input_type, input_list, sort, filter_type, search_type, related_status, hand_add_status, type)
SELECT ac.id, '材质', 0, 0, NULL, 90, 0, 0, 0, 1, 1
FROM pms_product_attribute_category ac
WHERE ac.name = '家居属性'
LIMIT 1;

-- 4. 二级分类与属性关联
INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '手机通讯' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '手机配件' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '电脑整机' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '电视' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '空调' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '厨房电器' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '男装' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '女装' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '运动鞋' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '休闲零食' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '乳品饮料' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '粮油生鲜' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '清洁用品' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '床上用品' AND category_data.level = 1;

INSERT IGNORE INTO pms_product_category_attribute_relation (product_category_id, product_attribute_id)
SELECT category_data.id, attribute_data.id
FROM pms_product_category category_data
JOIN pms_product_category parent_data ON parent_data.id = category_data.parent_id
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE category_data.name = '家具' AND category_data.level = 1;

-- 5. 商品、商品属性值与 SKU
INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '华为旗舰智能手机', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-PHONE-001', 5999.00, 6499.00, 100, 10, '部', 1.00,
       1, 1, 1, 1, 1000,
       'https://placehold.co/800x800/png?text=DEMO-PHONE-001',
       'https://placehold.co/800x800/png?text=DEMO-PHONE-001-1,https://placehold.co/800x800/png?text=DEMO-PHONE-001-2',
       '华为旗舰智能手机的演示描述。', '华为旗舰智能手机', '用于本地开发和接口联调的完整商品数据。',
       '<p>华为旗舰智能手机</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '手机通讯' AND category_data.level = 1
WHERE brand_data.name = '华为'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '曜石黑,羽砂白'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '旗舰级芯片'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-001' AND attribute_data.name = '核心配置' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-001-A', 5999.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-001-A',
       CONCAT(attribute_data.id, '=', '曜石黑'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '曜石黑'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-001-B', 6199.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-001-B',
       CONCAT(attribute_data.id, '=', '羽砂白'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '羽砂白'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '小米高性能智能手机', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-PHONE-002', 3999.00, 4499.00, 100, 10, '部', 1.00,
       1, 1, 0, 1, 999,
       'https://placehold.co/800x800/png?text=DEMO-PHONE-002',
       'https://placehold.co/800x800/png?text=DEMO-PHONE-002-1,https://placehold.co/800x800/png?text=DEMO-PHONE-002-2',
       '小米高性能智能手机的演示描述。', '小米高性能智能手机', '用于本地开发和接口联调的完整商品数据。',
       '<p>小米高性能智能手机</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '手机通讯' AND category_data.level = 1
WHERE brand_data.name = '小米'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,白色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '高性能移动平台'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-002' AND attribute_data.name = '核心配置' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-002-A', 3999.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-002-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-002-B', 4199.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-002-B',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '苹果智能手机', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-PHONE-003', 6999.00, 7999.00, 100, 10, '部', 1.00,
       1, 1, 0, 1, 998,
       'https://placehold.co/800x800/png?text=DEMO-PHONE-003',
       'https://placehold.co/800x800/png?text=DEMO-PHONE-003-1,https://placehold.co/800x800/png?text=DEMO-PHONE-003-2',
       '苹果智能手机的演示描述。', '苹果智能手机', '用于本地开发和接口联调的完整商品数据。',
       '<p>苹果智能手机</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '手机通讯' AND category_data.level = 1
WHERE brand_data.name = '苹果'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,白色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, 'A系列处理器'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-003' AND attribute_data.name = '核心配置' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-003-A', 6999.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-003-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-003-B', 7199.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-003-B',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '三星影像旗舰手机', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-PHONE-004', 6499.00, 7299.00, 100, 10, '部', 1.00,
       1, 1, 1, 1, 997,
       'https://placehold.co/800x800/png?text=DEMO-PHONE-004',
       'https://placehold.co/800x800/png?text=DEMO-PHONE-004-1,https://placehold.co/800x800/png?text=DEMO-PHONE-004-2',
       '三星影像旗舰手机的演示描述。', '三星影像旗舰手机', '用于本地开发和接口联调的完整商品数据。',
       '<p>三星影像旗舰手机</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '手机通讯' AND category_data.level = 1
WHERE brand_data.name = '三星'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '玄夜黑,冰川蓝'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-004' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '旗舰级移动平台'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-004' AND attribute_data.name = '核心配置' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-004-A', 6499.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-004-A',
       CONCAT(attribute_data.id, '=', '玄夜黑'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '玄夜黑'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-004' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-004-B', 6699.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-004-B',
       CONCAT(attribute_data.id, '=', '冰川蓝'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '冰川蓝'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-004' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, 'OPPO影像手机', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-PHONE-005', 4999.00, 5499.00, 100, 10, '部', 1.00,
       1, 1, 0, 1, 996,
       'https://placehold.co/800x800/png?text=DEMO-PHONE-005',
       'https://placehold.co/800x800/png?text=DEMO-PHONE-005-1,https://placehold.co/800x800/png?text=DEMO-PHONE-005-2',
       'OPPO影像手机的演示描述。', 'OPPO影像手机', '用于本地开发和接口联调的完整商品数据。',
       '<p>OPPO影像手机</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '手机通讯' AND category_data.level = 1
WHERE brand_data.name = 'OPPO'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '星空黑,云朵白'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-005' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '高性能移动平台'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-005' AND attribute_data.name = '核心配置' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-005-A', 4999.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-005-A',
       CONCAT(attribute_data.id, '=', '星空黑'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '星空黑'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-005' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-005-B', 5199.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-005-B',
       CONCAT(attribute_data.id, '=', '云朵白'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '云朵白'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-005' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, 'vivo轻薄智能手机', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-PHONE-006', 4299.00, 4799.00, 100, 10, '部', 1.00,
       1, 1, 0, 1, 995,
       'https://placehold.co/800x800/png?text=DEMO-PHONE-006',
       'https://placehold.co/800x800/png?text=DEMO-PHONE-006-1,https://placehold.co/800x800/png?text=DEMO-PHONE-006-2',
       'vivo轻薄智能手机的演示描述。', 'vivo轻薄智能手机', '用于本地开发和接口联调的完整商品数据。',
       '<p>vivo轻薄智能手机</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '手机通讯' AND category_data.level = 1
WHERE brand_data.name = 'vivo'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '深空黑,晨曦白'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-006' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '高性能移动平台'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-006' AND attribute_data.name = '核心配置' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-006-A', 4299.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-006-A',
       CONCAT(attribute_data.id, '=', '深空黑'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '深空黑'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-006' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-006-B', 4499.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-006-B',
       CONCAT(attribute_data.id, '=', '晨曦白'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '晨曦白'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PHONE-006' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '小米大容量移动电源', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-ACC-001', 199.00, 229.00, 100, 10, '件', 1.00,
       1, 1, 1, 1, 994,
       'https://placehold.co/800x800/png?text=DEMO-ACC-001',
       'https://placehold.co/800x800/png?text=DEMO-ACC-001-1,https://placehold.co/800x800/png?text=DEMO-ACC-001-2',
       '小米大容量移动电源的演示描述。', '小米大容量移动电源', '用于本地开发和接口联调的完整商品数据。',
       '<p>小米大容量移动电源</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '手机配件' AND category_data.level = 1
WHERE brand_data.name = '小米'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,白色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-ACC-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '20000mAh'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-ACC-001' AND attribute_data.name = '核心配置' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-007-A', 199.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-007-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-ACC-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-007-B', 219.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-007-B',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-ACC-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '华为无线蓝牙耳机', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-ACC-002', 699.00, 799.00, 100, 10, '件', 1.00,
       1, 1, 0, 1, 993,
       'https://placehold.co/800x800/png?text=DEMO-ACC-002',
       'https://placehold.co/800x800/png?text=DEMO-ACC-002-1,https://placehold.co/800x800/png?text=DEMO-ACC-002-2',
       '华为无线蓝牙耳机的演示描述。', '华为无线蓝牙耳机', '用于本地开发和接口联调的完整商品数据。',
       '<p>华为无线蓝牙耳机</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '手机配件' AND category_data.level = 1
WHERE brand_data.name = '华为'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '曜石黑,陶瓷白'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-ACC-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '主动降噪'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-ACC-002' AND attribute_data.name = '核心配置' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-008-A', 699.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-008-A',
       CONCAT(attribute_data.id, '=', '曜石黑'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '曜石黑'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-ACC-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-008-B', 719.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-008-B',
       CONCAT(attribute_data.id, '=', '陶瓷白'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '陶瓷白'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-ACC-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '苹果无线耳机', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-ACC-003', 1299.00, 1499.00, 100, 10, '件', 1.00,
       1, 1, 0, 1, 992,
       'https://placehold.co/800x800/png?text=DEMO-ACC-003',
       'https://placehold.co/800x800/png?text=DEMO-ACC-003-1,https://placehold.co/800x800/png?text=DEMO-ACC-003-2',
       '苹果无线耳机的演示描述。', '苹果无线耳机', '用于本地开发和接口联调的完整商品数据。',
       '<p>苹果无线耳机</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '手机配件' AND category_data.level = 1
WHERE brand_data.name = '苹果'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '白色,黑色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-ACC-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '空间音频'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-ACC-003' AND attribute_data.name = '核心配置' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-009-A', 1299.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-009-A',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-ACC-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-009-B', 1499.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-009-B',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '手机数码属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-ACC-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '联想游戏笔记本', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-PC-001', 7999.00, 8999.00, 100, 10, '台', 1.00,
       1, 1, 1, 1, 991,
       'https://placehold.co/800x800/png?text=DEMO-PC-001',
       'https://placehold.co/800x800/png?text=DEMO-PC-001-1,https://placehold.co/800x800/png?text=DEMO-PC-001-2',
       '联想游戏笔记本的演示描述。', '联想游戏笔记本', '用于本地开发和接口联调的完整商品数据。',
       '<p>联想游戏笔记本</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '电脑整机' AND category_data.level = 1
WHERE brand_data.name = '联想'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '深空灰,黑色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '高性能移动处理器'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-001' AND attribute_data.name = '处理器' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-010-A', 7999.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-010-A',
       CONCAT(attribute_data.id, '=', '深空灰'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '深空灰'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-010-B', 8199.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-010-B',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '戴尔轻薄笔记本', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-PC-002', 6999.00, 7599.00, 100, 10, '台', 1.00,
       1, 1, 0, 1, 990,
       'https://placehold.co/800x800/png?text=DEMO-PC-002',
       'https://placehold.co/800x800/png?text=DEMO-PC-002-1,https://placehold.co/800x800/png?text=DEMO-PC-002-2',
       '戴尔轻薄笔记本的演示描述。', '戴尔轻薄笔记本', '用于本地开发和接口联调的完整商品数据。',
       '<p>戴尔轻薄笔记本</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '电脑整机' AND category_data.level = 1
WHERE brand_data.name = '戴尔'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '银色,深空灰'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '高性能移动处理器'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-002' AND attribute_data.name = '处理器' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-011-A', 6999.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-011-A',
       CONCAT(attribute_data.id, '=', '银色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '银色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-011-B', 7199.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-011-B',
       CONCAT(attribute_data.id, '=', '深空灰'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '深空灰'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '华为智慧办公笔记本', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-PC-003', 6499.00, 6999.00, 100, 10, '台', 1.00,
       1, 1, 0, 1, 989,
       'https://placehold.co/800x800/png?text=DEMO-PC-003',
       'https://placehold.co/800x800/png?text=DEMO-PC-003-1,https://placehold.co/800x800/png?text=DEMO-PC-003-2',
       '华为智慧办公笔记本的演示描述。', '华为智慧办公笔记本', '用于本地开发和接口联调的完整商品数据。',
       '<p>华为智慧办公笔记本</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '电脑整机' AND category_data.level = 1
WHERE brand_data.name = '华为'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '深空灰,银色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '高性能移动处理器'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-003' AND attribute_data.name = '处理器' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-012-A', 6499.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-012-A',
       CONCAT(attribute_data.id, '=', '深空灰'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '深空灰'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-012-B', 6699.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-012-B',
       CONCAT(attribute_data.id, '=', '银色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '银色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '苹果轻薄笔记本', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-PC-004', 8999.00, 9999.00, 100, 10, '台', 1.00,
       1, 0, 1, 1, 988,
       'https://placehold.co/800x800/png?text=DEMO-PC-004',
       'https://placehold.co/800x800/png?text=DEMO-PC-004-1,https://placehold.co/800x800/png?text=DEMO-PC-004-2',
       '苹果轻薄笔记本的演示描述。', '苹果轻薄笔记本', '用于本地开发和接口联调的完整商品数据。',
       '<p>苹果轻薄笔记本</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '电脑整机' AND category_data.level = 1
WHERE brand_data.name = '苹果'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '深空灰,银色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-004' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, 'M系列处理器'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-004' AND attribute_data.name = '处理器' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-013-A', 8999.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-013-A',
       CONCAT(attribute_data.id, '=', '深空灰'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '深空灰'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-004' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-013-B', 9199.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-013-B',
       CONCAT(attribute_data.id, '=', '银色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '银色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '电脑属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-PC-004' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '小米4K智能电视', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-TV-001', 2999.00, 3499.00, 100, 10, '台', 1.00,
       1, 0, 0, 1, 987,
       'https://placehold.co/800x800/png?text=DEMO-TV-001',
       'https://placehold.co/800x800/png?text=DEMO-TV-001-1,https://placehold.co/800x800/png?text=DEMO-TV-001-2',
       '小米4K智能电视的演示描述。', '小米4K智能电视', '用于本地开发和接口联调的完整商品数据。',
       '<p>小米4K智能电视</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '电视' AND category_data.level = 1
WHERE brand_data.name = '小米'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,银色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-TV-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '一级能效'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-TV-001' AND attribute_data.name = '能效等级' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-014-A', 2999.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-014-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-TV-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-014-B', 3199.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-014-B',
       CONCAT(attribute_data.id, '=', '银色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '银色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-TV-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '三星量子点电视', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-TV-002', 5999.00, 6999.00, 100, 10, '台', 1.00,
       1, 0, 0, 1, 986,
       'https://placehold.co/800x800/png?text=DEMO-TV-002',
       'https://placehold.co/800x800/png?text=DEMO-TV-002-1,https://placehold.co/800x800/png?text=DEMO-TV-002-2',
       '三星量子点电视的演示描述。', '三星量子点电视', '用于本地开发和接口联调的完整商品数据。',
       '<p>三星量子点电视</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '电视' AND category_data.level = 1
WHERE brand_data.name = '三星'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,银色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-TV-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '一级能效'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-TV-002' AND attribute_data.name = '能效等级' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-015-A', 5999.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-015-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-TV-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-015-B', 6199.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-015-B',
       CONCAT(attribute_data.id, '=', '银色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '银色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-TV-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '海尔全面屏电视', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-TV-003', 2599.00, 2999.00, 100, 10, '台', 1.00,
       1, 0, 1, 1, 985,
       'https://placehold.co/800x800/png?text=DEMO-TV-003',
       'https://placehold.co/800x800/png?text=DEMO-TV-003-1,https://placehold.co/800x800/png?text=DEMO-TV-003-2',
       '海尔全面屏电视的演示描述。', '海尔全面屏电视', '用于本地开发和接口联调的完整商品数据。',
       '<p>海尔全面屏电视</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '电视' AND category_data.level = 1
WHERE brand_data.name = '海尔'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,灰色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-TV-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '二级能效'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-TV-003' AND attribute_data.name = '能效等级' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-016-A', 2599.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-016-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-TV-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-016-B', 2799.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-016-B',
       CONCAT(attribute_data.id, '=', '灰色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '灰色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-TV-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '美的新一级能效空调', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-AC-001', 3299.00, 3699.00, 100, 10, '台', 1.00,
       1, 0, 0, 1, 984,
       'https://placehold.co/800x800/png?text=DEMO-AC-001',
       'https://placehold.co/800x800/png?text=DEMO-AC-001-1,https://placehold.co/800x800/png?text=DEMO-AC-001-2',
       '美的新一级能效空调的演示描述。', '美的新一级能效空调', '用于本地开发和接口联调的完整商品数据。',
       '<p>美的新一级能效空调</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '空调' AND category_data.level = 1
WHERE brand_data.name = '美的'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '白色,金色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-AC-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '一级能效'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-AC-001' AND attribute_data.name = '能效等级' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-017-A', 3299.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-017-A',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-AC-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-017-B', 3499.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-017-B',
       CONCAT(attribute_data.id, '=', '金色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '金色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-AC-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '海尔变频空调', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-AC-002', 3499.00, 3899.00, 100, 10, '台', 1.00,
       1, 0, 0, 1, 983,
       'https://placehold.co/800x800/png?text=DEMO-AC-002',
       'https://placehold.co/800x800/png?text=DEMO-AC-002-1,https://placehold.co/800x800/png?text=DEMO-AC-002-2',
       '海尔变频空调的演示描述。', '海尔变频空调', '用于本地开发和接口联调的完整商品数据。',
       '<p>海尔变频空调</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '空调' AND category_data.level = 1
WHERE brand_data.name = '海尔'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '白色,银色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-AC-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '一级能效'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-AC-002' AND attribute_data.name = '能效等级' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-018-A', 3499.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-018-A',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-AC-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-018-B', 3699.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-018-B',
       CONCAT(attribute_data.id, '=', '银色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '银色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-AC-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '小米智能空调', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-AC-003', 2799.00, 3199.00, 100, 10, '台', 1.00,
       1, 0, 1, 1, 982,
       'https://placehold.co/800x800/png?text=DEMO-AC-003',
       'https://placehold.co/800x800/png?text=DEMO-AC-003-1,https://placehold.co/800x800/png?text=DEMO-AC-003-2',
       '小米智能空调的演示描述。', '小米智能空调', '用于本地开发和接口联调的完整商品数据。',
       '<p>小米智能空调</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '空调' AND category_data.level = 1
WHERE brand_data.name = '小米'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '白色,银色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-AC-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '一级能效'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-AC-003' AND attribute_data.name = '能效等级' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-019-A', 2799.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-019-A',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-AC-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-019-B', 2999.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-019-B',
       CONCAT(attribute_data.id, '=', '银色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '银色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-AC-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '美的智能电饭煲', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-KITCHEN-001', 499.00, 599.00, 100, 10, '台', 1.00,
       1, 0, 0, 1, 981,
       'https://placehold.co/800x800/png?text=DEMO-KITCHEN-001',
       'https://placehold.co/800x800/png?text=DEMO-KITCHEN-001-1,https://placehold.co/800x800/png?text=DEMO-KITCHEN-001-2',
       '美的智能电饭煲的演示描述。', '美的智能电饭煲', '用于本地开发和接口联调的完整商品数据。',
       '<p>美的智能电饭煲</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '厨房电器' AND category_data.level = 1
WHERE brand_data.name = '美的'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '白色,黑色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-KITCHEN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '二级能效'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-KITCHEN-001' AND attribute_data.name = '能效等级' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-020-A', 499.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-020-A',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-KITCHEN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-020-B', 519.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-020-B',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-KITCHEN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '海尔家用电烤箱', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-KITCHEN-002', 899.00, 1099.00, 100, 10, '台', 1.00,
       1, 0, 0, 1, 980,
       'https://placehold.co/800x800/png?text=DEMO-KITCHEN-002',
       'https://placehold.co/800x800/png?text=DEMO-KITCHEN-002-1,https://placehold.co/800x800/png?text=DEMO-KITCHEN-002-2',
       '海尔家用电烤箱的演示描述。', '海尔家用电烤箱', '用于本地开发和接口联调的完整商品数据。',
       '<p>海尔家用电烤箱</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '厨房电器' AND category_data.level = 1
WHERE brand_data.name = '海尔'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,银色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-KITCHEN-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '一级能效'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-KITCHEN-002' AND attribute_data.name = '能效等级' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-021-A', 899.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-021-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-KITCHEN-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-021-B', 919.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-021-B',
       CONCAT(attribute_data.id, '=', '银色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '银色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-KITCHEN-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '小米恒温电水壶', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-KITCHEN-003', 199.00, 249.00, 100, 10, '台', 1.00,
       1, 0, 1, 1, 979,
       'https://placehold.co/800x800/png?text=DEMO-KITCHEN-003',
       'https://placehold.co/800x800/png?text=DEMO-KITCHEN-003-1,https://placehold.co/800x800/png?text=DEMO-KITCHEN-003-2',
       '小米恒温电水壶的演示描述。', '小米恒温电水壶', '用于本地开发和接口联调的完整商品数据。',
       '<p>小米恒温电水壶</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '厨房电器' AND category_data.level = 1
WHERE brand_data.name = '小米'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '白色,黑色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-KITCHEN-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '一级能效'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-KITCHEN-003' AND attribute_data.name = '能效等级' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-022-A', 199.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-022-A',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-KITCHEN-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-022-B', 219.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-022-B',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家电属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-KITCHEN-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '耐克男子运动夹克', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-MEN-001', 699.00, 799.00, 100, 10, '件', 1.00,
       1, 0, 0, 1, 978,
       'https://placehold.co/800x800/png?text=DEMO-MEN-001',
       'https://placehold.co/800x800/png?text=DEMO-MEN-001-1,https://placehold.co/800x800/png?text=DEMO-MEN-001-2',
       '耐克男子运动夹克的演示描述。', '耐克男子运动夹克', '用于本地开发和接口联调的完整商品数据。',
       '<p>耐克男子运动夹克</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '男装' AND category_data.level = 1
WHERE brand_data.name = '耐克'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,灰色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-MEN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '聚酯纤维'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-MEN-001' AND attribute_data.name = '材质' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-023-A', 699.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-023-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-MEN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-023-B', 719.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-023-B',
       CONCAT(attribute_data.id, '=', '灰色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '灰色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-MEN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '阿迪达斯男子连帽卫衣', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-MEN-002', 599.00, 699.00, 100, 10, '件', 1.00,
       1, 0, 0, 1, 977,
       'https://placehold.co/800x800/png?text=DEMO-MEN-002',
       'https://placehold.co/800x800/png?text=DEMO-MEN-002-1,https://placehold.co/800x800/png?text=DEMO-MEN-002-2',
       '阿迪达斯男子连帽卫衣的演示描述。', '阿迪达斯男子连帽卫衣', '用于本地开发和接口联调的完整商品数据。',
       '<p>阿迪达斯男子连帽卫衣</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '男装' AND category_data.level = 1
WHERE brand_data.name = '阿迪达斯'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,白色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-MEN-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '棉混纺'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-MEN-002' AND attribute_data.name = '材质' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-024-A', 599.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-024-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-MEN-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-024-B', 619.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-024-B',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-MEN-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '耐克女子运动外套', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-WOMEN-001', 659.00, 759.00, 100, 10, '件', 1.00,
       1, 0, 1, 1, 976,
       'https://placehold.co/800x800/png?text=DEMO-WOMEN-001',
       'https://placehold.co/800x800/png?text=DEMO-WOMEN-001-1,https://placehold.co/800x800/png?text=DEMO-WOMEN-001-2',
       '耐克女子运动外套的演示描述。', '耐克女子运动外套', '用于本地开发和接口联调的完整商品数据。',
       '<p>耐克女子运动外套</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '女装' AND category_data.level = 1
WHERE brand_data.name = '耐克'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,蓝色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-WOMEN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '聚酯纤维'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-WOMEN-001' AND attribute_data.name = '材质' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-025-A', 659.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-025-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-WOMEN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-025-B', 679.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-025-B',
       CONCAT(attribute_data.id, '=', '蓝色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '蓝色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-WOMEN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '阿迪达斯女子休闲卫衣', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-WOMEN-002', 559.00, 659.00, 100, 10, '件', 1.00,
       1, 0, 0, 1, 975,
       'https://placehold.co/800x800/png?text=DEMO-WOMEN-002',
       'https://placehold.co/800x800/png?text=DEMO-WOMEN-002-1,https://placehold.co/800x800/png?text=DEMO-WOMEN-002-2',
       '阿迪达斯女子休闲卫衣的演示描述。', '阿迪达斯女子休闲卫衣', '用于本地开发和接口联调的完整商品数据。',
       '<p>阿迪达斯女子休闲卫衣</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '女装' AND category_data.level = 1
WHERE brand_data.name = '阿迪达斯'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '白色,灰色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-WOMEN-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '棉混纺'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-WOMEN-002' AND attribute_data.name = '材质' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-026-A', 559.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-026-A',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-WOMEN-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-026-B', 579.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-026-B',
       CONCAT(attribute_data.id, '=', '灰色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '灰色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-WOMEN-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '耐克缓震跑鞋', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-SHOE-001', 899.00, 999.00, 100, 10, '双', 1.00,
       1, 0, 0, 1, 974,
       'https://placehold.co/800x800/png?text=DEMO-SHOE-001',
       'https://placehold.co/800x800/png?text=DEMO-SHOE-001-1,https://placehold.co/800x800/png?text=DEMO-SHOE-001-2',
       '耐克缓震跑鞋的演示描述。', '耐克缓震跑鞋', '用于本地开发和接口联调的完整商品数据。',
       '<p>耐克缓震跑鞋</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '运动鞋' AND category_data.level = 1
WHERE brand_data.name = '耐克'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,白色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SHOE-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '织物鞋面'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SHOE-001' AND attribute_data.name = '材质' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-027-A', 899.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-027-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SHOE-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-027-B', 919.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-027-B',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SHOE-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '阿迪达斯轻量跑鞋', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-SHOE-002', 799.00, 899.00, 100, 10, '双', 1.00,
       1, 0, 1, 1, 973,
       'https://placehold.co/800x800/png?text=DEMO-SHOE-002',
       'https://placehold.co/800x800/png?text=DEMO-SHOE-002-1,https://placehold.co/800x800/png?text=DEMO-SHOE-002-2',
       '阿迪达斯轻量跑鞋的演示描述。', '阿迪达斯轻量跑鞋', '用于本地开发和接口联调的完整商品数据。',
       '<p>阿迪达斯轻量跑鞋</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '运动鞋' AND category_data.level = 1
WHERE brand_data.name = '阿迪达斯'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,蓝色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SHOE-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '网布鞋面'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SHOE-002' AND attribute_data.name = '材质' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-028-A', 799.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-028-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SHOE-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-028-B', 819.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-028-B',
       CONCAT(attribute_data.id, '=', '蓝色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '蓝色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SHOE-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '耐克复古休闲鞋', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-SHOE-003', 759.00, 859.00, 100, 10, '双', 1.00,
       1, 0, 0, 1, 972,
       'https://placehold.co/800x800/png?text=DEMO-SHOE-003',
       'https://placehold.co/800x800/png?text=DEMO-SHOE-003-1,https://placehold.co/800x800/png?text=DEMO-SHOE-003-2',
       '耐克复古休闲鞋的演示描述。', '耐克复古休闲鞋', '用于本地开发和接口联调的完整商品数据。',
       '<p>耐克复古休闲鞋</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '运动鞋' AND category_data.level = 1
WHERE brand_data.name = '耐克'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '白色,灰色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SHOE-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '合成革鞋面'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SHOE-003' AND attribute_data.name = '材质' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-029-A', 759.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-029-A',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SHOE-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-029-B', 779.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-029-B',
       CONCAT(attribute_data.id, '=', '灰色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '灰色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '服饰属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SHOE-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '每日坚果组合装', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-SNACK-001', 69.90, 79.90, 100, 10, '份', 1.00,
       1, 0, 0, 1, 971,
       'https://placehold.co/800x800/png?text=DEMO-SNACK-001',
       'https://placehold.co/800x800/png?text=DEMO-SNACK-001-1,https://placehold.co/800x800/png?text=DEMO-SNACK-001-2',
       '每日坚果组合装的演示描述。', '每日坚果组合装', '用于本地开发和接口联调的完整商品数据。',
       '<p>每日坚果组合装</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '休闲零食' AND category_data.level = 1
WHERE brand_data.name = '良品铺子'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '单盒,两盒装'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SNACK-001' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '180天'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SNACK-001' AND attribute_data.name = '保质期' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-030-A', 69.90, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-030-A',
       CONCAT(attribute_data.id, '=', '单盒'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '单盒'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SNACK-001' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-030-B', 79.90, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-030-B',
       CONCAT(attribute_data.id, '=', '两盒装'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '两盒装'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SNACK-001' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '风干牛肉零食', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-SNACK-002', 49.90, 59.90, 100, 10, '份', 1.00,
       1, 0, 1, 1, 970,
       'https://placehold.co/800x800/png?text=DEMO-SNACK-002',
       'https://placehold.co/800x800/png?text=DEMO-SNACK-002-1,https://placehold.co/800x800/png?text=DEMO-SNACK-002-2',
       '风干牛肉零食的演示描述。', '风干牛肉零食', '用于本地开发和接口联调的完整商品数据。',
       '<p>风干牛肉零食</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '休闲零食' AND category_data.level = 1
WHERE brand_data.name = '良品铺子'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '单袋,三袋装'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SNACK-002' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '270天'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SNACK-002' AND attribute_data.name = '保质期' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-031-A', 49.90, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-031-A',
       CONCAT(attribute_data.id, '=', '单袋'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '单袋'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SNACK-002' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-031-B', 59.90, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-031-B',
       CONCAT(attribute_data.id, '=', '三袋装'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '三袋装'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-SNACK-002' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '纯牛奶整箱装', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-DRINK-001', 69.90, 79.90, 100, 10, '箱', 1.00,
       1, 0, 0, 1, 969,
       'https://placehold.co/800x800/png?text=DEMO-DRINK-001',
       'https://placehold.co/800x800/png?text=DEMO-DRINK-001-1,https://placehold.co/800x800/png?text=DEMO-DRINK-001-2',
       '纯牛奶整箱装的演示描述。', '纯牛奶整箱装', '用于本地开发和接口联调的完整商品数据。',
       '<p>纯牛奶整箱装</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '乳品饮料' AND category_data.level = 1
WHERE brand_data.name = '伊利'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '12盒装,24盒装'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-DRINK-001' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '180天'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-DRINK-001' AND attribute_data.name = '保质期' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-032-A', 69.90, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-032-A',
       CONCAT(attribute_data.id, '=', '12盒装'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '12盒装'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-DRINK-001' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-032-B', 79.90, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-032-B',
       CONCAT(attribute_data.id, '=', '24盒装'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '24盒装'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-DRINK-001' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '常温酸奶整箱装', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-DRINK-002', 89.90, 99.90, 100, 10, '箱', 1.00,
       1, 0, 0, 1, 968,
       'https://placehold.co/800x800/png?text=DEMO-DRINK-002',
       'https://placehold.co/800x800/png?text=DEMO-DRINK-002-1,https://placehold.co/800x800/png?text=DEMO-DRINK-002-2',
       '常温酸奶整箱装的演示描述。', '常温酸奶整箱装', '用于本地开发和接口联调的完整商品数据。',
       '<p>常温酸奶整箱装</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '乳品饮料' AND category_data.level = 1
WHERE brand_data.name = '伊利'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '12盒装,24盒装'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-DRINK-002' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '150天'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-DRINK-002' AND attribute_data.name = '保质期' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-033-A', 89.90, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-033-A',
       CONCAT(attribute_data.id, '=', '12盒装'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '12盒装'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-DRINK-002' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-033-B', 99.90, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-033-B',
       CONCAT(attribute_data.id, '=', '24盒装'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '24盒装'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-DRINK-002' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '优质东北大米', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-FRESH-001', 59.90, 69.90, 100, 10, '份', 1.00,
       1, 0, 1, 1, 967,
       'https://placehold.co/800x800/png?text=DEMO-FRESH-001',
       'https://placehold.co/800x800/png?text=DEMO-FRESH-001-1,https://placehold.co/800x800/png?text=DEMO-FRESH-001-2',
       '优质东北大米的演示描述。', '优质东北大米', '用于本地开发和接口联调的完整商品数据。',
       '<p>优质东北大米</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '粮油生鲜' AND category_data.level = 1
WHERE brand_data.name = '金龙鱼'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '5千克装,10千克装'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FRESH-001' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '365天'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FRESH-001' AND attribute_data.name = '保质期' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-034-A', 59.90, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-034-A',
       CONCAT(attribute_data.id, '=', '5千克装'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '5千克装'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FRESH-001' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-034-B', 69.90, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-034-B',
       CONCAT(attribute_data.id, '=', '10千克装'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '10千克装'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FRESH-001' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '当季水果礼盒', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-FRESH-002', 99.00, 119.00, 100, 10, '份', 1.00,
       1, 0, 0, 1, 966,
       'https://placehold.co/800x800/png?text=DEMO-FRESH-002',
       'https://placehold.co/800x800/png?text=DEMO-FRESH-002-1,https://placehold.co/800x800/png?text=DEMO-FRESH-002-2',
       '当季水果礼盒的演示描述。', '当季水果礼盒', '用于本地开发和接口联调的完整商品数据。',
       '<p>当季水果礼盒</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '粮油生鲜' AND category_data.level = 1
WHERE brand_data.name = '鲜丰水果'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '标准礼盒,家庭礼盒'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FRESH-002' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '7天'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FRESH-002' AND attribute_data.name = '保质期' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-035-A', 99.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-035-A',
       CONCAT(attribute_data.id, '=', '标准礼盒'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '标准礼盒'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FRESH-002' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-035-B', 109.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-035-B',
       CONCAT(attribute_data.id, '=', '家庭礼盒'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '家庭礼盒'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '食品属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FRESH-002' AND attribute_data.name = '包装规格' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '深层洁净洗衣液', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-CLEAN-001', 59.90, 69.90, 100, 10, '件', 1.00,
       1, 0, 0, 1, 965,
       'https://placehold.co/800x800/png?text=DEMO-CLEAN-001',
       'https://placehold.co/800x800/png?text=DEMO-CLEAN-001-1,https://placehold.co/800x800/png?text=DEMO-CLEAN-001-2',
       '深层洁净洗衣液的演示描述。', '深层洁净洗衣液', '用于本地开发和接口联调的完整商品数据。',
       '<p>深层洁净洗衣液</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '清洁用品' AND category_data.level = 1
WHERE brand_data.name = '蓝月亮'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '单瓶装,两瓶装'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-CLEAN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '塑料包装'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-CLEAN-001' AND attribute_data.name = '材质' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-036-A', 59.90, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-036-A',
       CONCAT(attribute_data.id, '=', '单瓶装'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '单瓶装'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-CLEAN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-036-B', 69.90, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-036-B',
       CONCAT(attribute_data.id, '=', '两瓶装'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '两瓶装'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-CLEAN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '纯棉四件套', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-BED-001', 399.00, 499.00, 100, 10, '套', 1.00,
       1, 0, 1, 1, 964,
       'https://placehold.co/800x800/png?text=DEMO-BED-001',
       'https://placehold.co/800x800/png?text=DEMO-BED-001-1,https://placehold.co/800x800/png?text=DEMO-BED-001-2',
       '纯棉四件套的演示描述。', '纯棉四件套', '用于本地开发和接口联调的完整商品数据。',
       '<p>纯棉四件套</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '床上用品' AND category_data.level = 1
WHERE brand_data.name = '宜家'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '浅灰色,米白色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-BED-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '纯棉'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-BED-001' AND attribute_data.name = '材质' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-037-A', 399.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-037-A',
       CONCAT(attribute_data.id, '=', '浅灰色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '浅灰色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-BED-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-037-B', 419.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-037-B',
       CONCAT(attribute_data.id, '=', '米白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '米白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-BED-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '简约实木书桌', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-FURN-001', 899.00, 1099.00, 100, 10, '件', 1.00,
       1, 0, 0, 1, 963,
       'https://placehold.co/800x800/png?text=DEMO-FURN-001',
       'https://placehold.co/800x800/png?text=DEMO-FURN-001-1,https://placehold.co/800x800/png?text=DEMO-FURN-001-2',
       '简约实木书桌的演示描述。', '简约实木书桌', '用于本地开发和接口联调的完整商品数据。',
       '<p>简约实木书桌</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '家具' AND category_data.level = 1
WHERE brand_data.name = '宜家'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '原木色,白色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FURN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '实木'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FURN-001' AND attribute_data.name = '材质' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-038-A', 899.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-038-A',
       CONCAT(attribute_data.id, '=', '原木色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '原木色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FURN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-038-B', 919.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-038-B',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FURN-001' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '人体工学办公椅', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-FURN-002', 699.00, 799.00, 100, 10, '件', 1.00,
       1, 0, 0, 1, 962,
       'https://placehold.co/800x800/png?text=DEMO-FURN-002',
       'https://placehold.co/800x800/png?text=DEMO-FURN-002-1,https://placehold.co/800x800/png?text=DEMO-FURN-002-2',
       '人体工学办公椅的演示描述。', '人体工学办公椅', '用于本地开发和接口联调的完整商品数据。',
       '<p>人体工学办公椅</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '家具' AND category_data.level = 1
WHERE brand_data.name = '宜家'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '黑色,灰色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FURN-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '金属与织物'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FURN-002' AND attribute_data.name = '材质' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-039-A', 699.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-039-A',
       CONCAT(attribute_data.id, '=', '黑色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '黑色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FURN-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-039-B', 719.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-039-B',
       CONCAT(attribute_data.id, '=', '灰色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '灰色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FURN-002' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product
(brand_id, product_category_id, name, sub_title, product_sn, price, original_price, stock, low_stock, unit, weight,
 publish_status, new_status, recommend_status, verify_status, sort, pic, album_pics, description,
 detail_title, detail_desc, detail_html, delete_status)
SELECT brand_data.id, category_data.id, '客厅组合收纳柜', '商城演示商品，可用于列表、详情、购物车和订单测试',
       'DEMO-FURN-003', 1299.00, 1499.00, 100, 10, '件', 1.00,
       1, 0, 1, 1, 961,
       'https://placehold.co/800x800/png?text=DEMO-FURN-003',
       'https://placehold.co/800x800/png?text=DEMO-FURN-003-1,https://placehold.co/800x800/png?text=DEMO-FURN-003-2',
       '客厅组合收纳柜的演示描述。', '客厅组合收纳柜', '用于本地开发和接口联调的完整商品数据。',
       '<p>客厅组合收纳柜</p><p>演示商品详情，仅用于开发环境。</p>', 0
FROM pms_brand brand_data
JOIN pms_product_category category_data ON category_data.name = '家具' AND category_data.level = 1
WHERE brand_data.name = '宜家'
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '原木色,白色'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FURN-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_product_attribute_value (product_id, product_attribute_id, value)
SELECT product_data.id, attribute_data.id, '板材'
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FURN-003' AND attribute_data.name = '材质' AND attribute_data.type = 1
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-040-A', 1299.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-040-A',
       CONCAT(attribute_data.id, '=', '原木色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '原木色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FURN-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

INSERT IGNORE INTO pms_sku_stock
(product_id, sku_code, price, stock, locked_stock, low_stock, pic, spec_key, spec_data)
SELECT product_data.id, 'DEMO-SKU-040-B', 1499.00, 50, 0, 5,
       'https://placehold.co/800x800/png?text=DEMO-SKU-040-B',
       CONCAT(attribute_data.id, '=', '白色'),
       JSON_ARRAY(JSON_OBJECT('attributeId', attribute_data.id, 'name', attribute_data.name, 'value', '白色'))
FROM pms_product product_data
JOIN pms_product_attribute_category attribute_category_data ON attribute_category_data.name = '家居属性'
JOIN pms_product_attribute attribute_data ON attribute_data.product_attribute_category_id = attribute_category_data.id
WHERE product_data.product_sn = 'DEMO-FURN-003' AND attribute_data.name = '颜色' AND attribute_data.type = 0
LIMIT 1;

-- 6. 演示会员（统一密码：123456）
INSERT IGNORE INTO ums_member
(username, password, nickname, phone, email, gender, birthday, status)
VALUES
('demo_alice', '$2a$10$5UZqFIfzvkd/afNJbzeVKe1.dbb3bMvcBUlivlEY1B.dxoqwj2NwK', '演示会员小林', '13900001001', 'demo.alice@example.com', 2, '2000-03-12', 1),
('demo_bob',   '$2a$10$5UZqFIfzvkd/afNJbzeVKe1.dbb3bMvcBUlivlEY1B.dxoqwj2NwK', '演示会员阿杰', '13900001002', 'demo.bob@example.com',   1, '1999-08-20', 1),
('demo_cindy', '$2a$10$5UZqFIfzvkd/afNJbzeVKe1.dbb3bMvcBUlivlEY1B.dxoqwj2NwK', '演示会员小陈', '13900001003', 'demo.cindy@example.com', 2, '2001-01-09', 1),
('demo_david', '$2a$10$5UZqFIfzvkd/afNJbzeVKe1.dbb3bMvcBUlivlEY1B.dxoqwj2NwK', '演示会员大卫', '13900001004', 'demo.david@example.com', 1, '1998-11-16', 1),
('demo_emma',  '$2a$10$5UZqFIfzvkd/afNJbzeVKe1.dbb3bMvcBUlivlEY1B.dxoqwj2NwK', '演示会员艾玛', '13900001005', 'demo.emma@example.com',  2, '2002-05-28', 1);

INSERT INTO ums_member_receive_address
(member_id, name, phone_number, default_status, post_code, province, city, region, detail_address)
SELECT member_data.id, '林同学', '13900001001', 1, '100000', '北京市', '北京市', '海淀区', '中关村大街100号'
FROM ums_member member_data
WHERE member_data.username = 'demo_alice'
  AND NOT EXISTS (
      SELECT 1 FROM ums_member_receive_address address_data
      WHERE address_data.member_id = member_data.id AND address_data.detail_address = '中关村大街100号'
  )
LIMIT 1;

INSERT INTO ums_member_receive_address
(member_id, name, phone_number, default_status, post_code, province, city, region, detail_address)
SELECT member_data.id, '杰同学', '13900001002', 1, '100000', '上海市', '上海市', '浦东新区', '张江路200号'
FROM ums_member member_data
WHERE member_data.username = 'demo_bob'
  AND NOT EXISTS (
      SELECT 1 FROM ums_member_receive_address address_data
      WHERE address_data.member_id = member_data.id AND address_data.detail_address = '张江路200号'
  )
LIMIT 1;

INSERT INTO ums_member_receive_address
(member_id, name, phone_number, default_status, post_code, province, city, region, detail_address)
SELECT member_data.id, '陈同学', '13900001003', 1, '100000', '广东省', '深圳市', '南山区', '科技园路300号'
FROM ums_member member_data
WHERE member_data.username = 'demo_cindy'
  AND NOT EXISTS (
      SELECT 1 FROM ums_member_receive_address address_data
      WHERE address_data.member_id = member_data.id AND address_data.detail_address = '科技园路300号'
  )
LIMIT 1;

INSERT INTO ums_member_receive_address
(member_id, name, phone_number, default_status, post_code, province, city, region, detail_address)
SELECT member_data.id, '大卫', '13900001004', 1, '100000', '浙江省', '杭州市', '余杭区', '文一西路400号'
FROM ums_member member_data
WHERE member_data.username = 'demo_david'
  AND NOT EXISTS (
      SELECT 1 FROM ums_member_receive_address address_data
      WHERE address_data.member_id = member_data.id AND address_data.detail_address = '文一西路400号'
  )
LIMIT 1;

INSERT INTO ums_member_receive_address
(member_id, name, phone_number, default_status, post_code, province, city, region, detail_address)
SELECT member_data.id, '艾玛', '13900001005', 1, '100000', '四川省', '成都市', '高新区', '天府大道500号'
FROM ums_member member_data
WHERE member_data.username = 'demo_emma'
  AND NOT EXISTS (
      SELECT 1 FROM ums_member_receive_address address_data
      WHERE address_data.member_id = member_data.id AND address_data.detail_address = '天府大道500号'
  )
LIMIT 1;

-- 7. 演示购物车
INSERT IGNORE INTO oms_cart_item (member_id, product_id, sku_id, quantity, selected)
SELECT member_data.id, product_data.id, sku_data.id, 1, 1
FROM ums_member member_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-001-A'
JOIN pms_product product_data ON product_data.id = sku_data.product_id
WHERE member_data.username = 'demo_alice';

INSERT IGNORE INTO oms_cart_item (member_id, product_id, sku_id, quantity, selected)
SELECT member_data.id, product_data.id, sku_data.id, 2, 1
FROM ums_member member_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-030-A'
JOIN pms_product product_data ON product_data.id = sku_data.product_id
WHERE member_data.username = 'demo_alice';

-- 8. 多状态订单
INSERT IGNORE INTO oms_order
(order_sn, member_id, status, total_amount, pay_amount, receiver_name, receiver_phone, receiver_post_code,
 receiver_province, receiver_city, receiver_region, receiver_detail_address, note, payment_time,
 delivery_company, delivery_sn, delivery_time, receive_time, cancel_time, create_time)
SELECT 'DEMO-ORDER-0001', member_data.id, 0, sku_data.price * 1, sku_data.price * 1,
       '林同学', '13900001001', '100000', '北京市', '北京市', '海淀区', '中关村大街100号',
       '演示订单', NULL,
       NULL, NULL, NULL, NULL, NULL,
       CURRENT_TIMESTAMP - INTERVAL 0 DAY
FROM ums_member member_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-002-A'
WHERE member_data.username = 'demo_alice'
LIMIT 1;

INSERT INTO oms_order_item
(order_id, order_sn, product_id, sku_id, sku_code, product_name, product_pic, spec_data,
 product_price, quantity, subtotal)
SELECT order_data.id, order_data.order_sn, product_data.id, sku_data.id, sku_data.sku_code,
       product_data.name, sku_data.pic, CAST(sku_data.spec_data AS CHAR), sku_data.price, 1, sku_data.price * 1
FROM oms_order order_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-002-A'
JOIN pms_product product_data ON product_data.id = sku_data.product_id
WHERE order_data.order_sn = 'DEMO-ORDER-0001'
  AND NOT EXISTS (
      SELECT 1 FROM oms_order_item item_data
      WHERE item_data.order_id = order_data.id AND item_data.sku_id = sku_data.id
  );

INSERT IGNORE INTO oms_order
(order_sn, member_id, status, total_amount, pay_amount, receiver_name, receiver_phone, receiver_post_code,
 receiver_province, receiver_city, receiver_region, receiver_detail_address, note, payment_time,
 delivery_company, delivery_sn, delivery_time, receive_time, cancel_time, create_time)
SELECT 'DEMO-ORDER-0002', member_data.id, 1, sku_data.price * 1, sku_data.price * 1,
       '林同学', '13900001001', '100000', '北京市', '北京市', '海淀区', '中关村大街100号',
       '演示订单', CURRENT_TIMESTAMP - INTERVAL 1 DAY + INTERVAL 10 MINUTE,
       NULL, NULL, NULL, NULL, NULL,
       CURRENT_TIMESTAMP - INTERVAL 1 DAY
FROM ums_member member_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-008-A'
WHERE member_data.username = 'demo_alice'
LIMIT 1;

INSERT INTO oms_order_item
(order_id, order_sn, product_id, sku_id, sku_code, product_name, product_pic, spec_data,
 product_price, quantity, subtotal)
SELECT order_data.id, order_data.order_sn, product_data.id, sku_data.id, sku_data.sku_code,
       product_data.name, sku_data.pic, CAST(sku_data.spec_data AS CHAR), sku_data.price, 1, sku_data.price * 1
FROM oms_order order_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-008-A'
JOIN pms_product product_data ON product_data.id = sku_data.product_id
WHERE order_data.order_sn = 'DEMO-ORDER-0002'
  AND NOT EXISTS (
      SELECT 1 FROM oms_order_item item_data
      WHERE item_data.order_id = order_data.id AND item_data.sku_id = sku_data.id
  );

INSERT IGNORE INTO oms_order
(order_sn, member_id, status, total_amount, pay_amount, receiver_name, receiver_phone, receiver_post_code,
 receiver_province, receiver_city, receiver_region, receiver_detail_address, note, payment_time,
 delivery_company, delivery_sn, delivery_time, receive_time, cancel_time, create_time)
SELECT 'DEMO-ORDER-0003', member_data.id, 2, sku_data.price * 1, sku_data.price * 1,
       '杰同学', '13900001002', '100000', '上海市', '上海市', '浦东新区', '张江路200号',
       '演示订单', CURRENT_TIMESTAMP - INTERVAL 3 DAY + INTERVAL 10 MINUTE,
       '顺丰速运', 'SF00032026', CURRENT_TIMESTAMP - INTERVAL 2 DAY, NULL, NULL,
       CURRENT_TIMESTAMP - INTERVAL 3 DAY
FROM ums_member member_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-010-A'
WHERE member_data.username = 'demo_bob'
LIMIT 1;

INSERT INTO oms_order_item
(order_id, order_sn, product_id, sku_id, sku_code, product_name, product_pic, spec_data,
 product_price, quantity, subtotal)
SELECT order_data.id, order_data.order_sn, product_data.id, sku_data.id, sku_data.sku_code,
       product_data.name, sku_data.pic, CAST(sku_data.spec_data AS CHAR), sku_data.price, 1, sku_data.price * 1
FROM oms_order order_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-010-A'
JOIN pms_product product_data ON product_data.id = sku_data.product_id
WHERE order_data.order_sn = 'DEMO-ORDER-0003'
  AND NOT EXISTS (
      SELECT 1 FROM oms_order_item item_data
      WHERE item_data.order_id = order_data.id AND item_data.sku_id = sku_data.id
  );

INSERT IGNORE INTO oms_order
(order_sn, member_id, status, total_amount, pay_amount, receiver_name, receiver_phone, receiver_post_code,
 receiver_province, receiver_city, receiver_region, receiver_detail_address, note, payment_time,
 delivery_company, delivery_sn, delivery_time, receive_time, cancel_time, create_time)
SELECT 'DEMO-ORDER-0004', member_data.id, 3, sku_data.price * 2, sku_data.price * 2,
       '陈同学', '13900001003', '100000', '广东省', '深圳市', '南山区', '科技园路300号',
       '演示订单', CURRENT_TIMESTAMP - INTERVAL 15 DAY + INTERVAL 10 MINUTE,
       '顺丰速运', 'SF00042026', CURRENT_TIMESTAMP - INTERVAL 14 DAY, CURRENT_TIMESTAMP - INTERVAL 12 DAY, NULL,
       CURRENT_TIMESTAMP - INTERVAL 15 DAY
FROM ums_member member_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-023-A'
WHERE member_data.username = 'demo_cindy'
LIMIT 1;

INSERT INTO oms_order_item
(order_id, order_sn, product_id, sku_id, sku_code, product_name, product_pic, spec_data,
 product_price, quantity, subtotal)
SELECT order_data.id, order_data.order_sn, product_data.id, sku_data.id, sku_data.sku_code,
       product_data.name, sku_data.pic, CAST(sku_data.spec_data AS CHAR), sku_data.price, 2, sku_data.price * 2
FROM oms_order order_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-023-A'
JOIN pms_product product_data ON product_data.id = sku_data.product_id
WHERE order_data.order_sn = 'DEMO-ORDER-0004'
  AND NOT EXISTS (
      SELECT 1 FROM oms_order_item item_data
      WHERE item_data.order_id = order_data.id AND item_data.sku_id = sku_data.id
  );

INSERT IGNORE INTO oms_order
(order_sn, member_id, status, total_amount, pay_amount, receiver_name, receiver_phone, receiver_post_code,
 receiver_province, receiver_city, receiver_region, receiver_detail_address, note, payment_time,
 delivery_company, delivery_sn, delivery_time, receive_time, cancel_time, create_time)
SELECT 'DEMO-ORDER-0005', member_data.id, 4, sku_data.price * 2, sku_data.price * 2,
       '大卫', '13900001004', '100000', '浙江省', '杭州市', '余杭区', '文一西路400号',
       '演示订单', NULL,
       NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL 6 DAY,
       CURRENT_TIMESTAMP - INTERVAL 7 DAY
FROM ums_member member_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-031-A'
WHERE member_data.username = 'demo_david'
LIMIT 1;

INSERT INTO oms_order_item
(order_id, order_sn, product_id, sku_id, sku_code, product_name, product_pic, spec_data,
 product_price, quantity, subtotal)
SELECT order_data.id, order_data.order_sn, product_data.id, sku_data.id, sku_data.sku_code,
       product_data.name, sku_data.pic, CAST(sku_data.spec_data AS CHAR), sku_data.price, 2, sku_data.price * 2
FROM oms_order order_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-031-A'
JOIN pms_product product_data ON product_data.id = sku_data.product_id
WHERE order_data.order_sn = 'DEMO-ORDER-0005'
  AND NOT EXISTS (
      SELECT 1 FROM oms_order_item item_data
      WHERE item_data.order_id = order_data.id AND item_data.sku_id = sku_data.id
  );

INSERT IGNORE INTO oms_order
(order_sn, member_id, status, total_amount, pay_amount, receiver_name, receiver_phone, receiver_post_code,
 receiver_province, receiver_city, receiver_region, receiver_detail_address, note, payment_time,
 delivery_company, delivery_sn, delivery_time, receive_time, cancel_time, create_time)
SELECT 'DEMO-ORDER-0006', member_data.id, 5, sku_data.price * 1, sku_data.price * 1,
       '艾玛', '13900001005', '100000', '四川省', '成都市', '高新区', '天府大道500号',
       '演示订单', CURRENT_TIMESTAMP - INTERVAL 5 DAY + INTERVAL 10 MINUTE,
       NULL, NULL, NULL, NULL, NULL,
       CURRENT_TIMESTAMP - INTERVAL 5 DAY
FROM ums_member member_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-017-A'
WHERE member_data.username = 'demo_emma'
LIMIT 1;

INSERT INTO oms_order_item
(order_id, order_sn, product_id, sku_id, sku_code, product_name, product_pic, spec_data,
 product_price, quantity, subtotal)
SELECT order_data.id, order_data.order_sn, product_data.id, sku_data.id, sku_data.sku_code,
       product_data.name, sku_data.pic, CAST(sku_data.spec_data AS CHAR), sku_data.price, 1, sku_data.price * 1
FROM oms_order order_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-017-A'
JOIN pms_product product_data ON product_data.id = sku_data.product_id
WHERE order_data.order_sn = 'DEMO-ORDER-0006'
  AND NOT EXISTS (
      SELECT 1 FROM oms_order_item item_data
      WHERE item_data.order_id = order_data.id AND item_data.sku_id = sku_data.id
  );

INSERT IGNORE INTO oms_order
(order_sn, member_id, status, total_amount, pay_amount, receiver_name, receiver_phone, receiver_post_code,
 receiver_province, receiver_city, receiver_region, receiver_detail_address, note, payment_time,
 delivery_company, delivery_sn, delivery_time, receive_time, cancel_time, create_time)
SELECT 'DEMO-ORDER-0007', member_data.id, 6, sku_data.price * 1, sku_data.price * 1,
       '杰同学', '13900001002', '100000', '上海市', '上海市', '浦东新区', '张江路200号',
       '演示订单', CURRENT_TIMESTAMP - INTERVAL 20 DAY + INTERVAL 10 MINUTE,
       NULL, NULL, NULL, NULL, NULL,
       CURRENT_TIMESTAMP - INTERVAL 20 DAY
FROM ums_member member_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-032-A'
WHERE member_data.username = 'demo_bob'
LIMIT 1;

INSERT INTO oms_order_item
(order_id, order_sn, product_id, sku_id, sku_code, product_name, product_pic, spec_data,
 product_price, quantity, subtotal)
SELECT order_data.id, order_data.order_sn, product_data.id, sku_data.id, sku_data.sku_code,
       product_data.name, sku_data.pic, CAST(sku_data.spec_data AS CHAR), sku_data.price, 1, sku_data.price * 1
FROM oms_order order_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-032-A'
JOIN pms_product product_data ON product_data.id = sku_data.product_id
WHERE order_data.order_sn = 'DEMO-ORDER-0007'
  AND NOT EXISTS (
      SELECT 1 FROM oms_order_item item_data
      WHERE item_data.order_id = order_data.id AND item_data.sku_id = sku_data.id
  );

INSERT IGNORE INTO oms_order
(order_sn, member_id, status, total_amount, pay_amount, receiver_name, receiver_phone, receiver_post_code,
 receiver_province, receiver_city, receiver_region, receiver_detail_address, note, payment_time,
 delivery_company, delivery_sn, delivery_time, receive_time, cancel_time, create_time)
SELECT 'DEMO-ORDER-0008', member_data.id, 3, sku_data.price * 1, sku_data.price * 1,
       '陈同学', '13900001003', '100000', '广东省', '深圳市', '南山区', '科技园路300号',
       '演示订单', CURRENT_TIMESTAMP - INTERVAL 30 DAY + INTERVAL 10 MINUTE,
       '顺丰速运', 'SF00082026', CURRENT_TIMESTAMP - INTERVAL 29 DAY, CURRENT_TIMESTAMP - INTERVAL 27 DAY, NULL,
       CURRENT_TIMESTAMP - INTERVAL 30 DAY
FROM ums_member member_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-038-A'
WHERE member_data.username = 'demo_cindy'
LIMIT 1;

INSERT INTO oms_order_item
(order_id, order_sn, product_id, sku_id, sku_code, product_name, product_pic, spec_data,
 product_price, quantity, subtotal)
SELECT order_data.id, order_data.order_sn, product_data.id, sku_data.id, sku_data.sku_code,
       product_data.name, sku_data.pic, CAST(sku_data.spec_data AS CHAR), sku_data.price, 1, sku_data.price * 1
FROM oms_order order_data
JOIN pms_sku_stock sku_data ON sku_data.sku_code = 'DEMO-SKU-038-A'
JOIN pms_product product_data ON product_data.id = sku_data.product_id
WHERE order_data.order_sn = 'DEMO-ORDER-0008'
  AND NOT EXISTS (
      SELECT 1 FROM oms_order_item item_data
      WHERE item_data.order_id = order_data.id AND item_data.sku_id = sku_data.id
  );

-- 9. 退款记录
INSERT IGNORE INTO oms_order_refund
(refund_sn, order_id, order_sn, member_id, refund_amount, reason, status, admin_note, handle_time)
SELECT 'DEMO-REFUND-0001', order_data.id, order_data.order_sn, order_data.member_id, order_data.pay_amount,
       '演示退款申请', 0, NULL, NULL
FROM oms_order order_data WHERE order_data.order_sn = 'DEMO-ORDER-0006';

INSERT IGNORE INTO oms_order_refund
(refund_sn, order_id, order_sn, member_id, refund_amount, reason, status, admin_note, handle_time)
SELECT 'DEMO-REFUND-0002', order_data.id, order_data.order_sn, order_data.member_id, order_data.pay_amount,
       '演示已退款订单', 1, '演示审核通过', CURRENT_TIMESTAMP - INTERVAL 18 DAY
FROM oms_order order_data WHERE order_data.order_sn = 'DEMO-ORDER-0007';

-- 10. 同步统计字段
UPDATE pms_brand brand_data
SET brand_data.product_count = (
    SELECT COUNT(*) FROM pms_product product_data
    WHERE product_data.brand_id = brand_data.id AND product_data.delete_status = 0
);

UPDATE pms_product_category category_data
SET category_data.product_count = (
    SELECT COUNT(*) FROM pms_product product_data
    WHERE product_data.product_category_id = category_data.id AND product_data.delete_status = 0
);

UPDATE pms_product_attribute_category attribute_category_data
SET attribute_category_data.attribute_count = (
        SELECT COUNT(*) FROM pms_product_attribute attribute_data
        WHERE attribute_data.product_attribute_category_id = attribute_category_data.id AND attribute_data.type = 0
    ),
    attribute_category_data.param_count = (
        SELECT COUNT(*) FROM pms_product_attribute attribute_data
        WHERE attribute_data.product_attribute_category_id = attribute_category_data.id AND attribute_data.type = 1
    );

COMMIT;

-- 执行结果检查：商品应为 40，SKU 应为 80，会员应为 5，订单应为 8。
SELECT 'demo_products' AS item, COUNT(*) AS total FROM pms_product WHERE product_sn LIKE 'DEMO-%'
UNION ALL
SELECT 'demo_skus', COUNT(*) FROM pms_sku_stock WHERE sku_code LIKE 'DEMO-SKU-%'
UNION ALL
SELECT 'demo_members', COUNT(*) FROM ums_member WHERE username LIKE 'demo_%'
UNION ALL
SELECT 'demo_orders', COUNT(*) FROM oms_order WHERE order_sn LIKE 'DEMO-ORDER-%';
