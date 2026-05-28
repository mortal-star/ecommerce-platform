CREATE DATABASE IF NOT EXISTS ecommerce DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS cms_announcement;
DROP TABLE IF EXISTS cms_banner;
DROP TABLE IF EXISTS cms_feedback;
DROP TABLE IF EXISTS oms_order_status_log;
DROP TABLE IF EXISTS oms_payment_record;
DROP TABLE IF EXISTS oms_order_item;
DROP TABLE IF EXISTS oms_order;
DROP TABLE IF EXISTS ums_cart_item;
DROP TABLE IF EXISTS pms_product_review;
DROP TABLE IF EXISTS pms_user_favorite;
DROP TABLE IF EXISTS pms_product_sku;
DROP TABLE IF EXISTS pms_product_image;
DROP TABLE IF EXISTS pms_product;
DROP TABLE IF EXISTS pms_category;
DROP TABLE IF EXISTS ums_user_address;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(64) DEFAULT NULL,
    avatar VARCHAR(255) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    email VARCHAR(128) DEFAULT NULL,
    gender TINYINT NOT NULL DEFAULT 0,
    user_type TINYINT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    last_login_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    UNIQUE KEY uk_sys_user_phone (phone),
    UNIQUE KEY uk_sys_user_email (email),
    KEY idx_sys_user_status (status),
    KEY idx_sys_user_user_type (user_type),
    KEY idx_sys_user_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    description VARCHAR(255) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    sort INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code),
    KEY idx_sys_role_status (status),
    KEY idx_sys_role_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    permission_code VARCHAR(128) NOT NULL,
    permission_name VARCHAR(64) NOT NULL,
    permission_type TINYINT NOT NULL DEFAULT 2,
    path VARCHAR(255) DEFAULT NULL,
    component VARCHAR(255) DEFAULT NULL,
    icon VARCHAR(64) DEFAULT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (permission_code),
    KEY idx_sys_permission_parent_id (parent_id),
    KEY idx_sys_permission_status (status),
    KEY idx_sys_permission_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

CREATE TABLE sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sys_user_role_role_id (role_id),
    KEY idx_sys_user_role_deleted (deleted),
    CONSTRAINT fk_sys_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_sys_user_role_role FOREIGN KEY (role_id) REFERENCES sys_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

