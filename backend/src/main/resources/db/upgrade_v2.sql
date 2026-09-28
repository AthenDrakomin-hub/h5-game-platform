-- ============================================================
-- H5 Game 数据库升级脚本 v1 → v2
-- 执行前请备份数据库！
-- 适用：已执行过原始 schema.sql 的数据库
-- ============================================================

USE h5_game;

-- 1. users 表增加 role 字段
ALTER TABLE users ADD COLUMN role VARCHAR(20) DEFAULT 'user' COMMENT '角色 user/admin/superadmin' AFTER status;
ALTER TABLE users ADD INDEX idx_role (role);

-- 2. users 表增加 version 乐观锁字段
ALTER TABLE users ADD COLUMN version INT DEFAULT 0 COMMENT '乐观锁版本号' AFTER update_time;

-- 3. 支付方式表
CREATE TABLE IF NOT EXISTS payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '支付方式名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '支付方式编码',
    type VARCHAR(20) NOT NULL COMMENT '类型 cny/crypto',
    category VARCHAR(20) DEFAULT 'recharge' COMMENT 'recharge/withdraw/both',
    icon VARCHAR(255) DEFAULT '',
    min_amount DECIMAL(18,2) DEFAULT 100.00,
    max_amount DECIMAL(18,2) DEFAULT 50000.00,
    fee_rate DECIMAL(10,4) DEFAULT 0.0000,
    fixed_fee DECIMAL(18,2) DEFAULT 0.00,
    address VARCHAR(500) DEFAULT '',
    address_name VARCHAR(100) DEFAULT '',
    qrcode VARCHAR(255) DEFAULT '',
    auto_confirm TINYINT DEFAULT 0,
    chain VARCHAR(20) DEFAULT '',
    api_config TEXT,
    status TINYINT DEFAULT 1,
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_type_status (type, status),
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付方式表';

-- 4. 管理员操作日志表
CREATE TABLE IF NOT EXISTS admin_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    admin_name VARCHAR(50) DEFAULT '',
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(30) DEFAULT '',
    target_id BIGINT DEFAULT NULL,
    before_data TEXT,
    after_data TEXT,
    ip VARCHAR(50) DEFAULT '',
    user_agent VARCHAR(500) DEFAULT '',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_id (admin_id),
    INDEX idx_action (action),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- 5. USDT充值地址池
CREATE TABLE IF NOT EXISTS usdt_address_pool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    address VARCHAR(100) NOT NULL UNIQUE,
    chain VARCHAR(20) DEFAULT 'TRC20',
    label VARCHAR(50) DEFAULT '',
    status TINYINT DEFAULT 1,
    last_used_time DATETIME DEFAULT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_chain_status (chain, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='USDT充值地址池';

-- 6. 初始化支付方式数据
INSERT INTO payment_methods (name, code, type, category, icon, min_amount, max_amount, fee_rate, address, auto_confirm, chain, status, sort) VALUES
('支付宝', 'alipay', 'cny', 'recharge', '/uploads/icons/alipay.svg', 100.00, 50000.00, 0.0000, '', 0, '', 1, 1),
('微信支付', 'wechat', 'cny', 'recharge', '/uploads/icons/wechat.svg', 100.00, 50000.00, 0.0000, '', 0, '', 1, 2),
('银行卡转账', 'bank', 'cny', 'both', '/uploads/icons/bank.svg', 100.00, 50000.00, 0.0100, '', 0, '', 1, 3),
('USDT-TRC20', 'usdt_trc20', 'crypto', 'both', '/uploads/icons/usdt.svg', 100.00, 500000.00, 0.0050, 'TExxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 1, 'TRC20', 1, 4),
('USDT-ERC20', 'usdt_erc20', 'crypto', 'both', '/uploads/icons/usdt.svg', 100.00, 500000.00, 0.0050, '0xExxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 1, 'ERC20', 1, 5);

-- ============================================================
-- 索引优化（v2.1）
-- ============================================================
-- users: invite_code改唯一索引, 新增invited_by索引
ALTER TABLE users DROP INDEX IF EXISTS idx_invite_code;
ALTER TABLE users ADD UNIQUE KEY uk_invite_code (invite_code);
ALTER TABLE users ADD INDEX IF NOT EXISTS idx_invited_by (invited_by);

-- orders: order_no改唯一索引, 新增user_id+status复合索引
ALTER TABLE orders DROP INDEX IF EXISTS idx_order_no;
ALTER TABLE orders ADD UNIQUE KEY uk_order_no (order_no);
ALTER TABLE orders ADD INDEX IF NOT EXISTS idx_user_id_status (user_id, status);

-- transactions: type索引改为user_id+type复合索引
ALTER TABLE transactions DROP INDEX IF EXISTS idx_type;
ALTER TABLE transactions ADD INDEX IF NOT EXISTS idx_user_id_type (user_id, type);

-- bets: 新增status索引
ALTER TABLE bets ADD INDEX IF NOT EXISTS idx_status (status);

-- 升级完成
SELECT 'v2.1 upgrade completed' AS status;
