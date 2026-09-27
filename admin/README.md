# H5 Admin Panel - 运营管理后台

> 基于 [vue-pure-admin](https://github.com/pure-admin/vue-pure-admin) 框架搭建的运营管理后台，**已对接真实后端 `/api/admin/*` 管理端接口（40+）**，支持用户管理、订单审核、游戏管理、活动管理、消息管理、站点配置。

## 项目概览

| 项目 | 仓库 | 说明 |
|---|---|---|
| H5 用户端 | [h5-clone](https://github.com/AthenDrakomin-hub/h5-clone) | Vue3 + Vant4 移动端 |
| 管理后台 | [h5-admin-panel](https://github.com/AthenDrakomin-hub/h5-admin-panel) | 本仓库 |
| 后端服务 | [h5-backend](https://github.com/AthenDrakomin-hub/h5-backend) | Spring Boot 3 + MySQL 8 |

## 技术栈

| 层级 | 技术 | 版本 |
|---|---|---|
| 框架 | Vue | 3.4+ |
| 语言 | TypeScript | 5.3+ |
| UI 库 | Element Plus | 2.5+ |
| 状态管理 | Pinia | 2.1+ |
| 路由 | vue-router | 4.2+ |
| HTTP | Axios | 1.6+ |
| 构建 | Vite | 5.0+ |
| 基础框架 | vue-pure-admin | 5.0+ |
| 图表 | ECharts | 5.4+ |

## 功能模块

### 仪表盘（Dashboard）
- 核心数据统计：总用户数、今日新增、总充值、总提现、净利润
- 投注统计：总投注额、总中奖额、投注笔数
- 待审核：充值待审核、提现待审核
- 最近订单列表、最近注册用户

### 用户管理（User Management）
- 用户列表：搜索（用户名/昵称/手机号）、筛选（状态/VIP等级）、分页
- 用户详情：查看完整用户信息
- 启用/禁用用户
- 调整余额（增加/扣减）
- 重置密码
- 设置 VIP 等级

### 订单管理（Order Management）
- 订单列表：筛选（类型/状态/订单号）、分页
- 充值审核：通过（自动加余额+生成交易流水）、拒绝
- 提现审核：通过（扣冻结）、拒绝（退回余额+解冻）
- 订单详情查看

### 游戏管理（Game Management）
- 彩票游戏：列表、新增、编辑、删除
- 开奖记录：列表、手动录入开奖结果
- 娱乐城平台：列表、新增、编辑
- 娱乐城游戏：列表（按平台筛选）、新增、编辑、删除

### 活动管理（Promo Management）
- 活动列表：分类筛选、分页
- 活动详情查看
- 新增/编辑活动
- 删除活动
- 活动上下架切换

### 消息管理（Message Management）
- 消息列表：分类筛选、分页
- 发送消息：全站广播 / 指定用户
- 删除消息

### 站点配置（Site Config）
- 站点参数配置
- Banner 管理：列表、新增、编辑、删除
- 公告管理：列表、新增、编辑、删除

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
npm run dev
# 默认 http://localhost:8848
```

### 生产构建
```bash
npm run build
# 产物在 dist/ 目录，约 23.7MB
```

### 预览构建
```bash
npm run preview
```

## 环境变量

| 变量 | 默认值 | 说明 |
|---|---|---|
| `VITE_API_BASE` | `/api` | API 基础路径 |
| `VITE_PORT` | 8848 | 开发端口 |

## 项目结构

```
src/
├── api/
│   ├── admin.ts              # 管理端 API（50+ 方法，对接 /api/admin/*）
│   ├── user.ts               # 用户端 API（兼容）
│   └── ...
├── views/
│   └── admin/                # 管理页面（6 个）
│       ├── Dashboard.vue         # 仪表盘
│       ├── UserManagement.vue    # 用户管理
│       ├── OrderManagement.vue   # 订单管理
│       ├── GameManagement.vue    # 游戏管理
│       ├── PromoManagement.vue   # 活动管理
│       └── MessageManagement.vue # 消息管理
├── router/                   # 路由配置
├── stores/                   # Pinia 状态
├── utils/
│   └── http.ts               # Axios 封装（拦截器 + 错误处理）
├── layout/                   # 布局（侧边栏 + 顶部栏）
└── App.vue
```

## API 对接规范

### 基础配置
- Base URL: `/api`
- 管理端路径: `/api/admin/*`
- 鉴权: `Authorization: Bearer <token>` + `X-User-Id: <userId>`

### 响应结构
```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "time": 1700000000000
}
```

### 管理端 API 清单（40+ 接口）

| 模块 | 接口数 | 说明 |
|---|---|---|
| 仪表盘 | 3 | 统计/最近订单/最近用户 |
| 用户管理 | 6 | 列表/详情/状态/余额/密码/VIP |
| 订单管理 | 6 | 列表/详情/充值审核/提现审核 |
| 游戏管理 | 8 | 彩票CRUD/开奖CRUD/娱乐城平台/游戏CRUD |
| 活动管理 | 5 | 列表/详情/保存/删除/上下架 |
| 消息管理 | 3 | 列表/发送/删除 |
| 站点配置 | 7 | 配置/Banner CRUD/公告 CRUD |

> 完整 API 文档见 [h5-backend README](https://github.com/AthenDrakomin-hub/h5-backend)

## 部署

### Nginx 配置示例
```nginx
server {
    listen 80;
    server_name admin.yourdomain.com;
    root /var/www/admin/dist;
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
scp -r dist/* root@yourserver:/var/www/admin/
nginx -s reload
```

## 管理员账号

管理员账号在 `users` 表中，`status` 字段标记管理员权限。首次部署后可通过数据库直接创建：

```sql
INSERT INTO users (username, password, nickname, balance, vip_level, status, is_trial)
VALUES ('admin', '$2a$10$...', '管理员', 0, 11, 99, 0);
-- password 需使用 BCrypt 加密
```

## 浏览器兼容

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## 许可证

MIT
