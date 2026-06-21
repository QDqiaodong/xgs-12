CREATE DATABASE IF NOT EXISTS retail_material 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE retail_material;

CREATE TABLE `store` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_code` varchar(50) NOT NULL COMMENT '门店编码',
  `store_name` varchar(100) NOT NULL COMMENT '门店名称',
  `store_type` varchar(20) NOT NULL COMMENT '门店类型：tea_milk/automotive/retail/warehouse',
  `address` varchar(200) DEFAULT NULL COMMENT '门店地址',
  `contact_person` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_code` (`store_code`),
  KEY `idx_store_type` (`store_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店信息表';

CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `store_id` bigint DEFAULT NULL COMMENT '所属门店ID',
  `role` varchar(20) NOT NULL COMMENT '角色：admin/warehouse/store_manager/finance',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE `material` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_code` varchar(50) NOT NULL COMMENT '耗材编码',
  `material_name` varchar(100) NOT NULL COMMENT '耗材名称',
  `category` varchar(50) NOT NULL COMMENT '耗材分类：packaging/cleaning/equipment',
  `specification` varchar(100) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) NOT NULL COMMENT '计量单位',
  `purchase_cost` decimal(10,2) NOT NULL COMMENT '采购成本',
  `shelf_life` int DEFAULT NULL COMMENT '保质期（天）',
  `shelf_location` varchar(100) DEFAULT NULL COMMENT '货架位置',
  `safety_stock` int NOT NULL DEFAULT 0 COMMENT '安全库存',
  `current_stock` int NOT NULL DEFAULT 0 COMMENT '当前库存',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常 2预警 3过期',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `update_by` bigint DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_code` (`material_code`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='耗材信息表';

