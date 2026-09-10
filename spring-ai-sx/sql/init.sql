-- ============================================================
-- 艾瑞电商 AI 客服系统初始化脚本
-- 包含：建库 + t_faq（8条）+ t_sensitive_word（4条）+ t_order（6条）
-- 首次部署执行一次即可（脚本会重建表并插入初始数据）
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `ai-agent`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE `ai-agent`;

-- ------------------------------------------------------------
-- FAQ 常见问题表（MySQL 为主数据源，Milvus 为向量检索副本）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_faq`;
CREATE TABLE `t_faq` (
  `id`          VARCHAR(64)  NOT NULL COMMENT 'FAQ ID（UUID）',
  `category_id` INT          NOT NULL DEFAULT 0 COMMENT '分类：1-订单 2-支付 3-商品 4-账户 5-其他',
  `question`    VARCHAR(500) NOT NULL COMMENT '常见问题',
  `answer`      TEXT         NOT NULL COMMENT '标准答案',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `use_count`   INT          NOT NULL DEFAULT 0 COMMENT '命中次数',
  PRIMARY KEY (`id`),
  INDEX `idx_category` (`category_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `t_faq` (`id`, `category_id`, `question`, `answer`, `status`, `use_count`) VALUES
(REPLACE(UUID(), '-', ''), 1, '如何查看我的订单物流信息？',
 '您可以在"我的订单"中找到对应订单，点击"查看物流"即可实时跟踪包裹位置。',
 1, 0),
(REPLACE(UUID(), '-', ''), 1, '订单已发货但是没有物流更新怎么办？',
 '快递揽收后一般24小时内会有物流更新，如超时未更新建议联系客服核查。',
 1, 0),
(REPLACE(UUID(), '-', ''), 1, '商品什么时候发货？',
 '订单确认后24小时内发货，发货后可在订单详情查看物流信息。',
 1, 0),
(REPLACE(UUID(), '-', ''), 2, '支付失败怎么办？',
 '建议先检查银行卡余额是否充足，或更换支付方式重试。如仍失败可联系客服协助处理。',
 1, 0),
(REPLACE(UUID(), '-', ''), 2, '运费怎么算？',
 '单笔订单满99元包邮，不满99元收取8元基础运费。',
 1, 0),
(REPLACE(UUID(), '-', ''), 3, '如何申请退换货？',
 '在订单详情页点击"申请售后"，选择退换货原因并提交。审核通过后会安排上门取件。',
 1, 0),
(REPLACE(UUID(), '-', ''), 3, '退换货需要多长时间？',
 '签收后7天内可申请，审核1-3个工作日，退款在原支付方式退回后3-5个工作日到账。',
 1, 0),
(REPLACE(UUID(), '-', ''), 4, '如何修改密码？',
 '进入"我的→设置→账号安全→修改密码"，输入原密码和新密码即可完成修改。',
 1, 0);

-- ------------------------------------------------------------
-- 敏感词表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_sensitive_word`;
CREATE TABLE `t_sensitive_word` (
    `id`   BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `word` VARCHAR(255) NOT NULL COMMENT '敏感词内容',
    `type` VARCHAR(10)  NOT NULL COMMENT '类型：deny=黑名单, allow=白名单',
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';

INSERT INTO `t_sensitive_word` (`word`, `type`) VALUES
('假货', 'deny'),
('刷单', 'deny'),
('正品', 'allow'),
('退货', 'allow');

-- ------------------------------------------------------------
-- 订单表（供 Function Calling 订单查询使用）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no`     VARCHAR(32)  NOT NULL COMMENT '订单编号',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `amount`       DECIMAL(10,2) NOT NULL COMMENT '订单金额（元）',
    `status`       VARCHAR(20)  NOT NULL COMMENT '订单状态：待支付/待发货/配送中/已签收/已退货',
    `create_time`  DATETIME     NOT NULL COMMENT '下单时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

