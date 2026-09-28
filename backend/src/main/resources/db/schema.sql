-- H5 Game 后端数据库建表脚本
-- 数据库: h5_game
-- 字符集: utf8mb4

CREATE DATABASE IF NOT EXISTS h5_game DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE h5_game;

-- ============================================================
-- 1. 用户表
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt)',
    nickname VARCHAR(50) DEFAULT '' COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT '' COMMENT '头像',
    phone VARCHAR(20) DEFAULT '' COMMENT '手机号',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    balance DECIMAL(18,2) DEFAULT 0.00 COMMENT '余额',
    frozen_balance DECIMAL(18,2) DEFAULT 0.00 COMMENT '冻结余额',
    vip_level INT DEFAULT 1 COMMENT 'VIP等级',
    is_trial TINYINT DEFAULT 0 COMMENT '是否试玩账号 0否 1是',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1正常',
    role VARCHAR(20) DEFAULT 'user' COMMENT '角色 user普通用户 admin管理员 superadmin超级管理员',
    fund_password VARCHAR(100) DEFAULT '' COMMENT '资金密码',
    invite_code VARCHAR(20) DEFAULT '' COMMENT '邀请码',
    invited_by BIGINT DEFAULT NULL COMMENT '邀请人ID',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT '' COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    version INT DEFAULT 0 COMMENT '乐观锁版本号',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    UNIQUE KEY uk_invite_code (invite_code),
    INDEX idx_invited_by (invited_by),
    INDEX idx_role (role),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 用户钱包地址（USDT）
-- ============================================================
CREATE TABLE IF NOT EXISTS user_wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    chain VARCHAR(20) DEFAULT 'TRC20' COMMENT '链类型 TRC20/ERC20',
    address VARCHAR(100) NOT NULL COMMENT '钱包地址',
    label VARCHAR(50) DEFAULT '' COMMENT '备注标签',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认 0否 1是',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户钱包地址';

-- ============================================================
-- 3. 用户银行卡
-- ============================================================
CREATE TABLE IF NOT EXISTS user_bank_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    bank_name VARCHAR(50) NOT NULL COMMENT '银行名称',
    branch_name VARCHAR(100) DEFAULT '' COMMENT '开户行支行',
    card_number VARCHAR(50) NOT NULL COMMENT '卡号',
    card_holder VARCHAR(50) NOT NULL COMMENT '持卡人姓名',
    phone VARCHAR(20) DEFAULT '' COMMENT '预留手机号',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户银行卡';

