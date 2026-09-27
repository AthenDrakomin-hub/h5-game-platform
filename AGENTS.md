# AGENTS.md

乐享 H5 Game Platform 全栈 monorepo，包含**前端用户端**、**后端服务**、**运营管理后台**三个子项目。

环境：Node 18+ · Vite 5.4 · Vue 3.5 · Java 17 · Spring Boot 3.2.5 · MySQL 8 · Maven 3.6+。

## 项目结构

```text
h5-game-platform/
├── frontend/                    # 移动端 H5 用户端（Vue 3 + Vant 4 + JS）
│   ├── src/
│   │   ├── main.js              # 入口：Pinia/Router/Vant 初始化，MSW/Telegram 条件启动
│   │   ├── App.vue              # 根组件：路由出口 + 全局登录弹窗 + Tabbar + Telegram BackButton
│   │   ├── api/                 # Axios 封装 + 6 大模块 API（auth/lottery/casino/promo/user/home）
│   │   │   ├── request.js       # Bearer+X-Token+X-User-Id 鉴权，多格式响应归一化
│   │   │   └── *.js             # 每个业务模块独立文件
│   │   ├── stores/              # Pinia store：app / auth / notice / user
│   │   ├── router/              # Hash 模式路由（80+ 路由），modules/ 按业务拆分
│   │   ├── views/               # 页面组件（75 个），按域分组：lottery/casino/promo/user/common
│   │   ├── components/          # 全局组件：Tabbar / PuzzleCaptcha / LotteryGamePage / LotteryTrendPanel
│   │   ├── utils/               # telegram.js / storage.js / safeBack.js
│   │   └── styles/              # variables.css（CSS 变量）/ global.css（过渡动画）
│   ├── mock/                    # MSW Mock（VITE_USE_MOCK=true 时启用）
│   ├── .env.development         # VITE_PROXY_TARGET / VITE_USE_MOCK / VITE_TELEGRAM_ENABLED
│   ├── .env.production
│   ├── vite.config.js           # 开发代理 /api /uploads 到 VITE_PROXY_TARGET
│   └── package.json
│
├── backend/                     # 后端服务（Spring Boot 3 + MyBatis-Plus + MySQL 8）
│   ├── src/main/java/com/h5/
│   │   ├── H5Application.java   # 启动类
│   │   ├── common/              # Result<T> / BusinessException / GlobalExceptionHandler / UserContext
│   │   ├── config/              # WebConfig / AuthInterceptor / MyBatisPlusConfig / MyMetaObjectHandler
│   │   ├── controller/          # 31 个控制器
│   │   │   ├── auth/            # 登录/注册/试玩
│   │   │   ├── home/            # 站点配置/Banner/游戏入口/公告
│   │   │   ├── lottery/         # 彩票分类/开奖/投注
│   │   │   ├── casino/          # 娱乐城平台/游戏/进入/转账
│   │   │   ├── promo/           # 活动/任务/转盘
│   │   │   ├── user/            # 用户信息/充值提现/订单/VIP/银行卡/消息/代理/返水/余额宝
│   │   │   └── admin/           # 管理端：仪表盘/用户/订单/游戏/活动/消息/站点配置
│   │   ├── entity/              # 15 个实体类（User/Bet/Order/...）
│   │   ├── mapper/              # 15 个 MyBatis-Plus Mapper 接口（无 XML）
│   │   ├── dto/                 # LoginDTO / RegisterDTO
│   │   └── util/JwtUtil.java    # JWT 工具：generateToken / parseToken / validateToken
│   ├── src/main/resources/
│   │   ├── application.yml      # 端口 18888，context-path=/api，数据库/JWT 配置
│   │   └── db/schema.sql        # 建表脚本 + 初始化数据
│   └── pom.xml                  # Spring Boot 3.2.5 / MyBatis-Plus 3.5.5 / JWT 0.12.5 / Lombok
│
└── admin/                       # 运营管理后台（Vue 3 + TypeScript + Element Plus + vue-pure-admin）
    ├── src/
    │   ├── main.ts              # Vue/Router/Pinia/ElementPlus 初始化
    │   ├── api/                 # admin.ts（管理端 50+ API）/ list.ts / mock.ts / routes.ts / system.ts / user.ts
    │   ├── views/admin/         # 6 个管理页面：Dashboard/User/Order/Game/Promo/Message
    │   ├── styles/              # 全局样式
    │   └── types/               # TypeScript 类型声明
    ├── vite.config.ts           # 基于 build/utils.ts 的 Vite 配置（CDN/压缩/代理）
    ├── eslint.config.js / .prettierrc.js / stylelint.config.js
    └── package.json             # pnpm 依赖管理，Node >=22，pnpm >=11
```

