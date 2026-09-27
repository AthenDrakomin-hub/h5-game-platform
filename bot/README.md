# NOVA Telegram Bot

NOVA 新星娱乐官方 Telegram 机器人，基于 Telegraf + TypeScript 开发。

## 功能特性

- 🚀 **Mini App 入口**：一键打开 H5 游戏大厅
- 💰 **钱包管理**：余额查询、充值、提现、交易明细
- 🎮 **游戏快捷入口**：极速赛车、幸运飞艇、时时彩、娱乐城
- 🎁 **活动中心**：每日签到、优惠活动、邀请返利、VIP 特权
- 💬 **智能客服**：FAQ 自动回复 + 人工客服转接
- 📢 **通知推送**：充值到账、提现到账、中奖通知、活动推送（后端回调）
- 🔐 **自动注册**：Telegram 用户 ID 自动绑定平台账号，无需手动注册

## 技术栈

- **运行时**：Node.js 18+
- **框架**：Telegraf 4.x
- **语言**：TypeScript 5.x
- **Web 服务**：Express 4.x（Webhook + 内部通知 API）
- **HTTP 客户端**：Axios

## 目录结构

```
bot/
├── src/
│   ├── index.ts              # 入口，Bot 初始化 + Webhook/Polling
│   ├── config/
│   │   └── index.ts          # 环境变量配置
│   ├── handlers/
│   │   ├── start.ts          # /start 欢迎 + 自动注册
│   │   ├── menu.ts           # 主菜单 + 各子菜单导航
│   │   ├── wallet.ts         # 余额/充值/提现/交易明细
│   │   ├── game.ts           # 游戏快捷入口
│   │   ├── promo.ts          # 签到/活动/邀请/VIP
│   │   ├── support.ts        # FAQ 智能客服 + 人工转接
│   │   └── notify.ts         # 通知推送服务 + 内部 API
│   ├── middlewares/
│   │   └── auth.ts           # 自动注册/登录鉴权中间件
│   ├── services/
│   │   ├── api.ts            # 后端 API 封装
│   │   └── user.ts           # 用户会话管理
│   ├── keyboards/
│   │   └── main.ts           # Inline Keyboard 定义
│   └── utils/
│       └── format.ts         # 消息格式化工具
├── package.json
├── tsconfig.json
├── .env.example
└── README.md
```

## 快速开始

### 1. 安装依赖

```bash
cd bot
npm install
```

### 2. 配置环境变量

```bash
cp .env.example .env
```

编辑 `.env` 文件，填入以下关键配置：

```env
# 必填
BOT_TOKEN=你的Bot Token
MINI_APP_URL=https://你的H5域名.com
API_BASE_URL=http://127.0.0.1:8888/api

# 开发模式用 polling，生产用 webhook
BOT_MODE=polling

# webhook 模式下必填
WEBHOOK_URL=https://你的域名.com/bot/webhook
WEBHOOK_PORT=3001
WEBHOOK_SECRET=随机字符串

# 后端调用通知接口的鉴权密钥
BOT_INTERNAL_SECRET=你的内部密钥
```

### 3. 开发运行

```bash
npm run dev
```

### 4. 生产构建与运行

```bash
npm run build
npm start
```

## 运行模式

### Long Polling（开发/调试）

- 无需公网域名和 SSL
- `BOT_MODE=polling`
- Bot 主动轮询 Telegram 服务器获取更新
- 通知 API 端口 = `WEBHOOK_PORT + 1`（默认 3002）

### Webhook（生产推荐）

- 需要公网域名 + HTTPS
- `BOT_MODE=webhook`
- Telegram 主动推送更新到你的服务器
- Nginx 反向代理配置示例：

```nginx
server {
    listen 443 ssl;
    server_name your-domain.com;

    location /bot/webhook {
        proxy_pass http://127.0.0.1:3001;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /api/bot/ {
        proxy_pass http://127.0.0.1:3001;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 后端对接

### Bot 调用后端 API

Bot 通过标准鉴权头调用后端 `/api/wap/*` 接口：

```
Authorization: Bearer <token>
X-User-Id: <userId>
```

### 后端调用 Bot 通知 API

后端在事件触发时（充值到账、提现到账、中奖等）调用 Bot 内部接口：

```
POST http://127.0.0.1:3001/api/bot/notify/send
Headers:
  X-Bot-Secret: <BOT_INTERNAL_SECRET>
  Content-Type: application/json

Body:
{
  "telegramId": 123456789,
  "type": "recharge",
  "data": {
    "amount": 500,
    "payType": "银行卡",
    "balance": 1500
  }
}
```

**支持的通知类型**：

| type | 说明 | data 字段 |
|---|---|---|
| `recharge` | 充值到账 | amount, payType, balance |
| `withdraw_apply` | 提现申请提交 | amount, accountType, orderNo |
| `withdraw_success` | 提现到账 | amount, account, balance |
| `win` | 中奖通知 | gameName, issue, amount, orderNo |
| `signin_remind` | 签到提醒 | （无） |
| `promo` | 活动推送 | title, content, actionUrl |
| `system` | 系统公告 | content |

### 后端需新增的接口

| 接口 | 说明 |
|---|---|
| `POST /api/bot/auth/register` | Telegram 用户自动注册/登录，返回 token + userId |
| `GET /api/wap/promo/signin/status` | 签到状态查询 |
| `POST /api/wap/promo/signin` | 执行签到 |
| `GET /api/wap/promo/list` | 活动列表 |

## 数据库扩展

后端需新增 3 张表（详见项目根目录 `backend/src/main/resources/db/schema.sql`）：

- `bot_telegram_user`：Telegram 用户绑定表
- `bot_notification`：Bot 通知记录表
- `bot_support_session`：客服会话表

## 部署（PM2）

```bash
npm install -g pm2

# 启动
pm2 start dist/index.js --name nova-bot

# 查看状态
pm2 status

# 查看日志
pm2 logs nova-bot

# 开机自启
pm2 startup
pm2 save
```

## 部署（systemd）

```ini
# /etc/systemd/system/nova-bot.service
[Unit]
Description=NOVA Telegram Bot
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/nova/bot
EnvironmentFile=/opt/nova/bot/.env
ExecStart=/usr/bin/node dist/index.js
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

```bash
systemctl enable nova-bot
systemctl start nova-bot
systemctl status nova-bot
```

## Bot 命令列表

| 命令 | 说明 |
|---|---|
| `/start` | 开始使用 / 进入游戏 |
| `/menu` | 主菜单 |
| `/balance` | 查询余额 |
| `/recharge` | 充值 |
| `/withdraw` | 提现 |
| `/signin` | 每日签到 |
| `/promo` | 活动中心 |
| `/support` | 在线客服 |
| `/rules` | 游戏规则 |

## 安全注意事项

1. **Bot Token 保密**：不要提交到 Git，通过环境变量注入
2. **内部通知 API 鉴权**：`BOT_INTERNAL_SECRET` 必须设置，且只监听内网
3. **Webhook Secret**：生产环境必须设置 `WEBHOOK_SECRET`
4. **用户数据**：不要在日志中输出用户 token 等敏感信息
5. **限流**：广播通知已内置限流（30条/秒），避免被 Telegram 封禁

## License

MIT
