# H5 Game - 移动端 H5 娱乐应用

> 基于 Vue 3 + Vant 4 + Vite 的移动端 H5 单页应用，包含彩票游戏、娱乐城、活动优惠、用户中心等完整业务模块。**已对接自研后端 API（190+ 接口），支持 Telegram Mini App，可直接部署运行。**

## 项目概览

| 项目 | 仓库 | 说明 |
|---|---|---|
| H5 用户端 | [h5-clone](https://github.com/AthenDrakomin-hub/h5-clone) | 本仓库，移动端用户端 |
| 管理后台 | [h5-admin-panel](https://github.com/AthenDrakomin-hub/h5-admin-panel) | Vue3 + Element Plus 管理系统 |
| 后端服务 | [h5-backend](https://github.com/AthenDrakomin-hub/h5-backend) | Spring Boot 3 + MySQL 8 |

## 技术栈

| 层级 | 技术 | 版本 |
|---|---|---|
| 框架 | Vue | 3.5+ |
| UI 库 | Vant | 4.9+ |
| 路由 | vue-router | 4.6+ (Hash 模式) |
| 状态管理 | Pinia | 2.2+ |
| HTTP | Axios | 1.7+ |
| 构建 | Vite | 5.4+ |
| 日期 | dayjs | 1.11+ |
| Telegram | telegram-web-app.js | 官方 SDK |

## 功能模块

### 首页
- 站点配置、Banner 轮播、系统公告
- 彩票分类、快捷入口、热门游戏

### 彩票游戏（4 种）
- 极速赛车（PK10）、时时彩（SSC）、PC 蛋蛋（PC28）、六合彩（LHC）
- 实时开奖、走势图表、遗漏统计、投注下单

### 娱乐城
- 多平台接入（PG/PP/JDB/MG/AG/KM）
- 热门游戏、最新游戏、全部游戏、大奖游戏
- 游戏进入、平台余额、转账

### 活动优惠
- 活动分类、活动列表、活动详情
- 每日签到、幸运转盘、亏损救援、邀请好友
- 任务中心、礼金中心、充值奖励、注册礼金

### 用户中心（40+ 子页面）
- 个人资料、头像设置、绑定手机/邮箱、资金密码
- 充值（支付宝/微信/银行卡/USDT/卡密）、提现
- 交易记录、投注记录、盈亏报表
- 消息中心（系统/财务/活动/私信）
- VIP 中心（11 级）、银行卡管理、USDT 钱包
- 代理中心（20+ 接口）、返水中心、余额宝
- 修改密码、设备信息、意见反馈

## 快速开始

### 环境要求
- Node.js 18+
- npm 9+

### 安装依赖
```bash
npm install
```

### 开发模式
```bash
# 真实后端（需后端运行在 8888 端口）
npm run dev

# 或配置后端地址
VITE_API_BASE=http://your-backend:8888/api npm run dev
```

### 生产构建
```bash
npm run build
# 产物在 dist/ 目录
```

## 环境变量

| 变量 | 默认值 | 说明 |
|---|---|---|
| `VITE_API_BASE` | `/api` | API 基础路径 |
| `VITE_TELEGRAM_ENABLED` | `false` | 是否启用 Telegram Mini App |
| `VITE_PROXY_TARGET` | - | 开发代理目标地址 |

## 项目结构

```
src/
├── api/                    # API 接口层（6 个模块，180+ 方法）
│   ├── auth.js             # 鉴权（登录/注册/试玩/登出）
│   ├── home.js             # 首页（配置/Banner/公告/分类/游戏）
│   ├── lottery.js          # 彩票（开奖/走势/投注/记录）
│   ├── casino.js           # 娱乐城（平台/游戏/进入/转账）
│   ├── promo.js            # 活动（列表/详情/签到/转盘/任务）
│   └── user.js             # 用户中心（90+ 方法）
├── components/             # 全局组件
│   ├── Tabbar.vue          # 底部导航
│   ├── LotteryGamePage.vue # 彩票游戏页
│   ├── LotteryTrendPanel.vue # 走势面板
│   └── PuzzleCaptcha.vue   # 滑块验证码
├── router/                 # 路由（80+ 路由）
├── stores/                 # Pinia 状态（4 个 store）
├── utils/                  # 工具函数
│   ├── request.js          # Axios 封装（拦截器/错误处理）
│   └── telegram.js         # Telegram SDK 封装
├── views/                  # 页面（75+ 页面）
├── App.vue
├── main.js
└── variables.css           # CSS 变量 + 主题
```

## Telegram Mini App 适配

已内置 Telegram Mini App 支持：

1. **SDK 集成**：`index.html` 引入 `telegram-web-app.js`
2. **条件初始化**：`main.js` 检测 Telegram 环境自动初始化
3. **主题适配**：`.telegram-env` CSS 类覆盖主题变量
4. **返回按钮**：路由级 BackButton 自动管理
5. **主按钮**：MainButton 用于表单提交
6. **触觉反馈**：HapticFeedback 支持
7. **用户信息**：Telegram 用户信息自动填充登录

**启用方式**：设置环境变量 `VITE_TELEGRAM_ENABLED=true`

## API 对接规范

### 基础配置
- Base URL: `/api`
- 用户端路径: `/api/wap/*`
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

## 部署

### Nginx 配置示例
```nginx
server {
    listen 80;
    server_name h5.yourdomain.com;
    root /var/www/h5/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8888;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 部署步骤
```bash
npm run build
scp -r dist/* root@yourserver:/var/www/h5/
nginx -s reload
```

## 浏览器兼容

- iOS Safari 12+
- Android Chrome 80+
- Telegram WebView（内置）
- 桌面浏览器（480px 居中壳）

## 许可证

MIT