生成物（`dist/`、`node_modules/`、`target/`）不要提交或手改。

## 构建 / lint / 测试命令

### 前端用户端（frontend/）

```bash
cd frontend
npm install
npm run dev                 # :5173，/api 代理到 VITE_PROXY_TARGET
npm run build               # 产物 dist/，含 vue-vendor / vant-vendor chunk 拆分
npm run preview             # 预览构建产物

# Mock 模式：编辑 .env.development 设 VITE_USE_MOCK=true，重启 dev server
# Telegram 开关：VITE_TELEGRAM_ENABLED=false 完全回退普通 H5
```

无 ESLint / Prettier / TypeScript / 单元测试脚本。

### 后端（backend/）

```bash
cd backend

# 建库建表（数据库 h5_game 用户 h5game 密码 H5g@me2024!）
mysql -u h5game -p'H5g@me2024!' h5_game < src/main/resources/db/schema.sql

# 编译打包（生产环境修改 config/application.yml 中的 jwt.secret 和数据库密码）
mvn clean package -DskipTests

# 运行（推荐用 systemd）
systemctl enable h5-backend && systemctl start h5-backend
# 后端运行在 http://127.0.0.1:18888/api/
```

无单元测试。

### 管理后台（admin/）

```bash
cd admin
pnpm install
pnpm dev                        # :8848，需 Node >=22
pnpm build                      # 生产构建
pnpm lint                       # ESLint + Prettier + Stylelint
pnpm typecheck                  # vue-tsc --noEmit
```

## 代码风格与协作规则

### 通用

- 最小 diff：跟随所在文件的缩进、引号、分号；不要整文件格式化或顺手重构。
- 注释用中文，写「为什么」。提交信息用中文，参考 `git log`；未经用户明确要求不 commit / push。
- 不要引入无关依赖。`.env*` 中的密钥不得写入仓库。客户端环境文件只放公开地址。
- 改前先读工作区已有改动，不要用重置覆盖他人修改。
- 涉及前后端对接时，同步更新两边代码（接口路径、字段名、响应结构）。

### 前端 JS/Vue（frontend/）

- 纯 JavaScript，无 TypeScript。`<script setup>` 风格。SFC 顺序：`<template>` → `<script setup>` → `<style scoped>`。
- `@/` 指向 `src/`（Vite 别名），跨目录优先 `@/`，同目录用相对路径。
- import 顺序：外部库（vue/pinia/vue-router/axios/vant/dayjs）→ 项目模块（@/stores / @/api / @/utils）→ 本地模块。
- Store：camelCase 文件名，`defineStore('auth', {...})` ID 与文件名一致。枚举集中放在 `api/enums.js`。
- 页面与组件：PascalCase。`cachedViews` 在 App.vue 中定义 KeepAlive 缓存。
- 路由：Hash 模式（`createWebHashHistory`），所有页面组件懒加载 `() => import()`。路由模块文件放 `router/modules/`。
- 需要登录的路由加 `meta: { requiresAuth: true }`。

### 请求层（api/request.js）

统一走 `api/request.js`，不要在页面里直接 import axios。关键约定：

