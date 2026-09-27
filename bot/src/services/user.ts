import { apiService, UserAuthData } from './api';
import { config } from '../config';

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

const CACHE_TTL = 30 * 60; // 30 分钟（秒）

/**
 * 会话存储抽象层
 * - 配置了 REDIS_URL 时使用 Redis（支持多实例部署）
 * - 未配置时使用内存 Map（开发/单实例模式）
 */
class SessionStore {
  private memoryCache = new Map<number, { data: BotUserSession; expireAt: number }>();
  private redisClient: any = null;
  private useRedis = false;

  constructor() {
    if (config.redisUrl) {
      this.initRedis();
    }
  }

  private async initRedis() {
    try {
      // 动态导入 ioredis（未安装时 fallback 到内存）
      const Redis = (await import('ioredis')).default;
      this.redisClient = new Redis(config.redisUrl, {
        maxRetriesPerRequest: 3,
        enableReadyCheck: true,
      });
      this.redisClient.on('error', (err: Error) => {
        console.error('[Redis] 连接错误:', err.message);
      });
      this.redisClient.on('connect', () => {
        console.log('[Redis] 会话存储已连接');
        this.useRedis = true;
      });
    } catch (e) {
      console.warn('[Redis] 未安装 ioredis 或连接失败，使用内存会话存储');
      this.useRedis = false;
    }
  }

  async get(telegramId: number): Promise<BotUserSession | undefined> {
    if (this.useRedis && this.redisClient) {
      try {
        const data = await this.redisClient.get(`bot:session:${telegramId}`);
        if (data) return JSON.parse(data);
      } catch (e) {
        console.warn('[Redis] 读取会话失败，降级内存:', e.message);
      }
    }
    // 内存模式
    const entry = this.memoryCache.get(telegramId);
    if (entry && Date.now() < entry.expireAt) {
      return entry.data;
    }
    if (entry) this.memoryCache.delete(telegramId);
    return undefined;
  }

  async set(telegramId: number, session: BotUserSession): Promise<void> {
    if (this.useRedis && this.redisClient) {
      try {
        await this.redisClient.setex(
          `bot:session:${telegramId}`,
          CACHE_TTL,
          JSON.stringify(session)
        );
        return;
      } catch (e) {
        console.warn('[Redis] 写入会话失败，降级内存:', e.message);
      }
    }
    this.memoryCache.set(telegramId, {
      data: session,
      expireAt: Date.now() + CACHE_TTL * 1000,
    });
  }

  async delete(telegramId: number): Promise<void> {
    if (this.useRedis && this.redisClient) {
      try {
        await this.redisClient.del(`bot:session:${telegramId}`);
        return;
      } catch (e) {
        // ignore
      }
    }
    this.memoryCache.delete(telegramId);
  }
}

const sessionStore = new SessionStore();

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
    const cached = await sessionStore.get(telegramId);
    if (cached) {
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

    await sessionStore.set(telegramId, session);
    return session;
  }

  /** 更新缓存中的余额 */
  async updateBalance(telegramId: number, balance: number) {
    const session = await sessionStore.get(telegramId);
    if (session) {
      session.balance = balance;
      await sessionStore.set(telegramId, session);
    }
  }

  /** 清除会话缓存 */
  async clearSession(telegramId: number) {
    await sessionStore.delete(telegramId);
  }

  /** 获取缓存会话（不触发注册） */
  async getCachedSession(telegramId: number): Promise<BotUserSession | undefined> {
    return sessionStore.get(telegramId);
  }
}

export const userService = new UserService();
