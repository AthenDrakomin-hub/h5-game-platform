# H5 Game Platform - 全栈娱乐平台

> 一站式 H5 娱乐平台 monorepo，包含**前端用户端**、**后端服务**、**运营管理后台**、**Telegram Bot** 四个子项目。前端 Vue3 + Vant4，后端 Spring Boot 3 + MySQL 8，管理后台 Vue3 + Element Plus，Bot Telegraf + TypeScript。**190+ API 接口，支持 Telegram Mini App，可直接部署运行。**

## 项目结构

```
h5-game-platform/
├── frontend/              # 移动端 H5 用户端（Vue3 + Vant4）
│   ├── src/
│   │   ├── api/           # 6 个 API 模块，180+ 方法
│   │   ├── views/         # 75+ 页面
│   │   ├── router/        # 80+ 路由
│   │   └── stores/        # 4 个 Pinia store
│   └── package.json
│
├── backend/               # 后端服务（Spring Boot 3 + MyBatis-Plus + MySQL 8）
│   ├── src/main/java/com/h5/
│   │   ├── controller/    # 32 个控制器（用户端 24 + 管理端 7 + Bot 1）
│   │   ├── entity/        # 16 个实体类
│   │   └── mapper/        # 16 个 Mapper
│   ├── src/main/resources/db/schema.sql  # 建表脚本（18张表）+ 初始化数据
│   └── pom.xml
│
├── admin/                 # 运营管理后台（Vue3 + Element Plus + vue-pure-admin）
│   ├── src/
│   │   ├── api/admin.ts   # 50+ 管理端 API 方法
│   │   └── views/admin/   # 6 个管理页面
│   └── package.json
│
└── bot/                   # Telegram Bot（Telegraf + TypeScript）
    ├── src/
    │   ├── handlers/      # 8 个处理器（start/menu/wallet/game/promo/support/notify）
    │   ├── services/      # API 封装 + 用户会话管理
    │   ├── middlewares/   # 自动注册鉴权中间件
    │   └── keyboards/     # Inline Keyboard 定义
    └── package.json
```

## 技术栈总览

| 层级 | 前端用户端 | 后端服务 | 管理后台 | Telegram Bot |
|---|---|---|---|---|
| 框架 | Vue 3.5+ | Spring Boot 3.2.5 | Vue 3.4+ | Telegraf 4.16+ |
| 语言 | JavaScript | Java 17+ | TypeScript 5.3+ | TypeScript 5.4+ |
| UI 库 | Vant 4.9+ | - | Element Plus 2.5+ | Inline Keyboard |
| 状态管理 | Pinia 2.2+ | - | Pinia 2.1+ | 内存缓存 |
| 路由 | vue-router 4.6+ | - | vue-router 4.2+ | - |
| HTTP | Axios 1.7+ | - | Axios 1.6+ | Axios 1.7+ |
| Web 服务 | - | Spring MVC | - | Express 4.19+ |
| ORM/数据库 | - | MyBatis-Plus 3.5.5 + MySQL 8 | - | - |
| 鉴权 | - | JWT + BCrypt | - | Telegram ID 自动绑定 |
| 构建 | Vite 5.4+ | Maven 3.6+ | Vite 5.0+ | tsc 5.4+ |
| 基础框架 | - | - | vue-pure-admin 5.0+ | - |

## 功能模块

### 前端用户端（frontend/）
- **首页**：站点配置、Banner 轮播、系统公告、彩票分类、快捷入口
- **彩票游戏**：极速赛车(PK10)、时时彩(SSC)、PC蛋蛋(PC28)、六合彩(LHC)，实时开奖、走势图表、投注下单
- **娱乐城**：多平台接入(PG/PP/JDB/MG/AG/KM)，热门/最新/全部/大奖游戏，游戏进入、平台余额、转账
- **活动优惠**：活动列表/详情、每日签到、幸运转盘、亏损救援、邀请好友、任务中心、礼金中心
- **用户中心**（40+ 子页面）：充值/提现、交易记录、投注记录、盈亏报表、消息中心、VIP中心(11级)、银行卡管理、USDT钱包、代理中心、返水中心、余额宝、资金密码、意见反馈
- **Telegram Mini App**：SDK 集成、主题适配、返回按钮、主按钮、触觉反馈

