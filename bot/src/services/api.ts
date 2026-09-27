import axios from 'axios';
import { config } from '../config';

export interface ApiResponse<T = any> {
  code: number;
  msg: string;
  data: T;
  time: number;
}

export interface UserAuthData {
  token: string;
  userId: number;
  id: number;
  username: string;
  nickname: string;
  balance: number;
  vipLevel: number;
  isTrial: boolean;
  createTime: string;
}

class ApiService {
  private client = axios.create({
    baseURL: config.apiBaseUrl,
    timeout: 15000,
    headers: { 'Content-Type': 'application/json' },
  });

  /** 带用户鉴权的请求 */
  async authRequest<T = any>(
    method: 'get' | 'post',
    path: string,
    token: string,
    userId: number | string,
    data?: any
  ): Promise<ApiResponse<T>> {
    const headers = {
      Authorization: `Bearer ${token}`,
      'X-User-Id': String(userId),
    };
    const res = method === 'get'
      ? await this.client.get(path, { headers, params: data })
      : await this.client.post(path, data, { headers });
    return res.data;
  }

  /** Telegram 用户自动注册/登录 */
  async telegramAuth(params: {
    telegramId: number;
    username?: string;
    firstName?: string;
    lastName?: string;
    photoUrl?: string;
    languageCode?: string;
    inviteCode?: string;
  }): Promise<ApiResponse<UserAuthData>> {
    const res = await this.client.post('/bot/auth/register', params);
    return res.data;
  }

  /** 查询余额 */
  async getBalance(token: string, userId: number) {
    return this.authRequest('get', '/wap/wallet/balance', token, userId);
  }

  /** 创建充值订单 */
  async createRecharge(token: string, userId: number, amount: number, payType: string) {
    return this.authRequest('post', '/wap/wallet/recharge/create', token, userId, {
      amount,
      payType,
    });
  }

  /** 提交提现申请 */
  async createWithdraw(token: string, userId: number, amount: number, account: string) {
    return this.authRequest('post', '/wap/wallet/withdraw/create', token, userId, {
      amount,
      account,
    });
  }

  /** 交易明细 */
  async getTransactions(token: string, userId: number, page = 1, size = 10) {
    return this.authRequest('get', '/wap/wallet/transactions', token, userId, { page, size });
  }

  /** 每日签到 */
  async dailySignIn(token: string, userId: number) {
    return this.authRequest('post', '/wap/promo/signin', token, userId);
  }

  /** 签到状态 */
  async getSignInStatus(token: string, userId: number) {
    return this.authRequest('get', '/wap/promo/signin/status', token, userId);
  }

  /** 活动列表 */
  async getPromoList(token: string, userId: number) {
    return this.authRequest('get', '/wap/promo/list', token, userId);
  }

  /** 用户信息 */
  async getUserInfo(token: string, userId: number) {
    return this.authRequest('get', '/wap/user/info', token, userId);
  }
}

export const apiService = new ApiService();
