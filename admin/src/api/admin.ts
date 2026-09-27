import { http } from "@/utils/http";

/**
 * 管理后台 API 模块
 * 对接真实后端 /api/admin/* 管理端接口
 * 后端: h5-backend (Spring Boot)
 */

// ============ 仪表盘 ============

/** 仪表盘统计数据 */
export const getDashboardStats = () => {
  return http.request<any>("get", "/admin/dashboard/stats");
};

/** 最近订单 */
export const getRecentOrders = (limit = 10) => {
  return http.request<any>("get", `/admin/dashboard/recent-orders?limit=${limit}`);
};

/** 最近注册用户 */
export const getRecentUsers = (limit = 10) => {
  return http.request<any>("get", `/admin/dashboard/recent-users?limit=${limit}`);
};

// ============ 用户管理 ============

/** 用户列表 */
export const getUserList = (params?: {
  page?: number;
  pageSize?: number;
  keyword?: string;
  status?: number;
  vipLevel?: number;
}) => {
  return http.request<any>("get", "/admin/user/list", { params });
};

/** 用户详情 */
export const getUserDetail = (id: number) => {
  return http.request<any>("get", `/admin/user/detail/${id}`);
};

/** 启用/禁用用户 */
export const updateUserStatus = (data: { id: number; status: number }) => {
  return http.request<any>("post", "/admin/user/update-status", { data });
};

/** 调整用户余额 */
export const adjustUserBalance = (data: {
  id: number;
  amount: number;
  type: "add" | "subtract";
}) => {
  return http.request<any>("post", "/admin/user/adjust-balance", { data });
};

/** 重置用户密码 */
export const resetUserPassword = (data: { id: number; password: string }) => {
  return http.request<any>("post", "/admin/user/reset-password", { data });
};

/** 设置用户VIP等级 */
export const setUserVip = (data: { id: number; vipLevel: number }) => {
  return http.request<any>("post", "/admin/user/set-vip", { data });
};

// ============ 订单管理 ============

/** 订单列表（充值/提现） */
export const getOrderList = (params?: {
  page?: number;
  pageSize?: number;
  type?: string;
  status?: string;
  orderNo?: string;
}) => {
  return http.request<any>("get", "/admin/order/list", { params });
};

/** 订单详情 */
export const getOrderDetail = (id: number) => {
  return http.request<any>("get", `/admin/order/detail/${id}`);
};

/** 审核通过充值订单 */
export const approveRecharge = (data: { id: number }) => {
  return http.request<any>("post", "/admin/order/recharge/approve", { data });
};

/** 拒绝充值订单 */
export const rejectRecharge = (data: { id: number; reason?: string }) => {
  return http.request<any>("post", "/admin/order/recharge/reject", { data });
};

/** 审核通过提现订单 */
export const approveWithdraw = (data: { id: number }) => {
  return http.request<any>("post", "/admin/order/withdraw/approve", { data });
};

/** 拒绝提现订单（退回余额） */
export const rejectWithdraw = (data: { id: number; reason?: string }) => {
  return http.request<any>("post", "/admin/order/withdraw/reject", { data });
};

// ============ 游戏管理 ============

/** 彩票游戏列表 */
export const getLotteryList = (params?: { page?: number; pageSize?: number }) => {
  return http.request<any>("get", "/admin/game/lottery/list", { params });
};

/** 保存彩票游戏（新增/编辑） */
export const saveLottery = (data: any) => {
  return http.request<any>("post", "/admin/game/lottery/save", { data });
};

/** 删除彩票游戏 */
export const deleteLottery = (id: number) => {
  return http.request<any>("post", `/admin/game/lottery/delete/${id}`);
};

/** 开奖记录列表 */
export const getDrawList = (params?: {
  page?: number;
  pageSize?: number;
  lotteryCode?: string;
}) => {
  return http.request<any>("get", "/admin/game/draw/list", { params });
};

/** 保存开奖记录 */
export const saveDraw = (data: any) => {
  return http.request<any>("post", "/admin/game/draw/save", { data });
};

/** 娱乐城平台列表 */
export const getCasinoProviderList = () => {
  return http.request<any>("get", "/admin/game/casino/provider/list");
};

/** 保存娱乐城平台 */
export const saveCasinoProvider = (data: any) => {
  return http.request<any>("post", "/admin/game/casino/provider/save", { data });
};

/** 娱乐城游戏列表 */
export const getCasinoGameList = (params?: {
  page?: number;
  pageSize?: number;
  providerCode?: string;
}) => {
  return http.request<any>("get", "/admin/game/casino/game/list", { params });
};

/** 保存娱乐城游戏 */
export const saveCasinoGame = (data: any) => {
  return http.request<any>("post", "/admin/game/casino/game/save", { data });
};

/** 删除娱乐城游戏 */
export const deleteCasinoGame = (id: number) => {
  return http.request<any>("post", `/admin/game/casino/game/delete/${id}`);
};

// ============ 活动管理 ============

/** 活动列表 */
export const getPromoList = (params?: {
  page?: number;
  pageSize?: number;
  category?: string;
}) => {
  return http.request<any>("get", "/admin/promo/list", { params });
};

/** 活动详情 */
export const getPromoDetail = (id: number) => {
  return http.request<any>("get", `/admin/promo/detail/${id}`);
};

/** 保存活动（新增/编辑） */
export const savePromo = (data: any) => {
  return http.request<any>("post", "/admin/promo/save", { data });
};

/** 删除活动 */
export const deletePromo = (id: number) => {
  return http.request<any>("post", `/admin/promo/delete/${id}`);
};

/** 活动上下架 */
export const togglePromoStatus = (data: { id: number; status: number }) => {
  return http.request<any>("post", "/admin/promo/toggle-status", { data });
};

// ============ 消息管理 ============

/** 消息列表 */
export const getMessageList = (params?: {
  page?: number;
  pageSize?: number;
  category?: string;
}) => {
  return http.request<any>("get", "/admin/message/list", { params });
};

/** 发送消息（全站/指定用户） */
export const sendMessage = (data: {
  userId?: number;
  category: string;
  title: string;
  content: string;
}) => {
  return http.request<any>("post", "/admin/message/send", { data });
};

/** 删除消息 */
export const deleteMessage = (id: number) => {
  return http.request<any>("post", `/admin/message/delete/${id}`);
};

// ============ 站点配置 ============

/** 站点配置列表 */
export const getSiteConfigList = () => {
  return http.request<any>("get", "/admin/config/site");
};

/** 保存站点配置 */
export const saveSiteConfig = (data: any) => {
  return http.request<any>("post", "/admin/config/site/save", { data });
};

/** Banner 列表 */
export const getBannerList = () => {
  return http.request<any>("get", "/admin/config/banner/list");
};

/** 保存 Banner */
export const saveBanner = (data: any) => {
  return http.request<any>("post", "/admin/config/banner/save", { data });
};

/** 删除 Banner */
export const deleteBanner = (id: number) => {
  return http.request<any>("post", `/admin/config/banner/delete/${id}`);
};

/** 公告列表 */
export const getNoticeList = () => {
  return http.request<any>("get", "/admin/config/notice/list");
};

/** 保存公告 */
export const saveNotice = (data: any) => {
  return http.request<any>("post", "/admin/config/notice/save", { data });
};

/** 删除公告 */
export const deleteNotice = (id: number) => {
  return http.request<any>("post", `/admin/config/notice/delete/${id}`);
};