### 后端服务（backend/）
- **用户端 API**（/api/wap/*，150+ 接口）：鉴权、首页、彩票、投注、娱乐城、活动、充值提现、订单、消息、用户、VIP、银行卡、钱包、代理、返水、余额宝
- **管理端 API**（/api/admin/*，40+ 接口）：仪表盘、用户管理、订单审核、游戏管理、活动管理、消息管理、站点配置
- **数据库**：15 张表，含初始化数据
- **核心业务**：充值审核（加余额+流水）、提现审核（冻结/退回）、投注（扣余额+流水）

### 管理后台（admin/）
- **仪表盘**：用户统计、充值/提现总额、投注统计、待审核数量
- **用户管理**：列表/搜索/筛选、启用禁用、调整余额、重置密码、设置VIP
- **订单管理**：充值审核（通过/拒绝）、提现审核（通过/拒绝）
- **游戏管理**：彩票游戏CRUD、开奖记录、娱乐城平台/游戏CRUD
- **活动管理**：活动CRUD、上下架
- **消息管理**：消息列表、发送全站消息、删除
- **站点配置**：站点参数、Banner管理、公告管理

## 快速开始

### 环境要求
- Node.js 18+（前端 + 管理后台）
- npm 9+
- JDK 17+（后端）
- Maven 3.6+（后端）
- MySQL 8.0+（后端）

### 1. 启动后端

```bash
cd backend

# 建库建表
mysql -u root -p < src/main/resources/db/schema.sql

# 修改数据库配置
# 编辑 src/main/resources/application.yml 中的数据库密码和 JWT secret

# 编译运行
mvn clean package -DskipTests
java -jar target/h5-backend-1.0.0.jar

# 后端运行在 http://localhost:8888
```

### 2. 启动前端用户端

```bash
cd frontend
npm install
npm run dev
# 前端运行在 http://localhost:5173
```

### 3. 启动管理后台

```bash
cd admin
npm install
npm run dev
# 管理后台运行在 http://localhost:8848
```

## 环境变量

### 前端（frontend/.env）
| 变量 | 默认值 | 说明 |
|---|---|---|
| `VITE_API_BASE` | `/api` | API 基础路径 |
| `VITE_TELEGRAM_ENABLED` | `false` | 启用 Telegram Mini App |

### 后端（backend/src/main/resources/application.yml）
| 配置项 | 默认值 | 说明 |
|---|---|---|
| `server.port` | 8888 | 服务端口 |
| `spring.datasource.url` | jdbc:mysql://localhost:3306/h5_game | 数据库连接 |
| `jwt.secret` | - | JWT 密钥（必须修改） |

### 管理后台（admin/.env）
| 变量 | 默认值 | 说明 |
|---|---|---|
| `VITE_API_BASE` | `/api` | API 基础路径 |
| `VITE_PORT` | 8848 | 开发端口 |

## API 对接规范

### 基础配置
- Base URL: `/api`
- 用户端路径: `/api/wap/*`（150+ 接口）
- 管理端路径: `/api/admin/*`（40+ 接口）
- 鉴权: `Authorization: Bearer <token>` + `X-User-Id: <userId>`（双 header）

### 响应结构
```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "time": 1700000000000
}
```

### 接口统计
| 模块 | 用户端 | 管理端 | 合计 |
|---|---|---|---|
| 鉴权 | 4 | - | 4 |
| 首页 | 6 | - | 6 |
| 彩票/投注 | 10 | - | 10 |
| 娱乐城 | 10 | - | 10 |
| 活动 | 23 | - | 23 |
| 充值提现 | 8 | - | 8 |
| 订单记录 | 4 | - | 4 |
| 消息 | 8 | - | 8 |
| 用户中心 | 40+ | - | 40+ |
| VIP | 3 | - | 3 |
| 银行卡/钱包 | 9 | - | 9 |
| 代理中心 | 20 | - | 20 |
| 返水 | 5 | - | 5 |
| 余额宝 | 6 | - | 6 |
| 仪表盘 | - | 3 | 3 |
| 用户管理 | - | 6 | 6 |
| 订单管理 | - | 6 | 6 |
| 游戏管理 | - | 8 | 8 |
| 活动管理 | - | 5 | 5 |
| 消息管理 | - | 3 | 3 |
| 站点配置 | - | 7 | 7 |
| **合计** | **150+** | **40+** | **190+** |

## 部署

### 后端部署（Systemd）
```bash
cd backend
mvn clean package -DskipTests
cp target/h5-backend-1.0.0.jar /opt/h5-game/backend/

# /etc/systemd/system/h5-backend.service
[Unit]
Description=H5 Game Backend
After=network.target mysql.service

[Service]
Type=simple
WorkingDirectory=/opt/h5-game/backend
ExecStart=/usr/bin/java -jar /opt/h5-game/backend/h5-backend-1.0.0.jar
Restart=always

[Install]
WantedBy=multi-user.target

systemctl daemon-reload
systemctl enable h5-backend
systemctl start h5-backend
```

### 前端部署（Nginx）
```bash
cd frontend
npm run build
cp -r dist/* /var/www/h5-game/frontend/
```

### 管理后台部署（Nginx）
```bash
cd admin
npm run build
cp -r dist/* /var/www/h5-game/admin/
```

### Nginx 配置示例
```nginx
# 前端用户端
server {
    listen 80;
    server_name h5.yourdomain.com;
    root /var/www/h5-game/frontend;
    index index.html;
    location / { try_files $uri $uri/ /index.html; }
    location /api/ {
        proxy_pass http://127.0.0.1:8888;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# 管理后台
server {
    listen 80;
    server_name admin.yourdomain.com;
    root /var/www/h5-game/admin;
    index index.html;
    location / { try_files $uri $uri/ /index.html; }
    location /api/ {
        proxy_pass http://127.0.0.1:8888;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 数据库设计（15 张表）

| 表名 | 说明 |
|---|---|
| `users` | 用户表（含余额、VIP、状态、试玩标记） |
| `user_wallets` | USDT 钱包地址 |
| `user_bank_cards` | 银行卡 |
| `orders` | 订单（充值/提现） |
| `transactions` | 交易流水 |
| `lotteries` | 彩票游戏 |
| `draw_results` | 开奖记录 |
| `bets` | 投注记录 |
| `casino_providers` | 娱乐城平台 |
| `casino_games` | 娱乐城游戏 |
| `promotions` | 活动 |
| `messages` | 消息（user_id=0 为全站） |
| `notices` | 公告 |
| `banners` | Banner |
| `site_config` | 站点配置 |

> 建表脚本：`backend/src/main/resources/db/schema.sql`（含初始化数据）

## 核心业务流程

### 充值流程
```
用户创建充值订单 → pending → 管理员审核通过 → 用户余额增加 → 交易流水
```

### 提现流程
```
用户创建提现 → 余额冻结 → pending → 管理员通过 → 扣冻结
                                      → 管理员拒绝 → 退回余额 + 解冻
```

### 投注流程
```
用户提交投注 → 扣减余额 → 生成投注记录 → 交易流水 → 开奖后更新中奖金额
```

## 子项目详细文档

- [前端用户端 README](frontend/README.md)
- [后端服务 README](backend/README.md)
- [管理后台 README](admin/README.md)

## 许可证

MIT
