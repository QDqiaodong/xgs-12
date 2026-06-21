-- 连锁零售门店耗材进销存管控系统 数据库初始化脚本
-- Database: retail_material

CREATE DATABASE IF NOT EXISTS retail_material DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE retail_material;

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色: ADMIN-管理员, WAREHOUSE-库管, STORE_MANAGER-店长, FINANCE-财务',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    store_id BIGINT COMMENT '所属门店ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
    UNIQUE KEY uk_username (username),
    KEY idx_store_id (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- 门店表
CREATE TABLE IF NOT EXISTS store (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    store_code VARCHAR(50) NOT NULL COMMENT '门店编码',
    store_name VARCHAR(100) NOT NULL COMMENT '门店名称',
    store_type VARCHAR(20) NOT NULL COMMENT '门店类型: tea_milk/automotive/retail/warehouse',
    address VARCHAR(255) COMMENT '门店地址',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-关闭, 1-营业',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_store_code (store_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店表';

-- 耗材分类表
CREATE TABLE IF NOT EXISTS material_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    category_code VARCHAR(50) NOT NULL COMMENT '分类编码',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_category_code (category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='耗材分类表';

-- 耗材信息表
CREATE TABLE IF NOT EXISTS material (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    material_code VARCHAR(50) NOT NULL COMMENT '耗材编码',
    material_name VARCHAR(100) NOT NULL COMMENT '耗材名称',
    category_id BIGINT COMMENT '分类ID',
    unit VARCHAR(20) COMMENT '单位',
    specification VARCHAR(100) COMMENT '规格型号',
    brand VARCHAR(50) COMMENT '品牌',
    price DECIMAL(10,2) COMMENT '参考单价',
    safety_stock INT DEFAULT 0 COMMENT '安全库存',
    description VARCHAR(500) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_material_code (material_code),
    KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='耗材信息表';

-- 门店库存表
CREATE TABLE IF NOT EXISTS store_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    material_id BIGINT NOT NULL COMMENT '耗材ID',
    quantity INT DEFAULT 0 COMMENT '库存数量',
    locked_quantity INT DEFAULT 0 COMMENT '锁定数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_store_material (store_id, material_id),
    KEY idx_store_id (store_id),
    KEY idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店库存表';

-- 入库单表
CREATE TABLE IF NOT EXISTS inbound_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL COMMENT '入库单号',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    order_type TINYINT DEFAULT 1 COMMENT '入库类型: 1-采购入库, 2-调拨入库, 3-退货入库',
    supplier VARCHAR(100) COMMENT '供应商',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '总金额',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待审核, 1-已审核, 2-已入库, 3-已取消',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_store_id (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库单表';

-- 入库单明细表
CREATE TABLE IF NOT EXISTS inbound_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '入库单ID',
    material_id BIGINT NOT NULL COMMENT '耗材ID',
    quantity INT NOT NULL COMMENT '入库数量',
    unit_price DECIMAL(10,2) COMMENT '单价',
    amount DECIMAL(12,2) COMMENT '金额',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_order_id (order_id),
    KEY idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库单明细表';

-- 出库单表
CREATE TABLE IF NOT EXISTS outbound_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL COMMENT '出库单号',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    order_type TINYINT DEFAULT 1 COMMENT '出库类型: 1-领用出库, 2-调拨出库, 3-报损出库',
    recipient VARCHAR(50) COMMENT '领用人',
    total_amount DECIMAL(12,2) DEFAULT 0 COMMENT '总金额',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待审核, 1-已审核, 2-已出库, 3-已取消',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_store_id (store_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库单表';

-- 出库单明细表
CREATE TABLE IF NOT EXISTS outbound_order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id BIGINT NOT NULL COMMENT '出库单ID',
    material_id BIGINT NOT NULL COMMENT '耗材ID',
    quantity INT NOT NULL COMMENT '出库数量',
    unit_price DECIMAL(10,2) COMMENT '单价',
    amount DECIMAL(12,2) COMMENT '金额',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    KEY idx_order_id (order_id),
    KEY idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库单明细表';

-- 库存流水表
CREATE TABLE IF NOT EXISTS inventory_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    material_id BIGINT NOT NULL COMMENT '耗材ID',
    change_type TINYINT NOT NULL COMMENT '变动类型: 1-入库, 2-出库, 3-调拨, 4-盘点',
    change_quantity INT NOT NULL COMMENT '变动数量(正数增加,负数减少)',
    before_quantity INT COMMENT '变动前数量',
    after_quantity INT COMMENT '变动后数量',
    order_no VARCHAR(50) COMMENT '关联单号',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人',
    KEY idx_store_material (store_id, material_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';

-- 初始化管理员账号 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, role, status) VALUES 
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 'ADMIN', 1);

-- 初始化示例门店
INSERT INTO store (store_code, store_name, store_type, address, contact_person, contact_phone, status) VALUES 
('ST001', '奶茶1号店', 'tea_milk', '北京市朝阳区xxx路xxx号', '张三', '13800138001', 1),
('ST002', '汽修中心店', 'automotive', '北京市海淀区xxx路xxx号', '李四', '13800138002', 1),
('ST003', '零售旗舰店', 'retail', '北京市西城区xxx路xxx号', '王五', '13800138003', 1);

-- 初始化示例耗材分类
INSERT INTO material_category (category_code, category_name, parent_id, sort_order, status) VALUES 
('C001', '办公用品', 0, 1, 1),
('C002', '清洁用品', 0, 2, 1),
('C003', '包装材料', 0, 3, 1),
('C004', '文具', 1, 1, 1),
('C005', '打印耗材', 1, 2, 1);

-- 初始化示例耗材
INSERT INTO material (material_code, material_name, category_id, unit, specification, brand, price, safety_stock, status) VALUES 
('M001', 'A4打印纸', 4, '包', '500张/包', '得力', 25.00, 50, 1),
('M002', '签字笔', 4, '盒', '12支/盒', '晨光', 15.00, 100, 1),
('M003', '垃圾袋', 2, '卷', '100只/卷', '美丽雅', 8.00, 200, 1),
('M004', '洗洁精', 2, '瓶', '1.5L', '立白', 12.00, 50, 1),
('M005', '购物袋', 3, '包', '100只/包', '定制', 35.00, 100, 1);