- 鉴权自动携带：`Authorization: Bearer <token>` + `X-Token` + `X-User-Id`。
- 响应结构归一化：兼容 `{code:0|200,data}` / `{code:200,result}` / `{status:'success',data}` / 裸数据。
- 401/400/500 自动 showFailToast；只有明确 token 失效（code=401/1001/1002 或 message 含"登录/token/未登录"）才清除 localStorage。
- 普通网络错误不清登录态，保留可恢复路径。
- 需要静默失败（如轮询）时传 `skipErrorToast: true`。
- Promise 必须显式 resolve/reject，禁止永久挂起的 await。

### Pinia Store

- 只放状态与业务动作，网络请求复用 `@/api/*`。
- `setAuth()` 兼容两种结构：真实后端 `{token, userId, username, ...}` 和 mock `{token, userInfo: {...}}`。
- 不要在页面中直接改写跨页面共享状态的内部结构。

### Telegram Mini App（frontend/）

- `VITE_TELEGRAM_ENABLED=true` 时在 Telegram 内自动登录（`authStore.telegramLogin()`）。
- `utils/telegram.js` 封装：主题适配、原生返回按钮 `showBackButton/hideBackButton`、触觉反馈 `haptic.*()`、分享 `shareToTelegram()`。
- 平台差异用 `isTelegram` 判断，禁止 UA 嗅探。
- 非 Tabbar 主页面显示 BackButton（App.vue 中 `TG_BACK_PATTERNS` 控制）。

### 样式（frontend/）

- 全局 CSS 变量定义在 `styles/variables.css`，组件内用 `var(--xxx)` 引用。
- `global.css` 只做全局覆盖和过渡动画。禁止硬编码大段颜色，优先复用变量。

### Java 后端（backend/）

- Java 17，Spring Boot 3.2.5，Lombok（`@Data` `@RequiredArgsConstructor`），Jakarta 包名（非 `javax`）。
- 包名 `com.h5`。Controller 按业务域分组：`controller/auth/`、`controller/home/`、`controller/lottery/` 等。
- 实体类在 `entity/`，Mapper 接口在 `mapper/`（MyBatis-Plus 接口，无 XML），Service 逻辑直接写在 Controller 中（简化版，无 Service 层）。
- 统一响应：`Result<T>`（`code/msg/data/time`），业务异常抛 `BusinessException`，由 `GlobalExceptionHandler` 捕获。
- 当前用户从 `UserContext.getUserId()` / `UserContext.getUsername()` 取（ThreadLocal），由 `AuthInterceptor` 设置。
- 鉴权：`AuthInterceptor` 验证 `Authorization: Bearer *** + `X-User-Id`，放行路径在 `WebConfig` 中配置。
- JWT：`JwtUtil` 组件，secret 从 `config/application.yml` 注入，生产密钥已改为 H5GameProdSecret2024!SafeKeyForTokenSigning。
- import：java/jakarta → 第三方 → `com.h5.*`；避免无必要的 `*`。
- 注释用中文。

### TypeScript 管理后台（admin/）

- Vue 3 + TypeScript + Element Plus + vue-pure-admin 框架。
- ESLint + Prettier + Stylelint；`pnpm lint` 修复。`pnpm typecheck` 类型检查。
- API 放 `src/api/*.ts`，类型声明放 `src/types/`。
- 使用 pnpm 而非 npm（`package.json` 中有 `preinstall: npx only-allow pnpm`）。

### 代理工作流

- 先读目标文件及其直接依赖，再改。变更保持最小范围。
- 报告时写明改了哪些文件、跑了哪些命令、哪些检查没跑；未运行的构建不要写成已通过。
- 后端 `config/application.yml` 中的 JWT secret、数据库密码等敏感配置不要提交到仓库。
- 生产域名 goodspage.cn，Nginx 代理 `/api/` → `127.0.0.1:18888/api/`，`/uploads/` → `https://api.goodspage.cn/uploads/`。
- 改动接口时同步更新前后端的字段名和响应结构。
