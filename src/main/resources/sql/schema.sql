-- Mall 单体后端完整空库结构（MySQL 8）
-- 可重复执行；只创建不存在的库和表，不删除、清空或覆盖现有数据。
CREATE DATABASE IF NOT EXISTS mall
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
USE mall;

CREATE TABLE IF NOT EXISTS ums_member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(64) NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(100) NULL,
    avatar VARCHAR(255) NULL,
    gender TINYINT NOT NULL DEFAULT 0,
    birthday DATE NULL,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_username (username),
    UNIQUE KEY uk_member_phone (phone),
    UNIQUE KEY uk_member_email (email),
    KEY idx_member_status_time (status, create_time, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ums_member_receive_address (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(32) NOT NULL,
    default_status TINYINT NOT NULL DEFAULT 0,
    post_code VARCHAR(16) NULL,
    province VARCHAR(64) NOT NULL,
    city VARCHAR(64) NOT NULL,
    region VARCHAR(64) NOT NULL,
    detail_address VARCHAR(255) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_address_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ums_admin (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NULL,
    email VARCHAR(128) NULL,
    avatar VARCHAR(255) NULL,
    note VARCHAR(500) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    login_time DATETIME NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_username (username),
    UNIQUE KEY uk_admin_email (email),
    KEY idx_admin_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ums_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    sort INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (code),
    KEY idx_role_status_sort (status, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ums_resource (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(128) NOT NULL,
    url_pattern VARCHAR(255) NULL,
    http_method VARCHAR(16) NULL,
    description VARCHAR(500) NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_resource_code (code),
    KEY idx_resource_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ums_admin_role_relation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    admin_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_role (admin_id, role_id),
    KEY idx_admin_role_role_id (role_id),
    CONSTRAINT fk_admin_role_admin FOREIGN KEY (admin_id)
        REFERENCES ums_admin (id) ON DELETE CASCADE,
    CONSTRAINT fk_admin_role_role FOREIGN KEY (role_id)
        REFERENCES ums_role (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS ums_role_resource_relation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_resource (role_id, resource_id),
    KEY idx_role_resource_resource_id (resource_id),
    CONSTRAINT fk_role_resource_role FOREIGN KEY (role_id)
        REFERENCES ums_role (id) ON DELETE CASCADE,
    CONSTRAINT fk_role_resource_resource FOREIGN KEY (resource_id)
        REFERENCES ums_resource (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS pms_brand (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    first_letter CHAR(1) NULL,
    sort INT NOT NULL DEFAULT 0,
    factory_status TINYINT NOT NULL DEFAULT 0,
    show_status TINYINT NOT NULL DEFAULT 1,
    product_count INT NOT NULL DEFAULT 0,
    product_comment_count INT NOT NULL DEFAULT 0,
    logo VARCHAR(255) NULL,
    big_pic VARCHAR(255) NULL,
    brand_story TEXT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pms_brand_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS pms_product_category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    level INT NOT NULL,
    product_count INT NOT NULL DEFAULT 0,
    product_unit VARCHAR(16) NULL,
    nav_status TINYINT NOT NULL DEFAULT 0,
    show_status TINYINT NOT NULL DEFAULT 1,
    sort INT NOT NULL DEFAULT 0,
    icon VARCHAR(255) NULL,
    keywords VARCHAR(255) NULL,
    description VARCHAR(500) NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_parent_name (parent_id, name),
    KEY idx_category_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS pms_product_attribute_category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    attribute_count INT NOT NULL DEFAULT 0,
    param_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_attribute_category_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS pms_product_attribute (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_attribute_category_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    select_type TINYINT NOT NULL DEFAULT 0,
    input_type TINYINT NOT NULL DEFAULT 0,
    input_list VARCHAR(500) NULL,
    sort INT NOT NULL DEFAULT 0,
    filter_type TINYINT NOT NULL DEFAULT 0,
    search_type TINYINT NOT NULL DEFAULT 0,
    related_status TINYINT NOT NULL DEFAULT 0,
    hand_add_status TINYINT NOT NULL DEFAULT 0,
    type TINYINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_attribute_category_name_type
        (product_attribute_category_id, name, type),
    KEY idx_attribute_category_id (product_attribute_category_id),
    CONSTRAINT fk_attribute_category FOREIGN KEY (product_attribute_category_id)
        REFERENCES pms_product_attribute_category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS pms_product_category_attribute_relation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_category_id BIGINT NOT NULL,
    product_attribute_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_attribute (product_category_id, product_attribute_id),
    KEY idx_relation_attribute_id (product_attribute_id),
    CONSTRAINT fk_relation_product_category FOREIGN KEY (product_category_id)
        REFERENCES pms_product_category (id),
    CONSTRAINT fk_relation_product_attribute FOREIGN KEY (product_attribute_id)
        REFERENCES pms_product_attribute (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS pms_product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    brand_id BIGINT NULL,
    product_category_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    sub_title VARCHAR(255) NULL,
    product_sn VARCHAR(64) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2) NULL,
    stock INT NOT NULL DEFAULT 0,
    low_stock INT NOT NULL DEFAULT 0,
    unit VARCHAR(16) NULL,
    weight DECIMAL(10,2) NULL,
    publish_status TINYINT NOT NULL DEFAULT 0,
    new_status TINYINT NOT NULL DEFAULT 0,
    recommend_status TINYINT NOT NULL DEFAULT 0,
    verify_status TINYINT NOT NULL DEFAULT 0,
    sort INT NOT NULL DEFAULT 0,
    pic VARCHAR(255) NULL,
    album_pics VARCHAR(2000) NULL,
    description TEXT NULL,
    detail_title VARCHAR(255) NULL,
    detail_desc VARCHAR(1000) NULL,
    detail_html LONGTEXT NULL,
    delete_status TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_sn (product_sn),
    KEY idx_product_brand_id (brand_id),
    KEY idx_product_category_id (product_category_id),
    KEY idx_product_publish_status (publish_status, delete_status, create_time, id),
    CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES pms_brand (id),
    CONSTRAINT fk_product_category FOREIGN KEY (product_category_id)
        REFERENCES pms_product_category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS pms_product_attribute_value (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    product_attribute_id BIGINT NOT NULL,
    value VARCHAR(1000) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_product_attribute (product_id, product_attribute_id),
    KEY idx_attribute_id (product_attribute_id),
    CONSTRAINT fk_pav_product FOREIGN KEY (product_id) REFERENCES pms_product (id),
    CONSTRAINT fk_pav_attribute FOREIGN KEY (product_attribute_id)
        REFERENCES pms_product_attribute (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS pms_sku_stock (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT UNSIGNED NOT NULL DEFAULT 0,
    locked_stock INT UNSIGNED NOT NULL DEFAULT 0,
    low_stock INT UNSIGNED NOT NULL DEFAULT 0,
    pic VARCHAR(255) NULL,
    spec_key VARCHAR(500) NOT NULL,
    spec_data JSON NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sku_code (sku_code),
    UNIQUE KEY uk_product_spec (product_id, spec_key),
    KEY idx_sku_product_id (product_id),
    CONSTRAINT fk_sku_product FOREIGN KEY (product_id) REFERENCES pms_product (id),
    CONSTRAINT chk_sku_locked_stock CHECK (locked_stock <= stock),
    CONSTRAINT chk_sku_price CHECK (price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS oms_cart_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    selected TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_cart_member_sku (member_id, sku_id),
    KEY idx_cart_member_id (member_id),
    KEY idx_cart_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS oms_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_sn VARCHAR(64) NOT NULL,
    member_id BIGINT NOT NULL,
    submit_token VARCHAR(64) NULL COMMENT '下单幂等令牌，历史订单允许为空',
    status TINYINT NOT NULL DEFAULT 0
        COMMENT '0待支付 1待发货 2已发货 3已完成 4已取消 5退款处理中 6已退款',
    total_amount DECIMAL(12,2) NOT NULL,
    pay_amount DECIMAL(12,2) NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(32) NOT NULL,
    receiver_post_code VARCHAR(20) NULL,
    receiver_province VARCHAR(64) NOT NULL,
    receiver_city VARCHAR(64) NOT NULL,
    receiver_region VARCHAR(64) NOT NULL,
    receiver_detail_address VARCHAR(255) NOT NULL,
    note VARCHAR(500) NULL,
    payment_time DATETIME NULL,
    delivery_company VARCHAR(64) NULL,
    delivery_sn VARCHAR(64) NULL,
    delivery_time DATETIME NULL,
    receive_time DATETIME NULL,
    cancel_time DATETIME NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_sn (order_sn),
    KEY idx_order_member_status_time (member_id, status, create_time, id),
    UNIQUE KEY uk_order_member_submit_token (member_id, submit_token),
    KEY idx_order_timeout_scan (status, create_time, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS oms_order_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    order_sn VARCHAR(64) NOT NULL,
    product_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    sku_code VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    product_pic VARCHAR(500) NULL,
    spec_data TEXT NULL,
    product_price DECIMAL(12,2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order_item_order_id (order_id),
    KEY idx_order_item_order_sn (order_sn),
    KEY idx_order_item_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS oms_order_refund (
    id BIGINT NOT NULL AUTO_INCREMENT,
    refund_sn VARCHAR(64) NOT NULL,
    order_id BIGINT NOT NULL,
    order_sn VARCHAR(64) NOT NULL,
    member_id BIGINT NOT NULL,
    refund_amount DECIMAL(12,2) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1已退款 2已拒绝',
    admin_note VARCHAR(500) NULL,
    handle_time DATETIME NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refund_sn (refund_sn),
    KEY idx_refund_order_id (order_id),
    KEY idx_refund_order_sn (order_sn),
    KEY idx_refund_member_status_time (member_id, status, create_time, id),
    KEY idx_refund_status_time (status, create_time, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
