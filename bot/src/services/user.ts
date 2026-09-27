import { apiService, UserAuthData } from './api';

export interface BotUserSession {
  telegramId: number;
  userId: number;
  token: string;
  username?: string;
  nickname: string;
  balance: number;
  vipLevel: number;
  authAt: number;
}

/** 内存会话缓存（生产环境建议换 Redis） */
const sessionCache = new Map<number, BotUserSession>();
const CACHE_TTL = 30 * 60 * 1000; // 30 分钟

class UserService {
  /** 获取或创建用户会话（自动注册/登录） */
  async getOrCreateUser(
    telegramId: number,
    profile: {
      username?: string;
      firstName?: string;
      lastName?: string;
      photoUrl?: string;
      languageCode?: string;
    },
    inviteCode?: string
  ): Promise<BotUserSession> {
    // 检查缓存
    const cached = sessionCache.get(telegramId);
    if (cached && Date.now() - cached.authAt < CACHE_TTL) {
      return cached;
    }

    // 调用后端自动注册/登录
    const res = await apiService.telegramAuth({
      telegramId,
      username: profile.username,
      firstName: profile.firstName,
      lastName: profile.lastName,
      photoUrl: profile.photoUrl,
      languageCode: profile.languageCode,
      inviteCode,
    });

    if (res.code !== 200 || !res.data) {
      throw new Error(`注册/登录失败: ${res.msg}`);
    }

    const userData: UserAuthData = res.data;
    const session: BotUserSession = {
      telegramId,
      userId: userData.userId,
      token: userData.token,
      username: profile.username,
      nickname: userData.nickname || profile.firstName || `用户${userData.userId}`,
      balance: userData.balance,
      vipLevel: userData.vipLevel,
      authAt: Date.now(),
    };

    sessionCache.set(telegramId, session);
    return session;
  }

  /** 更新缓存中的余额 */
  updateBalance(telegramId: number, balance: number) {
    const session = sessionCache.get(telegramId);
    if (session) {
      session.balance = balance;
    }
  }

  /** 清除会话缓存 */
  clearSession(telegramId: number) {
    sessionCache.delete(telegramId);
  }

  /** 获取缓存会话（不触发注册） */
  getCachedSession(telegramId: number): BotUserSession | undefined {
    return sessionCache.get(telegramId);
  }
}

export const userService = new UserService();