CREATE TABLE `transfer_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) NOT NULL COMMENT '调拨单号',
  `source_store_id` bigint NOT NULL COMMENT '调出门店ID（总仓为0）',
  `target_store_id` bigint NOT NULL COMMENT '目标门店ID',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待审批 1已审批 2已出库 3在途 4已签收 5已驳回',
  `total_cost` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '总成本',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `apply_time` datetime NOT NULL COMMENT '申请时间',
  `approver_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `outbound_time` datetime DEFAULT NULL COMMENT '出库时间',
  `receive_time` datetime DEFAULT NULL COMMENT '签收时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_target_store` (`target_store_id`),
  KEY `idx_status` (`status`),
  KEY `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='调拨单主表';

CREATE TABLE `transfer_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `transfer_order_id` bigint NOT NULL COMMENT '调拨单ID',
  `material_id` bigint NOT NULL COMMENT '耗材ID',
  `material_name` varchar(100) NOT NULL COMMENT '耗材名称',
  `specification` varchar(100) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) NOT NULL COMMENT '计量单位',
  `request_quantity` int NOT NULL COMMENT '申请数量',
  `approved_quantity` int NOT NULL COMMENT '审批数量',
  `actual_quantity` int DEFAULT NULL COMMENT '实收数量',
  `unit_cost` decimal(10,2) NOT NULL COMMENT '单位成本',
  `total_cost` decimal(12,2) NOT NULL COMMENT '总成本',
  `difference` int DEFAULT NULL COMMENT '差异数量',
  `difference_reason` varchar(200) DEFAULT NULL COMMENT '差异原因',
  PRIMARY KEY (`id`),
  KEY `idx_transfer_order_id` (`transfer_order_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='调拨单明细表';

CREATE TABLE `monthly_consumption` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `year` int NOT NULL COMMENT '年份',
  `month` int NOT NULL COMMENT '月份',
  `total_cost` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT '总成本',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_year_month` (`store_id`, `year`, `month`),
  KEY `idx_year_month` (`year`, `month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='月度消耗主表';

CREATE TABLE `consumption_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `consumption_id` bigint NOT NULL COMMENT '消耗记录ID',
  `material_id` bigint NOT NULL COMMENT '耗材ID',
  `material_name` varchar(100) NOT NULL COMMENT '耗材名称',
  `specification` varchar(100) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) NOT NULL COMMENT '计量单位',
  `quantity` int NOT NULL COMMENT '消耗数量',
  `unit_cost` decimal(10,2) NOT NULL COMMENT '单位成本',
  `total_cost` decimal(12,2) NOT NULL COMMENT '总成本',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_consumption_id` (`consumption_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='月度消耗明细表';

CREATE TABLE `inventory_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_no` varchar(50) NOT NULL COMMENT '计划编号',
  `plan_name` varchar(100) NOT NULL COMMENT '计划名称',
  `scope` tinyint NOT NULL COMMENT '盘点范围：0总仓 1门店 2全部',
  `target_stores` varchar(500) DEFAULT NULL COMMENT '目标门店ID列表（JSON数组）',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0草稿 1执行中 2已完成',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_plan_no` (`plan_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='盘点计划表';

CREATE TABLE `inventory_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `plan_id` bigint NOT NULL COMMENT '盘点计划ID',
  `store_id` bigint NOT NULL COMMENT '门店ID（0为总仓）',
  `material_id` bigint NOT NULL COMMENT '耗材ID',
  `material_name` varchar(100) NOT NULL COMMENT '耗材名称',
  `specification` varchar(100) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) NOT NULL COMMENT '计量单位',
  `book_quantity` int NOT NULL COMMENT '账面数量',
  `actual_quantity` int NOT NULL COMMENT '实盘数量',
  `difference` int NOT NULL COMMENT '差异数量',
  `difference_rate` decimal(5,2) NOT NULL COMMENT '差异率（%）',
  `operator_id` bigint NOT NULL COMMENT '盘点人ID',
  `operate_time` datetime NOT NULL COMMENT '盘点时间',
  `photos` varchar(1000) DEFAULT NULL COMMENT '拍照凭证（JSON数组）',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='盘点记录表';

CREATE TABLE `stock_alert` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `material_id` bigint NOT NULL COMMENT '耗材ID',
  `material_name` varchar(100) NOT NULL COMMENT '耗材名称',
  `alert_type` tinyint NOT NULL COMMENT '预警类型：0库存不足 1临期 2过期',
  `alert_message` varchar(200) NOT NULL COMMENT '预警消息',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0未处理 1已处理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_material_id` (`material_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存预警表';

INSERT INTO `sys_user` (`username`, `password`, `real_name`, `role`, `status`) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 'ADMIN', 1),
('warehouse', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '总部库管', 'WAREHOUSE', 1),
('store_manager', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '门店店长', 'STORE_MANAGER', 1),
('finance', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '财务人员', 'FINANCE', 1);

INSERT INTO `store` (`id`, `store_code`, `store_name`, `store_type`, `address`, `contact_person`, `contact_phone`, `status`) VALUES
(0, 'HQ001', '总部总仓', 'warehouse', '总部地址', '总仓管理员', '13800138000', 1);

INSERT INTO `store` (`store_code`, `store_name`, `store_type`, `address`, `contact_person`, `contact_phone`, `status`) VALUES
('ST001', '奶茶1号店', 'tea_milk', '北京市朝阳区xxx路xxx号', '张三', '13800138001', 1),
('ST002', '汽修中心店', 'automotive', '北京市海淀区xxx路xxx号', '李四', '13800138002', 1),
('ST003', '零售旗舰店', 'retail', '北京市西城区xxx路xxx号', '王五', '13800138003', 1);

INSERT INTO `material` (`material_code`, `material_name`, `category`, `specification`, `unit`, `purchase_cost`, `shelf_life`, `shelf_location`, `safety_stock`, `current_stock`, `status`) VALUES
('M001', '奶茶杯500ml', 'packaging', '500ml/PP材质', '个', 0.35, 365, 'A区-1货架', 1000, 5000, 1),
('M002', '奶茶杯700ml', 'packaging', '700ml/PP材质', '个', 0.45, 365, 'A区-1货架', 800, 3000, 1),
('M003', '吸管', 'packaging', '标准长度20cm', '包', 5.00, 365, 'A区-2货架', 200, 1000, 1),
('M004', '封口膜', 'packaging', '标准规格', '卷', 25.00, 365, 'A区-2货架', 50, 200, 1),
('M005', '清洁剂', 'cleaning', '500ml/中性', '瓶', 15.00, 730, 'B区-1货架', 30, 100, 1),
('M006', '消毒液', 'cleaning', '1L/医用级', '瓶', 28.00, 365, 'B区-1货架', 20, 80, 1),
('M007', '手套', 'cleaning', '一次性/L码', '包', 8.00, 365, 'B区-2货架', 100, 500, 1),
('M008', '抹布', 'cleaning', '标准规格', '条', 3.00, 180, 'B区-2货架', 50, 200, 1),
('M009', '机油滤芯', 'equipment', '标准型号', '个', 35.00, NULL, 'C区-1货架', 20, 100, 1),
('M010', '刹车片', 'equipment', '前轮标准型', '套', 120.00, NULL, 'C区-1货架', 10, 50, 1);