-- ============================================================
-- 4. 订单表（充值/提现）
-- ============================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(20) NOT NULL COMMENT '类型 recharge充值 withdraw提现',
    amount DECIMAL(18,2) NOT NULL COMMENT '金额',
    fee DECIMAL(18,2) DEFAULT 0.00 COMMENT '手续费',
    actual_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '实际到账金额',
    method VARCHAR(50) DEFAULT '' COMMENT '支付方式 bank/usdt/...',
    method_name VARCHAR(100) DEFAULT '' COMMENT '支付方式名称',
    pay_account VARCHAR(255) DEFAULT '' COMMENT '收款账户',
    pay_qrcode VARCHAR(255) DEFAULT '' COMMENT '收款二维码',
    user_account VARCHAR(255) DEFAULT '' COMMENT '用户付款账户',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态 pending处理中 success成功 failed失败 cancelled已取消',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    admin_remark VARCHAR(500) DEFAULT '' COMMENT '管理员备注',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    audit_by BIGINT DEFAULT NULL COMMENT '审核人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user_id_status (user_id, status),
    INDEX idx_type_status (type, status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================================
-- 5. 交易流水
-- ============================================================
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    type VARCHAR(30) NOT NULL COMMENT '类型 recharge/withdraw/bet/win/transfer/bonus',
    amount DECIMAL(18,2) NOT NULL COMMENT '金额(正数增加,负数减少)',
    balance_before DECIMAL(18,2) DEFAULT 0.00 COMMENT '变动前余额',
    balance_after DECIMAL(18,2) DEFAULT 0.00 COMMENT '变动后余额',
    ref_id BIGINT DEFAULT NULL COMMENT '关联ID(订单ID/投注ID)',
    ref_no VARCHAR(50) DEFAULT '' COMMENT '关联单号',
    description VARCHAR(255) DEFAULT '' COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_user_id_type (user_id, type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易流水';

-- ============================================================
-- 6. 彩种配置
-- ============================================================
CREATE TABLE IF NOT EXISTS lotteries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '彩种名称',
    code VARCHAR(30) NOT NULL UNIQUE COMMENT '彩种代码 jsdd/jspk10/jsssc/...',
    category_code VARCHAR(30) DEFAULT '' COMMENT '分类代码 series28/alliance/ssc/marble/dw',
    category_name VARCHAR(50) DEFAULT '' COMMENT '分类名称',
    icon VARCHAR(255) DEFAULT '' COMMENT '图标',
    draw_interval INT DEFAULT 60 COMMENT '开奖间隔(秒)',
    close_time INT DEFAULT 15 COMMENT '封盘时间(秒)',
    status TINYINT DEFAULT 1 COMMENT '状态 0下架 1上架',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_code (code),
    INDEX idx_category (category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='彩种配置';

-- ============================================================
-- 7. 开奖记录
-- ============================================================
CREATE TABLE IF NOT EXISTS draw_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lottery_code VARCHAR(30) NOT NULL COMMENT '彩种代码',
    period VARCHAR(30) NOT NULL COMMENT '期号',
    numbers VARCHAR(100) NOT NULL COMMENT '开奖号码(逗号分隔)',
    draw_time DATETIME NOT NULL COMMENT '开奖时间',
    status TINYINT DEFAULT 1 COMMENT '状态 0未开奖 1已开奖',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_lottery_period (lottery_code, period),
    INDEX idx_lottery_code (lottery_code),
    INDEX idx_draw_time (draw_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开奖记录';

-- ============================================================
-- 8. 投注记录
-- ============================================================
CREATE TABLE IF NOT EXISTS bets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bet_no VARCHAR(50) NOT NULL UNIQUE COMMENT '投注单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    lottery_code VARCHAR(30) NOT NULL COMMENT '彩种代码',
    period VARCHAR(30) NOT NULL COMMENT '期号',
    play_type VARCHAR(30) DEFAULT '' COMMENT '玩法',
    numbers VARCHAR(255) DEFAULT '' COMMENT '投注号码',
    amount DECIMAL(18,2) NOT NULL COMMENT '投注金额',
    odds DECIMAL(10,4) DEFAULT 1.0000 COMMENT '赔率',
    win_amount DECIMAL(18,2) DEFAULT 0.00 COMMENT '中奖金额',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态 pending待开奖 win中奖 lose未中 cancelled已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_lottery_period (lottery_code, period),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投注记录';

-- ============================================================
-- 9. 娱乐城平台
-- ============================================================
CREATE TABLE IF NOT EXISTS casino_providers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '平台名称',
    code VARCHAR(30) NOT NULL UNIQUE COMMENT '平台代码',
    icon VARCHAR(255) DEFAULT '' COMMENT '图标',
    status TINYINT DEFAULT 1 COMMENT '状态',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='娱乐城平台';

-- ============================================================
-- 10. 娱乐城游戏
-- ============================================================
CREATE TABLE IF NOT EXISTS casino_games (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '游戏名称',
    game_code VARCHAR(50) NOT NULL COMMENT '游戏代码',
    provider_code VARCHAR(30) NOT NULL COMMENT '平台代码',
    provider_name VARCHAR(50) DEFAULT '' COMMENT '平台名称',
    category VARCHAR(30) DEFAULT '' COMMENT '分类 slot/live/fish/...',
    icon VARCHAR(255) DEFAULT '' COMMENT '图标',
    enter_url VARCHAR(500) DEFAULT '' COMMENT '进入URL(模板)',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热门',
    is_new TINYINT DEFAULT 0 COMMENT '是否最新',
    status TINYINT DEFAULT 1 COMMENT '状态',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_provider (provider_code),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='娱乐城游戏';

-- ============================================================
-- 11. 活动
-- ============================================================
CREATE TABLE IF NOT EXISTS promotions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '活动标题',
    category VARCHAR(30) DEFAULT '' COMMENT '分类 newbie/daily/deposit/vip/...',
    category_name VARCHAR(50) DEFAULT '' COMMENT '分类名称',
    image VARCHAR(255) DEFAULT '' COMMENT '活动图片',
    description TEXT COMMENT '活动描述',
    content TEXT COMMENT '活动详情(富文本)',
    status TINYINT DEFAULT 1 COMMENT '状态 0下架 1进行中',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动';

-- ============================================================
-- 12. 消息
-- ============================================================
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID(0=全体)',
    category VARCHAR(30) DEFAULT 'system' COMMENT '分类 system/announcement/promotion',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读 0未读 1已读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    INDEX idx_is_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息';

-- ============================================================
-- 13. 公告
-- ============================================================
CREATE TABLE IF NOT EXISTS notices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    type VARCHAR(20) DEFAULT 'notice' COMMENT '类型 notice公告 popup弹窗',
    status TINYINT DEFAULT 1 COMMENT '状态',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告';

-- ============================================================
-- 14. Banner
-- ============================================================
CREATE TABLE IF NOT EXISTS banners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) DEFAULT '' COMMENT '标题',
    image VARCHAR(255) NOT NULL COMMENT '图片',
    link VARCHAR(255) DEFAULT '' COMMENT '跳转链接',
    position VARCHAR(20) DEFAULT 'home' COMMENT '位置 home/casino/...',
    status TINYINT DEFAULT 1 COMMENT '状态',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner';

-- ============================================================
-- 15. 站点配置（KV）
-- ============================================================
CREATE TABLE IF NOT EXISTS site_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) DEFAULT '' COMMENT '说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站点配置';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 站点配置
INSERT INTO site_config (config_key, config_value, description) VALUES
('site_name', 'NOVA 新星娱乐', '站点名称'),
('customer_service_url', 'https://t.me/cs_bot', '客服链接'),
('telegram_group_url', 'https://t.me/h5_group', 'Telegram群组'),
('recharge_min_amount', '100', '充值最小金额'),
('recharge_max_amount', '50000', '充值最大金额'),
('withdraw_min_amount', '100', '提现最小金额'),
('withdraw_fee_rate', '0.01', '提现手续费率'),
('trial_balance', '2000', '试玩账号初始余额');

-- 彩种配置
INSERT INTO lotteries (name, code, category_code, category_name, draw_interval, close_time, sort, icon) VALUES
('极速PC28', 'jsdd', 'series28', 'PC28', 75, 15, 1, '/uploads/lottery/pc28.svg'),
('极速赛车', 'jspk10', 'alliance', '赛车', 60, 10, 2, '/uploads/lottery/pk10.svg'),
('极速时时彩', 'jsssc', 'ssc', '时时彩', 60, 10, 3, '/uploads/lottery/ssc.svg'),
('极速六合彩', 'happy8lhc', 'marble', '六合彩', 300, 30, 4, '/uploads/lottery/lhc.svg'),
('极速飞艇', 'jsft', 'alliance', '飞艇', 60, 10, 5, '/uploads/lottery/pk10.svg'),
('极速运动会', 'jsydh', 'dw', '运动会', 75, 15, 6, '/uploads/lottery/ssc.svg');

-- 娱乐城平台
INSERT INTO casino_providers (name, code, sort, icon) VALUES
('PG电子', 'pg', 1, '/uploads/casino/pg.svg'),
('PP电子', 'pp', 2, '/uploads/casino/pp.svg'),
('JDB捕鱼', 'jdb', 3, '/uploads/casino/jdb.svg'),
('AG真人', 'ag', 4, '/uploads/casino/ag.svg'),
('MG电子', 'mg', 5, '/uploads/casino/mg.svg'),
('KM电子', 'km', 6, '/uploads/casino/km.svg');

-- 娱乐城游戏
INSERT INTO casino_games (name, game_code, provider_code, provider_name, category, icon, is_hot, is_new, status, sort) VALUES
('老虎机经典', 'slot_classic', 'pg', 'PG电子', 'slot', '/uploads/casino/game1.svg', 1, 0, 1, 1),
('百家乐', 'baccarat', 'ag', 'AG真人', 'live', '/uploads/casino/game2.svg', 1, 0, 1, 2),
('轮盘', 'roulette', 'mg', 'MG电子', 'table', '/uploads/casino/game3.svg', 0, 1, 1, 3),
('骰宝', 'sicbo', 'pp', 'PP电子', 'table', '/uploads/casino/game4.svg', 0, 0, 1, 4),
('德州扑克', 'poker', 'pg', 'PG电子', 'card', '/uploads/casino/game5.svg', 1, 0, 1, 5),
('捕鱼达人', 'fishing', 'jdb', 'JDB捕鱼', 'fish', '/uploads/casino/game6.svg', 0, 1, 1, 6),
('麻将', 'mahjong', 'km', 'KM电子', 'card', '/uploads/casino/game7.svg', 0, 0, 1, 7),
('棋牌合集', 'chess', 'pp', 'PP电子', 'card', '/uploads/casino/game8.svg', 0, 0, 1, 8),
('电子竞技', 'esports', 'mg', 'MG电子', 'esport', '/uploads/casino/game9.svg', 1, 1, 1, 9),
('真人视讯', 'live_dealer', 'ag', 'AG真人', 'live', '/uploads/casino/game10.svg', 0, 0, 1, 10),
('彩票游戏', 'lottery_game', 'pg', 'PG电子', 'lottery', '/uploads/casino/game11.svg', 0, 0, 1, 11),
('竞技天地', 'arena', 'km', 'KM电子', 'esport', '/uploads/casino/game12.svg', 0, 1, 1, 12);

-- 活动
INSERT INTO promotions (title, category, category_name, description, status, sort, image) VALUES
('新人注册送88元彩金', 'newbie', '新人专享', '新用户注册即送88元彩金，可用于所有游戏。', 1, 1, '/uploads/promo/promo1.svg'),
('每日签到领红包', 'daily', '每日活动', '每日签到可领取随机红包，连续签到奖励翻倍。', 1, 2, '/uploads/promo/promo2.svg'),
('首充100%赠送', 'deposit', '充值优惠', '首次充值享受100%赠送，最高赠送888元。', 1, 3, '/uploads/promo/promo3.svg'),
('VIP专属返水', 'vip', 'VIP特权', 'VIP会员享受高额返水，最高可达1.5%。', 1, 4, '/uploads/promo/promo4.svg');

-- 公告
INSERT INTO notices (title, content, type, sort) VALUES
('欢迎来到H5 Game', '欢迎来到H5 Game，祝您游戏愉快！', 'notice', 1),
('系统维护通知', '系统将于每周三凌晨2:00-4:00进行维护，请提前做好准备。', 'notice', 2);

-- Banner
INSERT INTO banners (title, image, link, position, sort) VALUES
('新人专享', '/uploads/banners/banner1.svg', '/promo/detail/1', 'home', 1),
('每日签到', '/uploads/banners/banner2.svg', '/promo/detail/2', 'home', 2),
('娱乐城狂欢', '/uploads/banners/banner3.svg', '/casino', 'home', 3);

-- ============================================================
-- Telegram Bot 相关表
-- ============================================================

-- Telegram 用户绑定表
CREATE TABLE IF NOT EXISTS bot_telegram_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    telegram_id BIGINT NOT NULL UNIQUE COMMENT 'Telegram用户ID',
    user_id BIGINT NOT NULL COMMENT '关联平台用户ID',
    username VARCHAR(64) DEFAULT '' COMMENT 'Telegram用户名',
    first_name VARCHAR(64) DEFAULT '' COMMENT '名',
    last_name VARCHAR(64) DEFAULT '' COMMENT '姓',
    photo_url VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    language_code VARCHAR(10) DEFAULT '' COMMENT '语言',
    is_premium TINYINT DEFAULT 0 COMMENT '是否Premium用户',
    invite_code VARCHAR(32) DEFAULT '' COMMENT '注册时使用的邀请码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_telegram_id (telegram_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Telegram用户绑定表';

-- Bot 通知记录表
CREATE TABLE IF NOT EXISTS bot_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    telegram_id BIGINT NOT NULL COMMENT 'Telegram用户ID',
    type VARCHAR(32) NOT NULL COMMENT '通知类型 recharge/withdraw/win/signin/promo/system',
    title VARCHAR(128) DEFAULT '' COMMENT '标题',
    content TEXT COMMENT '内容',
    data JSON COMMENT '附加数据',
    status TINYINT DEFAULT 0 COMMENT '0待发送 1已发送 2失败',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    sent_at DATETIME DEFAULT NULL COMMENT '发送时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status),
    INDEX idx_type (type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bot通知记录表';

-- Bot 客服会话表
CREATE TABLE IF NOT EXISTS bot_support_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    telegram_id BIGINT NOT NULL COMMENT 'Telegram用户ID',
    status TINYINT DEFAULT 0 COMMENT '0待处理 1处理中 2已关闭',
    admin_id BIGINT DEFAULT NULL COMMENT '处理客服ID',
    last_message TEXT COMMENT '最后一条消息',
    last_message_at DATETIME DEFAULT NULL COMMENT '最后消息时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    closed_at DATETIME DEFAULT NULL,
    INDEX idx_status (status),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bot客服会话表';

-- ============================================================
-- 16. 支付方式表
-- ============================================================
CREATE TABLE IF NOT EXISTS payment_methods (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '支付方式名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '支付方式编码 alipay/wechat/bank/usdt_trc20/usdt_erc20/...',
    type VARCHAR(20) NOT NULL COMMENT '类型 cny法币 crypto数字货币',
    category VARCHAR(20) DEFAULT 'recharge' COMMENT '适用场景 recharge充值 withdraw提现 both两者',
    icon VARCHAR(255) DEFAULT '' COMMENT '图标URL',
    min_amount DECIMAL(18,2) DEFAULT 100.00 COMMENT '最小金额',
    max_amount DECIMAL(18,2) DEFAULT 50000.00 COMMENT '最大金额',
    fee_rate DECIMAL(10,4) DEFAULT 0.0000 COMMENT '手续费率',
    fixed_fee DECIMAL(18,2) DEFAULT 0.00 COMMENT '固定手续费',
    address VARCHAR(500) DEFAULT '' COMMENT '收款地址(USDT地址/银行卡号/支付宝账号)',
    address_name VARCHAR(100) DEFAULT '' COMMENT '收款人姓名/开户行',
    qrcode VARCHAR(255) DEFAULT '' COMMENT '收款二维码图片',
    auto_confirm TINYINT DEFAULT 0 COMMENT '是否自动确认到账 0人工 1自动(USDT链上监听)',
    chain VARCHAR(20) DEFAULT '' COMMENT '区块链类型 TRC20/ERC20(仅crypto)',
    api_config TEXT COMMENT 'API配置JSON(三方支付网关配置)',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_type_status (type, status),
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付方式表';

-- ============================================================
-- 17. 管理员操作日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS admin_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL COMMENT '操作管理员ID',
    admin_name VARCHAR(50) DEFAULT '' COMMENT '管理员用户名',
    action VARCHAR(50) NOT NULL COMMENT '操作类型 login/approve_recharge/reject_recharge/approve_withdraw/reject_withdraw/adjust_balance/disable_user/enable_user/set_vip/...',
    target_type VARCHAR(30) DEFAULT '' COMMENT '操作对象类型 user/order/bet/promotion/...',
    target_id BIGINT DEFAULT NULL COMMENT '操作对象ID',
    before_data TEXT COMMENT '变更前数据JSON',
    after_data TEXT COMMENT '变更后数据JSON',
    ip VARCHAR(50) DEFAULT '' COMMENT '操作IP',
    user_agent VARCHAR(500) DEFAULT '' COMMENT 'User-Agent',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_admin_id (admin_id),
    INDEX idx_action (action),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- ============================================================
-- 18. USDT充值地址池（用于自动到账匹配）
-- ============================================================
CREATE TABLE IF NOT EXISTS usdt_address_pool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    address VARCHAR(100) NOT NULL UNIQUE COMMENT 'USDT收款地址',
    chain VARCHAR(20) DEFAULT 'TRC20' COMMENT '链类型',
    label VARCHAR(50) DEFAULT '' COMMENT '标签',
    status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
    last_used_time DATETIME DEFAULT NULL COMMENT '最后使用时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_chain_status (chain, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='USDT充值地址池';

-- ============================================================
-- 支付方式初始化数据
-- ============================================================
INSERT INTO payment_methods (name, code, type, category, icon, min_amount, max_amount, fee_rate, address, address_name, qrcode, auto_confirm, chain, status, sort) VALUES
('支付宝', 'alipay', 'cny', 'recharge', '/uploads/icons/alipay.svg', 100.00, 50000.00, 0.0000, '', '', '/uploads/qrcode/alipay_qr.svg', 0, '', 1, 1),
('微信支付', 'wechat', 'cny', 'recharge', '/uploads/icons/wechat.svg', 100.00, 50000.00, 0.0000, '', '', '/uploads/qrcode/wechat_qr.svg', 0, '', 1, 2),
('银行卡转账', 'bank', 'cny', 'both', '/uploads/icons/bank.svg', 100.00, 50000.00, 0.0100, '', '', '', 0, '', 1, 3),
('USDT-TRC20', 'usdt_trc20', 'crypto', 'both', '/uploads/icons/usdt.svg', 100.00, 500000.00, 0.0050, 'TExxxxxxxxxxxxxxxxxxxxxxxxxxxxx', '', '/uploads/qrcode/usdt_qr.svg', 1, 'TRC20', 1, 4),
('USDT-ERC20', 'usdt_erc20', 'crypto', 'both', '/uploads/icons/usdt.svg', 100.00, 500000.00, 0.0050, '0xExxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', '', '/uploads/qrcode/usdt_qr.svg', 1, 'ERC20', 1, 5);