INSERT INTO `t_order` (`order_no`, `product_name`, `amount`, `status`, `create_time`) VALUES
('AR100', '智能手机', 2999.00, '已签收', '2026-06-15 10:30:00'),
('AR101', '蓝牙耳机', 399.00, '待发货', '2026-07-01 14:20:00'),
('AR102', '笔记本电脑', 5999.00, '配送中', '2026-06-28 09:15:00'),
('AR103', '智能手表', 1499.00, '待支付', '2026-07-10 20:45:00'),
('AR104', '机械键盘', 599.00, '已签收', '2026-05-20 16:00:00'),
('AR105', '无线鼠标', 199.00, '已退货', '2026-06-05 11:10:00');

-- ------------------------------------------------------------
-- 商品表（供 Function Calling 商品查询/价格查询/热销查询使用）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_product`;
CREATE TABLE `t_product` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `product_no`  VARCHAR(32)   NOT NULL COMMENT '商品编号',
    `name`        VARCHAR(100)  NOT NULL COMMENT '商品名称',
    `category`    VARCHAR(50)   NOT NULL COMMENT '商品分类',
    `price`       DECIMAL(10,2) NOT NULL COMMENT '商品价格（元）',
    `stock`       INT           NOT NULL DEFAULT 0 COMMENT '库存数量',
    `sales`       INT           NOT NULL DEFAULT 0 COMMENT '累计销量',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0-下架 1-在售',
    `create_time` DATETIME      NOT NULL COMMENT '上架时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_product_no` (`product_no`),
    INDEX `idx_category` (`category`),
    INDEX `idx_sales` (`sales`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

INSERT INTO `t_product` (`product_no`, `name`, `category`, `price`, `stock`, `sales`, `status`, `create_time`) VALUES
('P001', '智能手机',   '手机数码', 2999.00, 500,  1200, 1, '2026-03-01 09:00:00'),
('P002', '蓝牙耳机',   '手机数码', 399.00,  2000, 3500, 1, '2026-03-05 09:00:00'),
('P003', '笔记本电脑', '电脑办公', 5999.00, 300,  800,  1, '2026-03-10 09:00:00'),
('P004', '智能手表',   '智能穿戴', 1499.00, 800,  1500, 1, '2026-03-15 09:00:00'),
('P005', '机械键盘',   '电脑办公', 599.00,  1200, 2000, 1, '2026-03-20 09:00:00'),
('P006', '无线鼠标',   '电脑办公', 199.00,  3000, 5000, 1, '2026-03-25 09:00:00'),
('P007', '平板电脑',   '电脑办公', 3299.00, 400,  600,  1, '2026-04-01 09:00:00'),
('P008', '智能音箱',   '智能家居', 299.00,  1500, 2800, 1, '2026-04-05 09:00:00'),
('P009', '显示器',     '电脑办公', 1299.00, 700,  900,  1, '2026-04-10 09:00:00'),
('P010', '充电宝',     '手机数码', 149.00,  5000, 6000, 1, '2026-04-15 09:00:00'),
('P011', '高清摄像头', '电脑办公', 259.00,  900,  1800, 1, '2026-04-20 09:00:00'),
('P012', '无线路由器', '网络设备', 399.00,  600,  1600, 1, '2026-04-25 09:00:00');

-- ------------------------------------------------------------
-- 用户表（登录/注册）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名（登录账号）',
    `password`    VARCHAR(128) NOT NULL COMMENT '密码（SHA-256 加盐哈希）',
    `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `create_time` DATETIME     NOT NULL COMMENT '注册时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ------------------------------------------------------------
-- 购物车表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_cart`;
CREATE TABLE `t_cart` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
    `product_no`  VARCHAR(32) NOT NULL COMMENT '商品编号',
    `quantity`    INT         NOT NULL DEFAULT 1 COMMENT '数量',
    `create_time` DATETIME    NOT NULL COMMENT '加入时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product` (`user_id`, `product_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';