CREATE TABLE sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_permission (role_id, permission_id),
    KEY idx_sys_role_permission_permission_id (permission_id),
    KEY idx_sys_role_permission_deleted (deleted),
    CONSTRAINT fk_sys_role_permission_role FOREIGN KEY (role_id) REFERENCES sys_role (id),
    CONSTRAINT fk_sys_role_permission_permission FOREIGN KEY (permission_id) REFERENCES sys_permission (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

CREATE TABLE ums_user_address (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    receiver_name VARCHAR(64) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    province VARCHAR(64) NOT NULL,
    city VARCHAR(64) NOT NULL,
    district VARCHAR(64) NOT NULL,
    detail_address VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20) DEFAULT NULL,
    default_flag TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_ums_user_address_user_id (user_id),
    KEY idx_ums_user_address_default (user_id, default_flag),
    KEY idx_ums_user_address_deleted (deleted),
    CONSTRAINT fk_ums_user_address_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';

CREATE TABLE pms_category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    icon VARCHAR(255) DEFAULT NULL,
    sort INT NOT NULL DEFAULT 0,
    level TINYINT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_pms_category_parent_id (parent_id),
    KEY idx_pms_category_status_sort (status, sort),
    KEY idx_pms_category_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

CREATE TABLE pms_product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    subtitle VARCHAR(255) DEFAULT NULL,
    main_image VARCHAR(255) DEFAULT NULL,
    detail TEXT,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    original_price DECIMAL(10,2) DEFAULT NULL,
    stock INT NOT NULL DEFAULT 0,
    sales INT NOT NULL DEFAULT 0,
    unit VARCHAR(16) DEFAULT '件',
    status TINYINT NOT NULL DEFAULT 1,
    sort INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_pms_product_category_id (category_id),
    KEY idx_pms_product_status_sort (status, sort),
    KEY idx_pms_product_name (name),
    KEY idx_pms_product_deleted (deleted),
    FULLTEXT KEY ft_pms_product_name_subtitle (name, subtitle),
    CONSTRAINT fk_pms_product_category FOREIGN KEY (category_id) REFERENCES pms_category (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

CREATE TABLE pms_product_image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    image_type TINYINT NOT NULL DEFAULT 1,
    sort INT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_pms_product_image_product_id (product_id),
    KEY idx_pms_product_image_deleted (deleted),
    CONSTRAINT fk_pms_product_image_product FOREIGN KEY (product_id) REFERENCES pms_product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品图片表';

CREATE TABLE pms_product_sku (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    sku_code VARCHAR(64) NOT NULL,
    sku_name VARCHAR(128) NOT NULL,
    attributes JSON DEFAULT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    original_price DECIMAL(10,2) DEFAULT NULL,
    stock INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pms_product_sku_code (sku_code),
    KEY idx_pms_product_sku_product_id (product_id),
    KEY idx_pms_product_sku_status (status),
    KEY idx_pms_product_sku_deleted (deleted),
    CONSTRAINT fk_pms_product_sku_product FOREIGN KEY (product_id) REFERENCES pms_product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

CREATE TABLE pms_user_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pms_user_favorite (user_id, product_id),
    KEY idx_pms_user_favorite_product_id (product_id),
    KEY idx_pms_user_favorite_deleted (deleted),
    CONSTRAINT fk_pms_user_favorite_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_pms_user_favorite_product FOREIGN KEY (product_id) REFERENCES pms_product (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏表';

CREATE TABLE pms_product_review (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    order_item_id BIGINT DEFAULT NULL,
    rating TINYINT NOT NULL,
    content VARCHAR(1000) DEFAULT NULL,
    images JSON DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_pms_product_review_user_id (user_id),
    KEY idx_pms_product_review_product_id (product_id),
    KEY idx_pms_product_review_status (status),
    KEY idx_pms_product_review_deleted (deleted),
    CONSTRAINT fk_pms_product_review_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_pms_product_review_product FOREIGN KEY (product_id) REFERENCES pms_product (id),
    CONSTRAINT chk_pms_product_review_rating CHECK (rating BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

CREATE TABLE ums_cart_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    selected TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_ums_cart_item (user_id, product_id, sku_id),
    KEY idx_ums_cart_item_user_id (user_id),
    KEY idx_ums_cart_item_product_id (product_id),
    KEY idx_ums_cart_item_sku_id (sku_id),
    KEY idx_ums_cart_item_deleted (deleted),
    CONSTRAINT fk_ums_cart_item_user FOREIGN KEY (user_id) REFERENCES sys_user (id),
    CONSTRAINT fk_ums_cart_item_product FOREIGN KEY (product_id) REFERENCES pms_product (id),
    CONSTRAINT fk_ums_cart_item_sku FOREIGN KEY (sku_id) REFERENCES pms_product_sku (id),
    CONSTRAINT chk_ums_cart_item_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车明细表';

CREATE TABLE oms_order (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    freight_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    pay_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_type TINYINT DEFAULT NULL,
    order_status TINYINT NOT NULL DEFAULT 0,
    receiver_name VARCHAR(64) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_province VARCHAR(64) NOT NULL,
    receiver_city VARCHAR(64) NOT NULL,
    receiver_district VARCHAR(64) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    remark VARCHAR(255) DEFAULT NULL,
    pay_time DATETIME DEFAULT NULL,
    delivery_time DATETIME DEFAULT NULL,
    receive_time DATETIME DEFAULT NULL,
    close_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_oms_order_no (order_no),
    KEY idx_oms_order_user_id (user_id),
    KEY idx_oms_order_status (order_status),
    KEY idx_oms_order_create_time (create_time),
    KEY idx_oms_order_deleted (deleted),
    CONSTRAINT fk_oms_order_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE oms_order_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    product_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    product_name VARCHAR(128) NOT NULL,
    product_image VARCHAR(255) DEFAULT NULL,
    sku_name VARCHAR(128) DEFAULT NULL,
    product_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    quantity INT NOT NULL DEFAULT 1,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_oms_order_item_order_id (order_id),
    KEY idx_oms_order_item_order_no (order_no),
    KEY idx_oms_order_item_product_id (product_id),
    KEY idx_oms_order_item_sku_id (sku_id),
    KEY idx_oms_order_item_deleted (deleted),
    CONSTRAINT fk_oms_order_item_order FOREIGN KEY (order_id) REFERENCES oms_order (id),
    CONSTRAINT fk_oms_order_item_product FOREIGN KEY (product_id) REFERENCES pms_product (id),
    CONSTRAINT fk_oms_order_item_sku FOREIGN KEY (sku_id) REFERENCES pms_product_sku (id),
    CONSTRAINT chk_oms_order_item_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

ALTER TABLE pms_product_review
    ADD CONSTRAINT fk_pms_product_review_order_item FOREIGN KEY (order_item_id) REFERENCES oms_order_item (id);

CREATE TABLE oms_payment_record (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    payment_no VARCHAR(128) DEFAULT NULL,
    payment_type TINYINT NOT NULL,
    payment_status TINYINT NOT NULL DEFAULT 0,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    transaction_id VARCHAR(128) DEFAULT NULL,
    callback_content TEXT,
    pay_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_oms_payment_record_order_id (order_id),
    KEY idx_oms_payment_record_order_no (order_no),
    KEY idx_oms_payment_record_payment_status (payment_status),
    KEY idx_oms_payment_record_deleted (deleted),
    CONSTRAINT fk_oms_payment_record_order FOREIGN KEY (order_id) REFERENCES oms_order (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

CREATE TABLE oms_order_status_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    order_no VARCHAR(64) NOT NULL,
    from_status TINYINT DEFAULT NULL,
    to_status TINYINT NOT NULL,
    operator_id BIGINT DEFAULT NULL,
    operator_type TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(255) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_oms_order_status_log_order_id (order_id),
    KEY idx_oms_order_status_log_order_no (order_no),
    KEY idx_oms_order_status_log_operator_id (operator_id),
    KEY idx_oms_order_status_log_deleted (deleted),
    CONSTRAINT fk_oms_order_status_log_order FOREIGN KEY (order_id) REFERENCES oms_order (id),
    CONSTRAINT fk_oms_order_status_log_operator FOREIGN KEY (operator_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单状态日志表';

CREATE TABLE cms_banner (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    link_url VARCHAR(255) DEFAULT NULL,
    sort INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    start_time DATETIME DEFAULT NULL,
    end_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_cms_banner_status_sort (status, sort),
    KEY idx_cms_banner_time (start_time, end_time),
    KEY idx_cms_banner_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

CREATE TABLE cms_announcement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    content TEXT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    sort INT NOT NULL DEFAULT 0,
    publish_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_cms_announcement_status_sort (status, sort),
    KEY idx_cms_announcement_publish_time (publish_time),
    KEY idx_cms_announcement_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告表';

CREATE TABLE cms_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT DEFAULT NULL,
    contact VARCHAR(128) DEFAULT NULL,
    content VARCHAR(1000) NOT NULL,
    reply VARCHAR(1000) DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    reply_time DATETIME DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_cms_feedback_user_id (user_id),
    KEY idx_cms_feedback_status (status),
    KEY idx_cms_feedback_deleted (deleted),
    CONSTRAINT fk_cms_feedback_user FOREIGN KEY (user_id) REFERENCES sys_user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

INSERT INTO sys_user (id, username, password, nickname, phone, email, user_type, status)
VALUES
(1, 'admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi7qQnm1ydV2M5c85o7iZJ3zW9xJj8K', '系统管理员', '13800000000', 'admin@example.com', 2, 1),
(2, 'testuser', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi7qQnm1ydV2M5c85o7iZJ3zW9xJj8K', '测试用户', '13900000000', 'user@example.com', 1, 1);

INSERT INTO sys_role (id, role_code, role_name, description, status, sort)
VALUES
(1, 'ADMIN', '超级管理员', '拥有系统全部权限', 1, 1),
(2, 'USER', '普通用户', '用户端默认角色', 1, 2);

INSERT INTO sys_permission (id, parent_id, permission_code, permission_name, permission_type, path, component, sort, status)
VALUES
(1, 0, 'system', '系统管理', 1, '/system', NULL, 1, 1),
(2, 1, 'system:user', '用户管理', 2, '/system/user', NULL, 1, 1),
(3, 1, 'system:role', '角色管理', 2, '/system/role', NULL, 2, 1),
(4, 1, 'system:permission', '权限管理', 2, '/system/permission', NULL, 3, 1),
(5, 0, 'product', '商品管理', 1, '/product', NULL, 2, 1),
(6, 5, 'product:category', '分类管理', 2, '/product/category', NULL, 1, 1),
(7, 5, 'product:item', '商品管理', 2, '/product/item', NULL, 2, 1),
(8, 0, 'order', '订单管理', 1, '/order', NULL, 3, 1),
(9, 8, 'order:list', '订单列表', 2, '/order/list', NULL, 1, 1),
(10, 0, 'content', '内容管理', 1, '/content', NULL, 4, 1),
(11, 10, 'content:banner', '轮播图管理', 2, '/content/banner', NULL, 1, 1),
(12, 10, 'content:announcement', '公告管理', 2, '/content/announcement', NULL, 2, 1);

INSERT INTO sys_user_role (user_id, role_id)
VALUES
(1, 1),
(2, 2);

INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

INSERT INTO pms_category (id, parent_id, name, icon, sort, level, status)
VALUES
(1, 0, '手机数码', NULL, 1, 1, 1),
(2, 0, '电脑办公', NULL, 2, 1, 1),
(3, 0, '家用电器', NULL, 3, 1, 1),
(4, 0, '服饰鞋包', NULL, 4, 1, 1),
(5, 0, '食品生鲜', NULL, 5, 1, 1),
(6, 1, '智能手机', NULL, 1, 2, 1),
(7, 1, '智能穿戴', NULL, 2, 2, 1),
(8, 2, '笔记本电脑', NULL, 1, 2, 1),
(9, 2, '办公外设', NULL, 2, 2, 1);

INSERT INTO pms_product (id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, status, sort)
VALUES
(1, 6, '示例智能手机', '高性能 5G 智能手机', '/images/products/phone.jpg', '示例商品详情', 2999.00, 3299.00, 100, 0, '台', 1, 1),
(2, 8, '示例轻薄笔记本', '高性能轻薄办公笔记本', '/images/products/laptop.jpg', '示例商品详情', 5999.00, 6499.00, 50, 0, '台', 1, 2);

INSERT INTO pms_product_sku (id, product_id, sku_code, sku_name, attributes, price, original_price, stock, status)
VALUES
(1, 1, 'PHONE-8G-128G-BLACK', '8GB+128GB 黑色', JSON_OBJECT('memory', '8GB', 'storage', '128GB', 'color', '黑色'), 2999.00, 3299.00, 60, 1),
(2, 1, 'PHONE-12G-256G-WHITE', '12GB+256GB 白色', JSON_OBJECT('memory', '12GB', 'storage', '256GB', 'color', '白色'), 3499.00, 3799.00, 40, 1),
(3, 2, 'LAPTOP-16G-512G-GRAY', '16GB+512GB 灰色', JSON_OBJECT('memory', '16GB', 'storage', '512GB', 'color', '灰色'), 5999.00, 6499.00, 50, 1);

INSERT INTO pms_product_image (product_id, image_url, image_type, sort)
VALUES
(1, '/images/products/phone.jpg', 1, 1),
(1, '/images/products/phone-detail-1.jpg', 2, 2),
(2, '/images/products/laptop.jpg', 1, 1),
(2, '/images/products/laptop-detail-1.jpg', 2, 2);

INSERT INTO cms_banner (title, image_url, link_url, sort, status)
VALUES
('新品上市', '/images/banners/new-products.jpg', '/products?sort=new', 1, 1),
('限时优惠', '/images/banners/discount.jpg', '/products?tag=discount', 2, 1);

INSERT INTO cms_announcement (title, content, status, sort, publish_time)
VALUES
('商城上线公告', '欢迎使用电商平台，祝您购物愉快。', 1, 1, NOW());
