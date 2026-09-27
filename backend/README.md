# H5 Game Backend - H5 娱乐后端服务

> 基于 Spring Boot 3.2 + MyBatis-Plus + MySQL 8 + JWT 的后端服务，为 H5 用户端和管理后台提供完整 API。**用户端 150+ 接口 + 管理端 40+ 接口，合计 190+。**

## 项目概览

| 项目 | 仓库 | 说明 |
|---|---|---|
| H5 用户端 | [h5-clone](https://github.com/AthenDrakomin-hub/h5-clone) | Vue3 + Vant4 移动端 |
| 管理后台 | [h5-admin-panel](https://github.com/AthenDrakomin-hub/h5-admin-panel) | Vue3 + Element Plus 管理系统 |
| 后端服务 | [h5-backend](https://github.com/AthenDrakomin-hub/h5-backend) | 本仓库 |

## 技术栈

| 层级 | 技术 | 版本 |
|---|---|---|
| 框架 | Spring Boot | 3.2.5 |
| ORM | MyBatis-Plus | 3.5.5 |
| 数据库 | MySQL | 8.0+ |
| 鉴权 | JWT (jjwt) | 0.12.5 |
| 密码加密 | BCrypt | Spring Security |
| 工具库 | Hutool | 5.8.27 |
| 构建 | Maven | 3.6+ |
| JDK | Java | 17+ |

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 1. 建库建表
```bash
mysql -u root -p < src/main/resources/db/schema.sql
```
> schema.sql 包含 15 张表 + 初始化数据（彩种/娱乐城平台/活动/公告/Banner/站点配置）

### 2. 修改配置
```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/h5_game?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

jwt:
  secret: your_jwt_secret_key_at_least_32_chars
```

### 3. 编译运行
```bash
# 编译
mvn clean package -DskipTests

# 运行
java -jar target/h5-backend-1.0.0.jar

# 或开发模式
mvn spring-boot:run
```

### 4. 验证
```bash
curl http://localhost:8888/api/wap/home/config
# 返回站点配置 JSON
```

## 配置说明

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `server.port` | 8888 | 服务端口 |
| `server.servlet.context-path` | /api | 上下文路径 |
| `spring.datasource.url` | jdbc:mysql://localhost:3306/h5_game | 数据库连接 |
| `jwt.secret` | - | JWT 密钥（必须修改） |
| `jwt.expiration` | 86400000 | Token 有效期（毫秒） |

## 数据库设计（15 张表）

| 表名 | 说明 | 关键字段 |
|---|---|---|
| `users` | 用户表 | username, password, balance, frozen_balance, vip_level, status, is_trial |
| `user_wallets` | USDT 钱包 | user_id, chain, address, is_default |
| `user_bank_cards` | 银行卡 | user_id, bank_name, card_number, card_holder, is_default |
| `orders` | 订单表 | order_no, user_id, type(recharge/withdraw), amount, status, method |
| `transactions` | 交易流水 | user_id, type, amount, balance_before, balance_after, ref_no |
| `lotteries` | 彩票游戏 | name, code, category_code, draw_interval, status |
| `draw_results` | 开奖记录 | lottery_code, period, numbers, status |
| `bets` | 投注记录 | bet_no, user_id, lottery_code, period, play_type, numbers, amount, win_amount |
| `casino_providers` | 娱乐城平台 | name, code, icon, status |
| `casino_games` | 娱乐城游戏 | name, game_code, provider_code, category, enter_url, is_hot, is_new |
| `promotions` | 活动 | title, category, image, content, status, start_time, end_time |
| `messages` | 消息 | user_id(0=全站), category, title, content, is_read |
| `notices` | 公告 | title, content, type, status |
| `banners` | Banner | title, image, link, position, status |
| `site_config` | 站点配置 | config_key, config_value, description |

## API 总览

### 统一响应结构
```json
{
  "code": 200,
  "msg": "success",
  "data": {},
  "time": 1700000000000
}
```

### 鉴权方式
- Header: `Authorization: Bearer <token>`
- Header: `X-User-Id: <userId>`
- 两个 header 缺一不可，拦截器自动校验一致性

---

## 用户端 API（/api/wap/*，150+ 接口）

### 鉴权模块（4）
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /wap/auth/login | 登录 |
| POST | /wap/auth/register | 注册 |
| POST | /wap/auth/trial-login | 试玩登录 |
| POST | /wap/auth/logout | 登出 |

### 首页模块（6）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/home/config | 站点配置 |
| GET | /wap/home/banners | Banner 列表 |
| GET | /wap/home/announcements | 公告列表 |
| GET | /wap/home/categories | 彩票分类 |
| GET | /wap/home/games | 彩票游戏 |
| GET | /wap/home/quick-entries | 快捷入口 |

### 彩票模块（7）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/draw/info/batch | 批量开奖 |
| GET | /wap/lottery/data | 彩票数据 |
| GET | /wap/lottery/trend/dragon/{code} | 龙虎走势 |
| GET | /wap/lottery/trend/miss/{code} | 遗漏走势 |
| GET | /wap/lottery/pending-bets | 待开奖投注 |
| GET | /wap/lottery/bet-list | 投注记录 |
| GET | /wap/lottery/third-bet-list | 第三方投注 |

### 投注模块（3）
| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /wap/bet/place | 提交投注（扣余额+流水） |
| GET | /wap/bet/list | 投注列表 |
| GET | /wap/bet/detail/{betNo} | 投注详情 |

### 娱乐城模块（10）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/game/casino-providers | 平台列表 |
| GET | /wap/game/casino-games | 游戏列表（分页） |
| GET | /wap/game/casino-hot-games | 热门游戏 |
| GET | /wap/game/casino-new-games | 最新游戏 |
| GET | /wap/game/all-games | 全部游戏 |
| GET | /wap/game/big-prize-games | 大奖游戏 |
| GET | /wap/game/platform-balance/{platform} | 平台余额 |
| GET | /wap/game/km-platforms | KM平台 |
| GET | /wap/{platform}/enter | 游戏进入URL |
| POST | /wap/casino/transfer | 娱乐城转账 |

### 活动模块（23）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/promotion/categories | 活动分类 |
| GET | /wap/promotion/list | 活动列表 |
| GET | /wap/promotion/detail/{id} | 活动详情 |
| GET | /wap/promo/float-activities | 浮动活动 |
| GET | /wap/promo/float-event | 浮动事件 |
| GET | /wap/promo/reward/list | 礼金列表 |
| GET | /wap/promo/reward/summary | 礼金汇总 |
| POST | /wap/promo/recharge-reward/preview | 充值奖励预览 |
| GET | /wap/promo/recharge-reward/summary | 充值奖励汇总 |
| GET | /wap/promo/register-bonus/summary | 注册礼金 |
| GET | /wap/promo/task/list | 任务列表 |
| GET | /wap/promo/signin/info | 签到信息 |
| POST | /wap/promo/signin | 签到 |
| GET | /wap/promo/lucky-wheel/info | 幸运转盘信息 |
| POST | /wap/promo/lucky-wheel/spin | 转盘抽奖 |
| GET | /wap/promo/loss-rescue/summary | 亏损救援 |
| POST | /wap/promo/loss-rescue/claim | 领取救援金 |
| GET | /wap/promo/invite/info | 邀请信息 |

### 充值提现模块（8）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/payment/recharge-methods | 充值方式 |
| GET | /wap/payment/withdraw-methods | 提现方式 |
| POST | /wap/payment/recharge/create | 创建充值订单 |
| POST | /wap/payment/withdraw/create | 创建提现申请（冻结余额） |
| POST | /wap/recharge/manual-order | 手动充值 |
| POST | /wap/recharge/declare-paid | 声明已支付 |
| GET | /wap/recharge/query | 查询订单 |
| GET | /wap/usdt/rate | USDT汇率 |

### 订单记录模块（4）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/transaction/list | 交易记录 |
| GET | /wap/transaction/recharge-list | 充值订单 |
| GET | /wap/transaction/withdraw-list | 提现订单 |
| GET | /wap/transaction/detail/{orderNo} | 订单详情 |

### 消息模块（8）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/message/all/list | 全部消息 |
| GET | /wap/message/all/unread-counts | 未读统计 |
| POST | /wap/message/all/read | 标记已读 |
| GET | /wap/message/notification-list | 通知消息 |
| GET | /wap/message/finance-list | 财务消息 |
| GET | /wap/message/promo-list | 活动消息 |
| GET | /wap/message/private-list | 私信 |
| POST | /wap/message/read-all | 全部已读 |

### 用户模块（40+）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/user/info | 用户信息 |
| POST | /wap/user/update | 更新资料 |
| POST | /wap/user/change-password | 修改密码 |
| GET | /wap/user/avatars | 头像列表 |
| GET | /wap/user/device-info | 设备信息 |
| GET | /wap/user/bind-status | 绑定状态 |
| POST | /wap/user/set-withdraw-password | 设置资金密码 |
| POST | /wap/user/bind-phone | 绑定手机 |
| POST | /wap/user/bind-email | 绑定邮箱 |
| POST | /wap/user/set-birthday | 设置生日 |
| POST | /wap/user/update-avatar | 更新头像 |
| POST | /wap/user/upload-image | 上传图片 |
| POST | /wap/user/feedback/submit | 提交反馈 |
| GET | /wap/user/feedback/my-list | 我的反馈 |
| GET | /wap/report/profit | 盈亏报表 |

### VIP 模块（3）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/vip/info | VIP信息（11级+进度） |
| GET | /wap/vip/levels | VIP等级列表 |
| GET | /wap/vip/contents | VIP内容 |

### 银行卡模块（4）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/user-bank-card/list | 银行卡列表 |
| POST | /wap/user-bank-card/add | 添加银行卡 |
| POST | /wap/user-bank-card/set-default | 设默认 |
| POST | /wap/user-bank-card/delete | 删除 |

### USDT 钱包模块（5）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/user-wallet/list | 钱包列表 |
| POST | /wap/user-wallet/add | 添加钱包 |
| POST | /wap/user-wallet/delete | 删除 |
| GET | /wap/wallet/method-list | 钱包方式 |
| GET | /wap/wallet/method-type-list | 链类型 |

### 代理中心模块（20）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/agent/overview | 代理概览 |
| GET | /wap/agent/stats | 代理统计 |
| GET | /wap/agent/members | 成员列表 |
| GET | /wap/agent/records | 代理记录 |
| GET | /wap/agent/member-stats | 成员统计 |
| GET | /wap/agent/promote-dashboard | 推广面板 |
| GET | /wap/agent/workbench-stats | 工作台统计 |
| GET | /wap/agent/rebate-ratio | 返水比例 |
| GET | /wap/agent/rate-scope | 等级范围 |
| GET | /wap/agent/loss-summary | 亏损汇总 |
| GET | /wap/agent/loss-records | 亏损记录 |
| POST | /wap/agent/withdraw | 代理提现 |
| POST | /wap/agent/create-member | 创建成员 |

### 返水模块（5）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/rebate/overview | 返水概览 |
| GET | /wap/rebate/ladders | 返水阶梯 |
| GET | /wap/rebate/vendor-records | 厂商记录 |
| GET | /wap/rebate/user-list | 用户列表 |
| POST | /wap/rebate/claim-all | 领取全部 |

### 余额宝模块（6）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /wap/yuebao/config | 余额宝配置 |
| GET | /wap/yuebao/info | 余额宝信息 |
| GET | /wap/yuebao/records | 记录 |
| POST | /wap/yuebao/transfer-in | 转入（扣余额） |
| POST | /wap/yuebao/transfer-out | 转出（加余额） |
| POST | /wap/yuebao/claim-all | 领取收益 |

---

## 管理端 API（/api/admin/*，40+ 接口）

### 仪表盘（3）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /admin/dashboard/stats | 统计数据（用户/充值/提现/投注/待审核） |
| GET | /admin/dashboard/recent-orders | 最近订单 |
| GET | /admin/dashboard/recent-users | 最近用户 |

### 用户管理（6）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /admin/user/list | 用户列表（搜索/筛选/分页） |
| GET | /admin/user/detail/{id} | 用户详情 |
| POST | /admin/user/update-status | 启用/禁用 |
| POST | /admin/user/adjust-balance | 调整余额 |
| POST | /admin/user/reset-password | 重置密码 |
| POST | /admin/user/set-vip | 设置VIP |

### 订单管理（6）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /admin/order/list | 订单列表 |
| GET | /admin/order/detail/{id} | 订单详情 |
| POST | /admin/order/recharge/approve | 充值通过（加余额+流水） |
| POST | /admin/order/recharge/reject | 充值拒绝 |
| POST | /admin/order/withdraw/approve | 提现通过（扣冻结） |
| POST | /admin/order/withdraw/reject | 提现拒绝（退回余额） |

### 游戏管理（8）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /admin/game/lottery/list | 彩票游戏列表 |
| POST | /admin/game/lottery/save | 保存彩票游戏 |
| POST | /admin/game/lottery/delete/{id} | 删除彩票游戏 |
| GET | /admin/game/draw/list | 开奖记录 |
| POST | /admin/game/draw/save | 保存开奖 |
| GET | /admin/game/casino/provider/list | 娱乐城平台 |
| GET | /admin/game/casino/game/list | 娱乐城游戏 |
| POST | /admin/game/casino/game/save | 保存游戏 |

### 活动管理（5）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /admin/promo/list | 活动列表 |
| GET | /admin/promo/detail/{id} | 活动详情 |
| POST | /admin/promo/save | 保存活动 |
| POST | /admin/promo/delete/{id} | 删除活动 |
| POST | /admin/promo/toggle-status | 上下架 |

### 消息管理（3）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /admin/message/list | 消息列表 |
| POST | /admin/message/send | 发送消息（全站/指定用户） |
| POST | /admin/message/delete/{id} | 删除消息 |

### 站点配置（7）
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /admin/config/site | 站点配置 |
| POST | /admin/config/site/save | 保存配置 |
| GET | /admin/config/banner/list | Banner列表 |
| POST | /admin/config/banner/save | 保存Banner |
| POST | /admin/config/banner/delete/{id} | 删除Banner |
| GET | /admin/config/notice/list | 公告列表 |
| POST | /admin/config/notice/save | 保存公告 |

## 项目结构

```
src/main/java/com/h5/
├── H5Application.java          # 启动类
├── common/                     # 公共类
│   ├── Result.java             # 统一响应
│   ├── BusinessException.java  # 业务异常
│   ├── UserContext.java        # 当前用户上下文
│   └── GlobalExceptionHandler.java # 全局异常处理
├── config/                     # 配置类
│   ├── WebConfig.java          # CORS + 拦截器
│   ├── AuthInterceptor.java    # JWT 鉴权拦截器
│   ├── MyBatisPlusConfig.java  # 分页插件
│   └── JwtUtil.java            # JWT 工具
├── controller/                 # 控制器（31 个）
│   ├── auth/                   # 鉴权
│   ├── home/                   # 首页
│   ├── lottery/                # 彩票 + 投注
│   ├── casino/                 # 娱乐城 + 转账
│   ├── promo/                  # 活动 + 任务
│   ├── user/                   # 用户中心（15 个控制器）
│   └── admin/                  # 管理端（7 个控制器）
├── entity/                     # 实体类（15 个）
├── mapper/                     # Mapper（15 个）
└── service/                    # Service（可选）
src/main/resources/
├── application.yml             # 应用配置
└── db/schema.sql               # 建表脚本 + 初始化数据
```

## 部署

### 方式一：直接运行
```bash
mvn clean package -DskipTests
java -jar target/h5-backend-1.0.0.jar
```

### 方式二：Systemd 服务
```ini
# /etc/systemd/system/h5-backend.service
[Unit]
Description=H5 Game Backend
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/h5-backend
ExecStart=/usr/bin/java -jar /opt/h5-backend/h5-backend-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
systemctl daemon-reload
systemctl enable h5-backend
systemctl start h5-backend
systemctl status h5-backend
```

### 方式三：Docker
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/h5-backend-1.0.0.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 核心业务流程

### 充值流程
```
用户创建充值订单 → 订单状态 pending → 管理员审核通过 → 用户余额增加 → 交易流水记录
```

### 提现流程
```
用户创建提现申请 → 余额冻结 → 订单状态 pending → 管理员审核通过 → 扣冻结 → 交易流水
                                              → 管理员拒绝 → 退回余额 + 解冻
```

### 投注流程
```
用户提交投注 → 扣减余额 → 生成投注记录 → 交易流水 → 开奖后更新中奖金额
```

## 许可证

MIT
